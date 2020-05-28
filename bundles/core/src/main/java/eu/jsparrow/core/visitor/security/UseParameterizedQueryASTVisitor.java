package eu.jsparrow.core.visitor.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.visitor.sub.VariableDeclarationsVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Replaces a dynamic query with a prepared statement. For example, the
 * following code:
 * 
 * <pre>
 * String query = "SELECT first_name FROM employee WHERE department_id ='" + departmentId + "' ORDER BY last_name";
 * Statement statement = connection.createStatement();
 * statement.execute(query);
 * ResultSet resultSet = statement.getResultSet();
 * </pre>
 * 
 * is transformed to:
 * 
 * <pre>
 * String query = "SELECT first_name FROM employee WHERE department_id = ?" + " ORDER BY last_name";
 * PreparedStatement statement = connection.prepareStatement(query);
 * statement.setString(1, departmentId);
 * ResultSet resultSet = statement.executeQuery();
 * </pre>
 * 
 * @since 3.16.0
 *
 */
public class UseParameterizedQueryASTVisitor extends AbstractDynamicQueryASTVisitor {

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean safeToAddImport = isSafeToAddImport(compilationUnit, java.sql.PreparedStatement.class.getName());
		if (!safeToAddImport) {
			return false;
		}
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		Expression queryMethodArgument = analyzeStatementExecuteQuery(methodInvocation);
		SqlVariableAnalyzerVisitor sqlVariableVisitor = createSqlVariableAnalyzerVisitor(queryMethodArgument);
		if (sqlVariableVisitor == null) {
			return true;
		}

		List<ReplaceableParameter> replaceableParameters = analyzeQueryComponents(sqlVariableVisitor);
		if (replaceableParameters.isEmpty()) {
			return true;
		}

		Expression methodExpression = methodInvocation.getExpression();
		if (methodExpression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return true;
		}
		SimpleName sqlStatement = (SimpleName) methodExpression;

		SqlStatementAnalyzerVisitor sqlStatementVisitor = analyzeSqlStatementUsages(sqlStatement, methodInvocation);
		if (sqlStatementVisitor == null) {
			return true;
		}

		// Perform the replacement.
		replaceQuery(replaceableParameters);
		List<ExpressionStatement> setParameterStatements = createSetParameterStatements(replaceableParameters,
				sqlStatement);

		MethodInvocation createStatementInvocation = (MethodInvocation) sqlStatementVisitor.getInitializer();
		VariableDeclarationFragment fragment = sqlStatementVisitor.getDeclarationFragment();
		SimpleName query = (SimpleName) methodInvocation.arguments()
			.get(0);
		replaceStatementDeclaration(fragment, createStatementInvocation, query);
		addSetters(sqlStatementVisitor.getInitializer(), setParameterStatements);

