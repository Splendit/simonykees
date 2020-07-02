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
	private final VariableDeclarationFragment localVariableDeclarationFragment;
	private final SimpleName variableName;
	private final CompilationUnit compilationUnit;
	private Expression initializer;
	private boolean unsafe = false;
	private boolean beforeDeclaration = true;

	protected AbstractDBQueryUsageASTVisitor(SimpleName databaseQuery) {
		this.variableName = databaseQuery;
		this.compilationUnit = ASTNodeUtil.getSpecificAncestor(databaseQuery, CompilationUnit.class);
		ASTNode statementDeclaringNode = compilationUnit.findDeclaringNode(databaseQuery.resolveBinding());
		localVariableDeclarationFragment = findLocalVariableDeclarationFragment(statementDeclaringNode);	
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
	 * 
	 * @return the {@link VariableDeclarationFragment} of the
	 *         {@link java.sql.Statement} to be replaced with
	 *         {@link java.sql.PreparedStatement}.
	 */
	public VariableDeclarationFragment getDeclarationFragment() {
		return localVariableDeclarationFragment;
	}

	/**
	 * 
	 * @return as soon as {@link #analyze(Block)} returns true, it is guaranteed
	 *         that the return value of this method will not be null.
	 */
	public Expression getInitializer() {
		return initializer;
	}

	/**
	 * Invokes the method
	 * {@link ASTNode#accept(org.eclipse.jdt.core.dom.ASTVisitor)} on the block
	 * given by the parameter.
	 * 
	 * @return true if all the following conditions are fulfilled:
	 *         <ul>
	 *         <li>a declaration has been found</li>
	 *         <li>an initialization has been found</li>
	 *         <li>the variable is used in a safe way</li>
	 *         </ul>
	 *         and false as soon as one of the requirements from above is
	 *         missing.
	 *
	 */
	public boolean analyze(Block block) {
		if (this.localVariableDeclarationFragment == null) {
			return false;
		}
		block.accept(this);
		return !unsafe && !beforeDeclaration && initializer != null;
	}

	protected abstract boolean isOtherUnsafeVariableReference(SimpleName simpleName);
}
