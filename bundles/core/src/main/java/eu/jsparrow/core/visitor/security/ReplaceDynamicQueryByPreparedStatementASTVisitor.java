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
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.visitor.loop.DeclaredTypesASTVisitor;
import eu.jsparrow.core.visitor.sub.VariableDeclarationsVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * 
 * @since 3.16.0
 *
 */
public class ReplaceDynamicQueryByPreparedStatementASTVisitor extends AbstractAddImportASTVisitor {

	private static final String EXECUTE = "execute"; //$NON-NLS-1$
	private static final String EXECUTE_QUERY = "executeQuery"; //$NON-NLS-1$

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean safeToAddImport = isSafeToAddImport(compilationUnit, java.sql.PreparedStatement.class);
		if (!safeToAddImport) {
			return false;
		}
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		boolean hasRightTypeAndName = analyzeStatementExecuteQuery(methodInvocation);
		if (!hasRightTypeAndName) {
			return true;
		}

		SqlVariableAnalyzerVisitor sqlVariableVisitor = analyzeSqlVariableReferences(methodInvocation);
		if (sqlVariableVisitor == null || sqlVariableVisitor.isUnsafe()) {
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

		// 5 Perform the replacement.
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

		return true;
	}

	private boolean isSafeToAddImport(CompilationUnit compilationUnit, Class<?> clazz) {
		DeclaredTypesASTVisitor visitor = new DeclaredTypesASTVisitor();
		compilationUnit.accept(visitor);
		boolean matchesInnerClassName = visitor.getDeclaredTypes()
			.keySet()
			.stream()
			.anyMatch(name -> name.equals(clazz.getSimpleName()));
		if (matchesInnerClassName) {
			return false;
		}

		List<ImportDeclaration> importDeclarations = ASTNodeUtil.convertToTypedList(compilationUnit.imports(),
				ImportDeclaration.class);

		boolean clashing = importDeclarations.stream()
			.map(ImportDeclaration::getName)
			.filter(Name::isQualifiedName)
			.map(name -> (QualifiedName) name)
			.map(QualifiedName::getName)
			.anyMatch(importedName -> clazz.getSimpleName()
				.equals(importedName.getIdentifier()));
		if (clashing) {
			return false;
		}

		clashing = importDeclarations.stream()
			.map(ImportDeclaration::getName)
			.filter(Name::isSimpleName)
			.anyMatch(name -> clazz.getSimpleName()
				.equals(((SimpleName) name).getIdentifier()));

		if (clashing) {
			return false;
		}

		return importDeclarations.stream()
			.noneMatch(importDeclaration -> ClassRelationUtil.importsTypeOnDemand(importDeclaration, clazz.getName()));

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

	private SqlVariableAnalyzerVisitor analyzeSqlVariableReferences(MethodInvocation methodInvocation) {
		SimpleName query = (SimpleName) methodInvocation.arguments()
			.get(0);
		IBinding queryVariableBinding = query.resolveBinding();
		if (queryVariableBinding.getKind() != IBinding.VARIABLE) {
			return null;
		}

		ASTNode declaringNode = this.getCompilationUnit()
			.findDeclaringNode(queryVariableBinding);
		if (declaringNode == null || declaringNode.getNodeType() != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return null;
		}

		SqlVariableAnalyzerVisitor sqlVariableVisitor = new SqlVariableAnalyzerVisitor(query, declaringNode,
				getCompilationUnit());
		Block enclosingBlock = ASTNodeUtil.getSpecificAncestor(declaringNode, Block.class);
		enclosingBlock.accept(sqlVariableVisitor);
		if (sqlVariableVisitor.isUnsafe()) {
			return null;
		}
		return sqlVariableVisitor;
	}

	private boolean analyzeStatementExecuteQuery(MethodInvocation methodInvocation) {
		SimpleName methodName = methodInvocation.getName();
		if (!EXECUTE.equals(methodName.getIdentifier()) && !EXECUTE_QUERY.equals(methodName.getIdentifier())) {
			return false;
		}
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if (!ClassRelationUtil.isContentOfType(declaringClass, java.sql.Statement.class.getName())) {
			return false;
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if (arguments.size() != 1) {
			return false;
		}

		Expression argument = arguments.get(0);
		ITypeBinding argumentTypeBinding = argument.resolveTypeBinding();
		boolean isString = ClassRelationUtil.isContentOfType(argumentTypeBinding, java.lang.String.class.getName());
		if (!isString) {
			return false;
		}

		Expression methodExpression = methodInvocation.getExpression();
		if (methodExpression == null) {
			return false;
		}

		if (argument.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}

		if (EXECUTE.equals(methodName.getIdentifier())
				&& methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return false;
		}

		ITypeBinding methodExpressionTypeBinding = methodExpression.resolveTypeBinding();
		return ClassRelationUtil.isContentOfType(methodExpressionTypeBinding, java.sql.Statement.class.getName());
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
			listRewrtie.insertAfter(newDeclarationStatement, statement, null);
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

}