		SimpleName methodName = methodInvocation.getName();
		ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class)
			.forEach(node -> astRewrite.remove(node, null));
		if (EXECUTE.equals(methodName.getIdentifier())) {
			AST ast = methodInvocation.getAST();
			SimpleName originalName = methodInvocation.getName();
			SimpleName newName = ast.newSimpleName(EXECUTE_QUERY);
			astRewrite.replace(originalName, newName, null);
		}

		MethodInvocation getResultSetInvocation = sqlStatementVisitor.getGetResultSetInvocation();
		if (getResultSetInvocation != null) {
			removeGetResultSetInvocation(getResultSetInvocation, methodInvocation);
		}
		onRewrite();

		return true;
	}

	private void removeGetResultSetInvocation(MethodInvocation getResultSetInvocation,
			MethodInvocation methodInvocation) {
		StructuralPropertyDescriptor propertyDescriptor = getResultSetInvocation.getLocationInParent();
		if (propertyDescriptor == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			VariableDeclarationFragment originalFragment = (VariableDeclarationFragment) getResultSetInvocation
				.getParent();
			VariableDeclarationStatement statement = (VariableDeclarationStatement) originalFragment.getParent();
			AST ast = methodInvocation.getAST();
			VariableDeclarationFragment newFragment = ast.newVariableDeclarationFragment();
			newFragment.setName((SimpleName) astRewrite.createCopyTarget(originalFragment.getName()));
			newFragment.setInitializer((MethodInvocation) astRewrite.createMoveTarget(methodInvocation));
			VariableDeclarationStatement newDeclarationStatement = ast.newVariableDeclarationStatement(newFragment);
			newDeclarationStatement.setType((Type) astRewrite.createCopyTarget(statement.getType()));
			astRewrite.replace(methodInvocation.getParent(), newDeclarationStatement, null);
			if (statement.fragments()
				.size() == 1) {
				astRewrite.remove(statement, null);
			} else {
				astRewrite.remove(originalFragment, null);
			}
		} else {
			astRewrite.remove(getResultSetInvocation.getParent(), null);
		}
	}

	private boolean isRemovableGetResultSet(MethodInvocation getResultSetInvocation,
			MethodInvocation methodInvocation) {

		StructuralPropertyDescriptor propertyDescriptor = getResultSetInvocation.getLocationInParent();
		if (propertyDescriptor == ExpressionStatement.EXPRESSION_PROPERTY) {
			return true;
		} else if (propertyDescriptor == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) getResultSetInvocation.getParent();
			if (fragment.getLocationInParent() != VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
				return false;
			}
			SimpleName variableName = fragment.getName();
			String resultSetIdentifier = variableName.getIdentifier();
			Block newScope = ASTNodeUtil.getSpecificAncestor(methodInvocation, Block.class);
			VariableDeclarationsVisitor visitor = new VariableDeclarationsVisitor();
			newScope.accept(visitor);
			long numMatchingNames = visitor.getVariableDeclarationNames()
				.stream()
				.map(SimpleName::getIdentifier)
				.filter(resultSetIdentifier::equals)
				.count();
			return numMatchingNames == 1;
		}

		return false;
	}

	private SqlStatementAnalyzerVisitor analyzeSqlStatementUsages(SimpleName sqlStatement,
			MethodInvocation methodInvocation) {
		ASTNode stmDeclaration = getCompilationUnit().findDeclaringNode(sqlStatement.resolveBinding());
		SqlStatementAnalyzerVisitor sqlStatementVisitor = new SqlStatementAnalyzerVisitor(stmDeclaration, sqlStatement,
				getCompilationUnit());
		Block stmDeclarationBlock = ASTNodeUtil.getSpecificAncestor(stmDeclaration, Block.class);
		stmDeclarationBlock.accept(sqlStatementVisitor);
		if (sqlStatementVisitor.isUnsafe()) {
			return null;
		}
		Expression connectionCreateStatement = sqlStatementVisitor.getInitializer();
		if (connectionCreateStatement == null) {
			return null;
		}

		Statement statement = ASTNodeUtil.getSpecificAncestor(connectionCreateStatement, Statement.class);
		if (statement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return null;
		}

		boolean isConnectionPrepareStatement = analyzeSqlStatementInitializer(connectionCreateStatement);
		if (!isConnectionPrepareStatement) {
			return null;
		}

		MethodInvocation getResultSetInvocation = sqlStatementVisitor.getGetResultSetInvocation();
		if (getResultSetInvocation != null) {
			if (EXECUTE_QUERY.equals(methodInvocation.getName()
				.getIdentifier())) {
				return null;
			}
			boolean isRemovableRs = isRemovableGetResultSet(getResultSetInvocation, methodInvocation);
			if (!isRemovableRs) {
				return null;
			}
		}

		return sqlStatementVisitor;
	}

	private List<ReplaceableParameter> analyzeQueryComponents(SqlVariableAnalyzerVisitor sqlVariableVisitor) {
		List<Expression> queryComponents = sqlVariableVisitor.getDynamicQueryComponents();
		QueryComponentsAnalyzer componentsAnalyzer = new QueryComponentsAnalyzer(queryComponents);
		componentsAnalyzer.analyze();
		return componentsAnalyzer.getReplaceableParameters();
	}

	private void addSetters(Expression initializer, List<ExpressionStatement> setParameterStatements) {
		Statement statement = ASTNodeUtil.getSpecificAncestor(initializer, Statement.class);
		Block block = (Block) statement.getParent();
		ListRewrite listRewrite = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
		List<ExpressionStatement> setters = new ArrayList<>(setParameterStatements);
		Collections.reverse(setters);
		setters.forEach(setter -> listRewrite.insertAfter(setter, statement, null));
	}

	private void replaceStatementDeclaration(VariableDeclarationFragment fragment, MethodInvocation createStatement,
			SimpleName query) {
		VariableDeclarationStatement statement = (VariableDeclarationStatement) fragment.getParent();

		int numFragments = statement.fragments()
			.size();
		AST ast = astRewrite.getAST();
		SimpleType preparedStatementType = ast
			.newSimpleType(ast.newSimpleName(java.sql.PreparedStatement.class.getSimpleName()));
		if (numFragments > 1) {

			VariableDeclarationFragment newFragment = (VariableDeclarationFragment) astRewrite
				.createMoveTarget(fragment);
			VariableDeclarationStatement newDeclarationStatement = ast.newVariableDeclarationStatement(newFragment);
			newDeclarationStatement.setType(preparedStatementType);
			Block block = (Block) statement.getParent();
			ListRewrite listRewrtie = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
			listRewrtie.insertBefore(newDeclarationStatement, statement, null);
		} else {
			Type type = statement.getType();
			astRewrite.replace(type, preparedStatementType, null);
		}
		SimpleName name = createStatement.getName();
		astRewrite.replace(name, ast.newSimpleName("prepareStatement"), null); //$NON-NLS-1$
		ListRewrite listRewrite = astRewrite.getListRewrite(createStatement, MethodInvocation.ARGUMENTS_PROPERTY);
		listRewrite.insertFirst(astRewrite.createCopyTarget(query), null);
		addImports.add(java.sql.PreparedStatement.class.getName());

	}

	private List<ExpressionStatement> createSetParameterStatements(List<ReplaceableParameter> replaceableParameters,
			Expression statementName) {
		List<ExpressionStatement> statements = new ArrayList<>();
		AST ast = astRewrite.getAST();
		for (ReplaceableParameter parameter : replaceableParameters) {
			Expression component = parameter.getParameter();
			String setterName = parameter.getSetterName();
			Expression statementNameCopy = (Expression) astRewrite.createCopyTarget(statementName);
			MethodInvocation setter = ast.newMethodInvocation();
			setter.setExpression(statementNameCopy);
			setter.setName(ast.newSimpleName(setterName));
			int position = parameter.getPosition();
			NumberLiteral positionLiteral = ast.newNumberLiteral();
			positionLiteral.setToken(String.valueOf(position));
			Expression parameterExpression = (Expression) astRewrite.createCopyTarget(component);
			@SuppressWarnings("unchecked")
			List<Expression> setterArguments = setter.arguments();
			setterArguments.add(positionLiteral);
			setterArguments.add(parameterExpression);
			ExpressionStatement setterExpressionStatement = ast.newExpressionStatement(setter);
			statements.add(setterExpressionStatement);
		}
		return statements;
	}

	private void replaceQuery(List<ReplaceableParameter> replaceableParameters) {
		AST ast = astRewrite.getAST();
		for (ReplaceableParameter parameter : replaceableParameters) {
			StringLiteral previous = parameter.getPrevious();
			StringLiteral next = parameter.getNext();
			Expression component = parameter.getParameter();

			String oldPrevious = previous.getLiteralValue();
			String newPrevious = oldPrevious.substring(0, oldPrevious.length() - 1) + " ?"; //$NON-NLS-1$
			StringLiteral newPreviousLiteral = ast.newStringLiteral();
			newPreviousLiteral.setLiteralValue(newPrevious);
			astRewrite.replace(previous, newPreviousLiteral, null);

			String newNext = next.getLiteralValue()
				.replaceFirst("'", ""); //$NON-NLS-1$//$NON-NLS-2$
			StringLiteral newNextLiteral = ast.newStringLiteral();
			newNextLiteral.setLiteralValue(newNext);
			astRewrite.replace(next, newNextLiteral, null);

			if (component.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
				Assignment assignment = (Assignment) component.getParent();
				astRewrite.remove(assignment.getParent(), null);
			} else {
				astRewrite.remove(component, null);
			}
		}
	}

	private boolean analyzeSqlStatementInitializer(Expression sqlStatementInitializerExpression) {
		if (sqlStatementInitializerExpression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return false;
		}
		MethodInvocation sqlStatementInitializer = (MethodInvocation) sqlStatementInitializerExpression;
		Expression connection = sqlStatementInitializer.getExpression();
		ITypeBinding connectionTypeBinding = connection.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(connectionTypeBinding, java.sql.Connection.class.getName())) {
			return false;
		}
		SimpleName createStatement = sqlStatementInitializer.getName();
		if (!"createStatement".equals(createStatement.getIdentifier())) { //$NON-NLS-1$
			return false;
		}
		return sqlStatementInitializer.arguments()
			.isEmpty();
	}

	@Override
	protected boolean hasRequiredName(MethodInvocation methodInvocation) {
		if (super.hasRequiredName(methodInvocation)) {
			return !(EXECUTE.equals(methodInvocation.getName()
				.getIdentifier())
					&& methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY);
		}
		return false;
	}
}
