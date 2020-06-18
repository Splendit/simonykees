package eu.jsparrow.core.visitor.security;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import java.sql.Connection;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * 
 * 
 * A helper visitor for analyzing the creation and the references on a local
 * variable of the type {@link java.sql.Statement}.
 * 
 * @see AbstractDBQueryUsageASTVisitor
 * 
 * @since 3.16.0
 *
 */
public class SqlStatementAnalyzerVisitor extends AbstractDBQueryUsageASTVisitor {

	private MethodInvocation getResultSetInvocation;

	private MethodInvocation createStatementInvocation;

	private Statement statementContainingCreateStatement;

	public SqlStatementAnalyzerVisitor(SimpleName sqlStatement) {
		super(sqlStatement);
	}

	private MethodInvocation findGetResultSet(SimpleName simpleName) {
		StructuralPropertyDescriptor structuralDescriptor = simpleName.getLocationInParent();
		if (structuralDescriptor == MethodInvocation.EXPRESSION_PROPERTY) {
			MethodInvocation methodInvocation = (MethodInvocation) simpleName.getParent();
			SimpleName methodName = methodInvocation.getName();
			if ("getResultSet".equals(methodName.getIdentifier())) { //$NON-NLS-1$
				return methodInvocation;
			}
		}
		return null;
	}

	@Override
	protected boolean isOtherUnsafeVariableReference(SimpleName simpleName) {
		MethodInvocation getResultSet = findGetResultSet(simpleName);
		if (getResultSet != null) {
			if (this.getResultSetInvocation == null) {
				this.getResultSetInvocation = getResultSet;
			} else {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @return the first invocation of {@link java.sql.Statement#getResultSet()}
	 *         after invoking {@link java.sql.Statement#execute(String)}. In
	 *         case more than one invocation of
	 *         {@link java.sql.Statement#getResultSet()} occurs, then the flag
	 *         for unsafe usage will be set to {@code true}.
	 */
	public MethodInvocation getGetResultSetInvocation() {
		return this.getResultSetInvocation;
	}

	private MethodInvocation analyzeSqlStatementInitializer(Expression sqlStatementInitializerExpression) {
		if (sqlStatementInitializerExpression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return null;
		}
		MethodInvocation sqlStatementInitializer = (MethodInvocation) sqlStatementInitializerExpression;
		Expression connection = sqlStatementInitializer.getExpression();
		if (connection == null) {
			return null;
		}
		ITypeBinding connectionTypeBinding = connection.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(connectionTypeBinding, java.sql.Connection.class.getName())) {
			return null;
		}
		SimpleName createStatement = sqlStatementInitializer.getName();
		if (!"createStatement".equals(createStatement.getIdentifier())) { //$NON-NLS-1$
			return null;
		}
		if (!sqlStatementInitializer.arguments()
			.isEmpty()) {
			return null;
		}
		return sqlStatementInitializer;
	}

	private Statement findStatementContainingCreateStatement(MethodInvocation createStatementInvocation) {

		Statement statement = null;
		if (createStatementInvocation.getParent() == this.getDeclarationFragment()) {
			statement = (Statement) this.getDeclarationFragment()
				.getParent();

		} else if (createStatementInvocation.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			ASTNode assignment = createStatementInvocation.getParent();
			if (assignment.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
				statement = (ExpressionStatement) assignment.getParent();
			}
		}

		if (statement == null || statement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return null;
		}
		
		return statement;
	}

	/**
	 * Invokes the method
	 * {@link ASTNode#accept(org.eclipse.jdt.core.dom.ASTVisitor)} on the block
	 * given by the parameter.
	 * <p>
	 * Afterwards, further analyzing is carried out, for example:
	 * <ul>
	 * <li>to make sure that {@link #initializer} is an invocation of
	 * {@link Connection#createStatement()}</li>
	 * </ul>
	 * 
	 * @return true if the {@link SqlStatementAnalyzerVisitor} is valid,
	 *         otherwise false.
	 */
	public boolean analyze(Block block) {
		block.accept(this);
		if (unsafe) {
			return false;
		}
		createStatementInvocation = analyzeSqlStatementInitializer(initializer);

		if (createStatementInvocation != null) {
			statementContainingCreateStatement = findStatementContainingCreateStatement(createStatementInvocation);
		}
		
		if (createStatementInvocation == null || statementContainingCreateStatement == null) {
			unsafe = true;
		}
		
		return !unsafe;
	}

	/**
	 * 
	 * @return If the {@link SqlStatementAnalyzerVisitor} is valid, it is
	 *         guaranteed that a non null value of {@link MethodInvocation} is
	 *         returned.
	 */
	public MethodInvocation getCreateStatementInvocation() {
		return createStatementInvocation;
	}

	/**
	 * 
	 * @return If the {@link SqlStatementAnalyzerVisitor} is valid, it is
	 *         guaranteed that a non null value of {@link Statement} is
	 *         returned.
	 */
	public Statement getStatementContainingCreateStatement() {
		return statementContainingCreateStatement;
	}

}
