package eu.jsparrow.core.visitor.security;

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
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
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
		Expression executeQueryArgument = analyzeStatementExecuteQuery(methodInvocation);
		SqlVariableAnalyzerVisitor sqlVariableVisitor = createSqlVariableAnalyzerVisitor(executeQueryArgument);
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

		Statement statementContainingExecuteQuery = ASTNodeUtil.getSpecificAncestor(methodInvocation, Statement.class);

		transformCreateStatement(
				createStatementInvocation, statementContainingExecuteQuery, executeQueryArgument);

		insertStatementsBeforeExecuteQuery(statementContainingExecuteQuery, setParameterStatements);

		replaceStatementDeclaration(fragment);

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

	private void transformCreateStatement(MethodInvocation createStatementInvocation,
			Statement statementContainingExecuteQuery,
			Expression executeQueryArgument) {

		ExpressionStatement prepareStatementOnNewPosition = createPrepareStatementOnNewPosition(
				createStatementInvocation, statementContainingExecuteQuery);

		if (prepareStatementOnNewPosition != null) {
			Block block = (Block) statementContainingExecuteQuery.getParent();
			ListRewrite listRewrite = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
			listRewrite.insertBefore(prepareStatementOnNewPosition, statementContainingExecuteQuery, null);
		}

		AST ast = createStatementInvocation.getAST();
		SimpleName name = createStatementInvocation.getName();
		astRewrite.replace(name, ast.newSimpleName("prepareStatement"), null); //$NON-NLS-1$
		ListRewrite listRewrite = astRewrite.getListRewrite(createStatementInvocation,
				MethodInvocation.ARGUMENTS_PROPERTY);
		listRewrite.insertFirst(astRewrite.createMoveTarget(executeQueryArgument), null);
	}

	private boolean isBeforeStatementContainingExecuteQuery(Statement statement,
			Statement statementContainingExecuteQuery) {
		if(statement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return false;
		}
		Block block = (Block)statement.getParent();
		int indexOfStatementContainingExecuteQuery = block.statements().indexOf(statementContainingExecuteQuery);
		if(indexOfStatementContainingExecuteQuery < 1) {
			return false;
		}
		return block.statements().indexOf(statement) == indexOfStatementContainingExecuteQuery - 1;
	}

	private ExpressionStatement createPrepareStatementOnNewPosition(MethodInvocation createStatementInvocation,
			Statement statementContainingExecuteQuery) {
		AST ast = createStatementInvocation.getAST();
		ExpressionStatement prepareStatementOnNewPosition = null;
		StructuralPropertyDescriptor locationInParent = createStatementInvocation.getLocationInParent();
		if (locationInParent == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			Assignment prepareStatementAssignment = (Assignment) createStatementInvocation.getParent();
			if (prepareStatementAssignment.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
				ExpressionStatement expressionStatement = (ExpressionStatement) prepareStatementAssignment.getParent();
				if (isBeforeStatementContainingExecuteQuery(expressionStatement, statementContainingExecuteQuery)) {
					return null;
				}
				prepareStatementOnNewPosition = (ExpressionStatement) astRewrite
					.createMoveTarget(expressionStatement);

			}
		} else if (locationInParent == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) createStatementInvocation.getParent();
			if (fragment.getLocationInParent() != VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
				return null;
			}
			if (isBeforeStatementContainingExecuteQuery((VariableDeclarationStatement) fragment.getParent(),
					statementContainingExecuteQuery)) {
				return null;
			}
			Assignment assignment = ast.newAssignment();
			assignment.setLeftHandSide(ast.newSimpleName(fragment.getName()
				.getIdentifier()));
			assignment.setRightHandSide((Expression) astRewrite.createMoveTarget(createStatementInvocation));
			prepareStatementOnNewPosition = ast.newExpressionStatement(assignment);
		}
		return prepareStatementOnNewPosition;
	}

	protected void insertStatementsBeforeExecuteQuery(Statement executeQueryStatement,
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

		Block surroundingBody = this.findSurroundingBody(methodInvocation);
		if (surroundingBody == null) {
			return null;
		}
		SqlStatementAnalyzerVisitor sqlStatementVisitor = new SqlStatementAnalyzerVisitor(sqlStatement);
		surroundingBody.accept(sqlStatementVisitor);

		if (!sqlStatementVisitor.hasFoundDeclaration()) {
			return null;
		}
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
		return componentsAnalyzer.createReplaceableParameterList();
	}

	private void replaceStatementDeclaration(VariableDeclarationFragment fragment) {
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
		addImports.add(java.sql.PreparedStatement.class.getName());
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
