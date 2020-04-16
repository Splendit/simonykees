package eu.jsparrow.core.visitor.security;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * A helper visitor for analyzing the creation and references of a
 * {@link java.sql.Statement}.
 * 
 * @since 3.16.0
 *
 */
public class SqlStatementAnalyzerVisitor extends ASTVisitor {

	private ASTNode declaration;
	private SimpleName statementName;
	private Expression initializer;
	private CompilationUnit compilationUnit;
	private MethodInvocation getResultSetInvocation;
	private VariableDeclarationFragment variableDeclarationFragment;
	private boolean unsafe = false;
	private boolean beforeDeclaration = true;

	public SqlStatementAnalyzerVisitor(ASTNode declaration, SimpleName sqlStatement, CompilationUnit compilationUnit) {
		this.declaration = declaration;
		this.statementName = sqlStatement;
		this.compilationUnit = compilationUnit;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !unsafe;
	}

	@Override
	public boolean visit(VariableDeclarationFragment declarationFragment) {
		if (this.declaration == declarationFragment) {
			this.variableDeclarationFragment = declarationFragment;
			beforeDeclaration = false;
			Expression statementInitializer = declarationFragment.getInitializer();
			if (statementInitializer != null && statementInitializer.getNodeType() != ASTNode.NULL_LITERAL) {
				this.initializer = statementInitializer;
			}

			boolean fragmentInStatement = declarationFragment
				.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY;
			if (!fragmentInStatement) {
				this.unsafe = true;
			}

			VariableDeclarationStatement statement = (VariableDeclarationStatement) declarationFragment.getParent();
			boolean statementInBlock = Block.STATEMENTS_PROPERTY == statement.getLocationInParent();
			if (!statementInBlock) {
				this.unsafe = true;
			}

			return false;
		}
		return true;
	}

	private boolean isStatementReference(Expression expression) {
		if (expression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}
		SimpleName simpleName = (SimpleName) expression;
		if (!simpleName.getIdentifier()
			.equals(statementName.getIdentifier())) {
			return false;
		}
		IBinding binding = simpleName.resolveBinding();
		ASTNode declaringNode = compilationUnit.findDeclaringNode(binding);
		return declaringNode == declaration;
	}

	@Override
	public boolean visit(Assignment assignment) {
		if (beforeDeclaration) {
			return false;
		}

		Expression left = assignment.getLeftHandSide();
		if (isStatementReference(left)) {
			if (initializer == null) {
				Expression right = assignment.getRightHandSide();
				if (right.getNodeType() != ASTNode.NULL_LITERAL) {
					this.initializer = right;
					return false;
				}
			} else {
				unsafe = true;
			}

		}

		Expression right = assignment.getRightHandSide();
		if (isStatementReference(right)) {
			unsafe = true;
		}

		return true;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		if (beforeDeclaration) {
			return false;
		}

		if (simpleName == statementName) {
			return false;
		}

		if (isStatementReference(simpleName)) {
			MethodInvocation getResultSet = findGetResultSet(simpleName);
			if (getResultSet != null) {
				if (this.getResultSetInvocation == null) {
					this.getResultSetInvocation = getResultSet;
				} else {
					unsafe = true;
				}
			} else if (simpleName.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
				unsafe = true;
			}
		}

		return true;
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

	/**
	 * 
	 * @return if it is not safe to replace the {@link java.sql.Statement} with
	 *         a {@link java.sql.PreparedStatement}.
	 */
	public boolean isUnsafe() {
		return unsafe;
	}

	/**
	 * 
	 * @return the expression used for initializing the original
	 *         {@link java.sql.Statement}.
	 */
	public Expression getInitializer() {
		return initializer;
	}

	/**
	 * 
	 * @return the first invocation of {@link java.sql.Statement#getResultSet()}
	 *         after invoking {@link java.sql.Statement#execute(String)}. In
	 *         case more than one invocation of
	 *         {@link java.sql.Statement#getResultSet()} occurs, then the flag
	 *         {@link #unsafe} will be set to {@code true}.
	 */
	public MethodInvocation getGetResultSetInvocation() {
		return this.getResultSetInvocation;
	}

	/**
	 * 
	 * @return the {@link VariableDeclarationFragment} of the
	 *         {@link java.sql.Statement} to be replaced with
	 *         {@link java.sql.PreparedStatement}.
	 */
	public VariableDeclarationFragment getDeclarationFragment() {
		return variableDeclarationFragment;
	}

}
