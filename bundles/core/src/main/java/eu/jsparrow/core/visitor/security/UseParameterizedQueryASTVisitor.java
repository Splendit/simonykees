package eu.jsparrow.core.visitor.security;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

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

	private static final String PREPARED_STATEMENT_QUALIFIED_NAME = java.sql.PreparedStatement.class.getName();

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if (continueVisiting) {
			verifyImport(compilationUnit, PREPARED_STATEMENT_QUALIFIED_NAME);
		}
		return continueVisiting;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		Expression executeQueryArgument = analyzeStatementExecuteQuery(methodInvocation);
		if (executeQueryArgument == null) {
			return true;
		}
		List<Expression> dynamicQueryComponents = findDynamicQueryComponents(executeQueryArgument);
		QueryComponentsAnalyzer componentsAnalyzer = new QueryComponentsAnalyzer(dynamicQueryComponents);
		List<ReplaceableParameter> replaceableParameters = componentsAnalyzer.createReplaceableParameterList();
		if (replaceableParameters.isEmpty()) {
			return true;
		}

		Expression methodExpression = methodInvocation.getExpression();
		if (methodExpression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return true;
		}
		SimpleName sqlStatement = (SimpleName) methodExpression;

		Block surroundingBody = this.findSurroundingBody(methodInvocation);
		if (surroundingBody == null) {
			return true;
		}

		SqlStatementAnalyzerVisitor sqlStatementVisitor = new SqlStatementAnalyzerVisitor(sqlStatement,
				methodInvocation);
		if (!sqlStatementVisitor.analyze(surroundingBody)) {
			return true;
		}

		MethodInvocation createStatementInvocation = sqlStatementVisitor.getCreateStatementInvocation();
		Statement statementContainingCreateStatement = sqlStatementVisitor.getStatementContainingCreateStatement();

		Statement statementContainingExecuteQuery = ASTNodeUtil.getSpecificAncestor(methodInvocation, Statement.class);
		if (statementContainingExecuteQuery.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return true;
		}

		// Perform the replacement.
		replaceQuery(replaceableParameters);
		List<ExpressionStatement> setParameterStatements = createSetParameterStatements(replaceableParameters,
				sqlStatement);

		moveStatementContainingCreateStatement(statementContainingCreateStatement, createStatementInvocation,
				statementContainingExecuteQuery);

		transformCreateStatementInvocation(createStatementInvocation, executeQueryArgument);

		insertSetParameterStatementsBeforeExecuteQuery(statementContainingExecuteQuery, setParameterStatements);

		replaceStatementDeclaration(sqlStatementVisitor.getDeclarationFragment());

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

	private void transformCreateStatementInvocation(
			MethodInvocation createStatementInvocation, Expression executeQueryArgument) {
		AST ast = createStatementInvocation.getAST();
		SimpleName name = createStatementInvocation.getName();
		astRewrite.replace(name, ast.newSimpleName("prepareStatement"), null); //$NON-NLS-1$
		ListRewrite argumentsListRewrite = astRewrite.getListRewrite(createStatementInvocation,
				MethodInvocation.ARGUMENTS_PROPERTY);
		argumentsListRewrite.insertFirst(astRewrite.createMoveTarget(executeQueryArgument), null);
	}

	private void moveStatementContainingCreateStatement(Statement statementContainingCreateStatement,
			MethodInvocation createStatementInvocation, Statement statementContainingExecuteQuery) {

		Block block = (Block) statementContainingExecuteQuery.getParent();
		int executeQueryIndex = block.statements()
			.indexOf(statementContainingExecuteQuery);
		if (executeQueryIndex >= 1) {
			int createStatementIndex = block.statements()
				.indexOf(statementContainingCreateStatement);
			if (createStatementIndex == executeQueryIndex - 1) {
				return;
			}
		}
		AST ast = createStatementInvocation.getAST();
		Statement statementOnNewPosition;
		if (createStatementInvocation.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) createStatementInvocation
				.getParent();
			Assignment assignment = ast.newAssignment();
			assignment.setLeftHandSide(ast.newSimpleName(fragment.getName()
				.getIdentifier()));
			assignment.setRightHandSide((Expression) astRewrite.createMoveTarget(createStatementInvocation));
			statementOnNewPosition = ast.newExpressionStatement(assignment);
		} else {
			statementOnNewPosition = (Statement) astRewrite.createMoveTarget(statementContainingCreateStatement);
		}
		ListRewrite listRewrite = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
		listRewrite.insertBefore(statementOnNewPosition, statementContainingExecuteQuery, null);

	}

	protected void insertSetParameterStatementsBeforeExecuteQuery(Statement executeQueryStatement,
			List<ExpressionStatement> setParameterStatements) {
		Block block = (Block) executeQueryStatement.getParent();
		ListRewrite listRewrite = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
		setParameterStatements
			.forEach(setter -> listRewrite.insertBefore(setter, executeQueryStatement, null));
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

	private void replaceStatementDeclaration(VariableDeclarationFragment fragment) {
		VariableDeclarationStatement statement = (VariableDeclarationStatement) fragment.getParent();

		int numFragments = statement.fragments()
			.size();
		AST ast = astRewrite.getAST();
		addImport(PREPARED_STATEMENT_QUALIFIED_NAME);
		Name preparedStatementTypeName = findTypeName(PREPARED_STATEMENT_QUALIFIED_NAME);
		SimpleType preparedStatementType = ast.newSimpleType(preparedStatementTypeName);
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

	@Override
	protected String getNewPreviousLiteralValue(ReplaceableParameter parameter) {
		String oldPrevious = parameter.getPrevious()
			.getLiteralValue();
		return oldPrevious.substring(0, oldPrevious.length() - 1) + " ?"; //$NON-NLS-1$
	}

	@Override
	protected String getNewNextLiteralValue(ReplaceableParameter parameter) {
		return parameter.getNext()
			.getLiteralValue()
			.replaceFirst("'", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
