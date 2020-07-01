package eu.jsparrow.core.visitor.security;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * A helper visitor which analyzes the creation and usage of local variables
 * used for the execution of database queries.
 * 
 * Subclasses of this class are intended to be used by visitor classes in
 * connection with the fixing of potential SQL injection.
 * 
 * @since 3.18.0
 *
 */
public abstract class AbstractDBQueryUsageASTVisitor extends ASTVisitor {
	protected final VariableDeclarationFragment localVariableDeclarationFragment;
	protected final SimpleName variableName;
	protected final CompilationUnit compilationUnit;
	protected Expression initializer;
	protected boolean unsafe = false;
	protected boolean beforeDeclaration = true;

	protected AbstractDBQueryUsageASTVisitor(SimpleName databaseQuery) {
		this.variableName = databaseQuery;
		this.compilationUnit = ASTNodeUtil.getSpecificAncestor(databaseQuery, CompilationUnit.class);
		ASTNode statementDeclaringNode = compilationUnit.findDeclaringNode(databaseQuery.resolveBinding());
		localVariableDeclarationFragment = findLocalVariableDeclarationFragment(statementDeclaringNode);
		if (this.localVariableDeclarationFragment == null) {
			this.unsafe = true;
		}
	}

	private VariableDeclarationFragment findLocalVariableDeclarationFragment(ASTNode statementDeclaringNode) {

		if (statementDeclaringNode.getLocationInParent() != VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
			return null;
		}
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statementDeclaringNode
			.getParent();
		if (variableDeclarationStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return null;
		}
		return (VariableDeclarationFragment) statementDeclaringNode;
	}

	protected boolean isVariableReference(SimpleName simpleName) {
		if (!simpleName.getIdentifier()
			.equals(variableName.getIdentifier())) {
			return false;
		}
		IBinding binding = simpleName.resolveBinding();
		ASTNode declaringNode = compilationUnit.findDeclaringNode(binding);
		return declaringNode == localVariableDeclarationFragment;
	}

	private boolean isUnsafeVariableReference(SimpleName simpleName) {
		StructuralPropertyDescriptor locationInParent = simpleName.getLocationInParent();
		if (locationInParent == Assignment.LEFT_HAND_SIDE_PROPERTY) {			
			if (initializer != null) {
				return true;
			}
			Assignment assignment = (Assignment) simpleName.getParent();
			Expression right = assignment.getRightHandSide();
			if (right.getNodeType() != ASTNode.NULL_LITERAL) {
				this.initializer = right;
			}
			return false;
		}
		if (locationInParent == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			return true;
		}
		if (locationInParent == MethodInvocation.ARGUMENTS_PROPERTY) {
			return true;
		}
		if (locationInParent == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			return true;
		}
		if (simpleName == variableName) {
			return false;
		}
		if (simpleName == localVariableDeclarationFragment.getName()) {
			return false;
		}
		return isOtherUnsafeVariableReference(simpleName);
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !unsafe;
	}

	@Override
	public boolean visit(VariableDeclarationFragment declarationFragment) {
		if (this.localVariableDeclarationFragment == declarationFragment) {
			beforeDeclaration = false;
			Expression statementInitializer = declarationFragment.getInitializer();
			if (statementInitializer != null && statementInitializer.getNodeType() != ASTNode.NULL_LITERAL) {
				this.initializer = statementInitializer;
			}
		}
		return true;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		if (beforeDeclaration) {
			return false;
		}
		if (isVariableReference(simpleName)) {
			unsafe = isUnsafeVariableReference(simpleName);
		}
		return true;
	}

	/**
	 * There are various reasons which make the further usage of a variable
	 * analyzed by {@link AbstractDBQueryUsageASTVisitor} unsafe, for example:
	 * <ul>
	 * <li>The usage of the variable itself is unsafe.</li>
	 * <li>The declaration of the analyzed variable could not be found.</li>
	 * <li>No valid initialization of the analyzed variable could be found.</li>
	 * </ul>
	 * 
	 * @return true if the analyzed variable declaration is unsafe and prohibits
	 *         the transformation of code.
	 */
	public boolean isUnsafe() {
		return unsafe || beforeDeclaration || initializer == null;
	}

	/**
	 * 
	 * @return the expression used for initializing the analyzed variable. If
	 *         {@link #isUnsafe()} returns false, it is guaranteed that the
	 *         return value of this method will not be null.
	 */
	public Expression getInitializer() {
		return initializer;
	}

	/**
	 * 
	 * @return the {@link VariableDeclarationFragment} of the
	 *         {@link java.sql.Statement} to be replaced with
	 *         {@link java.sql.PreparedStatement}.
	 */
	public VariableDeclarationFragment getDeclarationFragment() {
		return localVariableDeclarationFragment;
	}

	protected abstract boolean isOtherUnsafeVariableReference(SimpleName simpleName);
}
