package eu.jsparrow.core.visitor.security;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * A helper visitor for analyzing the creation and references of a local
 * variable.
 * 
 * @since 3.18.0
 *
 */
public abstract class AbstractLocalVariableUsageASTVisitor extends ASTVisitor {
	protected final VariableDeclarationFragment localVariableDeclarationFragment;
	protected final Block blockOfLocalVariableDeclaration;
	protected final SimpleName variableName;
	protected final CompilationUnit compilationUnit;

	protected Expression initializer;
	protected boolean unsafe = false;
	protected boolean beforeDeclaration = true;

	protected AbstractLocalVariableUsageASTVisitor(SimpleName sqlStatement, MethodInvocation methodInvocation) {
		this.variableName = sqlStatement;
		this.compilationUnit = ASTNodeUtil.getSpecificAncestor(methodInvocation, CompilationUnit.class);
		ASTNode statementDeclaringNode = compilationUnit.findDeclaringNode(sqlStatement.resolveBinding());
		localVariableDeclarationFragment = findLocalVariableDeclarationFragment(methodInvocation,
				statementDeclaringNode);
		blockOfLocalVariableDeclaration = findBlockOfLocalVariableDeclaration(localVariableDeclarationFragment);
		if (this.localVariableDeclarationFragment == null || this.blockOfLocalVariableDeclaration == null) {
			this.unsafe = true;
		}
	}

	private VariableDeclarationFragment findLocalVariableDeclarationFragment(MethodInvocation methodInvocation,
			ASTNode statementDeclaringNode) {
		MethodDeclaration methodSurroundingDeclaration = ASTNodeUtil.getSpecificAncestor(statementDeclaringNode,
				MethodDeclaration.class);
		if (methodSurroundingDeclaration == null) {
			return null;
		}
		MethodDeclaration methodSurroundingInvocation = ASTNodeUtil.getSpecificAncestor(methodInvocation,
				MethodDeclaration.class);
		if (methodSurroundingInvocation != methodSurroundingDeclaration) {
			return null;
		}
		if (statementDeclaringNode.getLocationInParent() != VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
			return null;
		}
		return (VariableDeclarationFragment) statementDeclaringNode;
	}

	private Block findBlockOfLocalVariableDeclaration(VariableDeclarationFragment declarationFragment) {
		if (declarationFragment == null) {
			return null;
		}
		ASTNode declaration = declarationFragment.getParent();
		if (declaration.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return null;
		}
		return (Block) declaration.getParent();
	}

	protected boolean isVariableReference(Expression expression) {
		if (expression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}
		SimpleName simpleName = (SimpleName) expression;
		if (!simpleName.getIdentifier()
			.equals(variableName.getIdentifier())) {
			return false;
		}
		IBinding binding = simpleName.resolveBinding();
		ASTNode declaringNode = compilationUnit.findDeclaringNode(binding);
		return declaringNode == localVariableDeclarationFragment;
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

			boolean fragmentInStatement = declarationFragment
				.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY;
			if (!fragmentInStatement) {
				this.unsafe = true;
				return false;
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

	@Override
	public boolean visit(Assignment assignment) {
		if (beforeDeclaration) {
			return false;
		}

		Expression left = assignment.getLeftHandSide();
		if (isVariableReference(left)) {
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
		if (isVariableReference(right)) {
			unsafe = true;
		}
		return true;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		if (beforeDeclaration) {
			return false;
		}

		if (simpleName == variableName) {
			return false;
		}

		if (isVariableReference(simpleName)) {
			if (simpleName.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
				unsafe = true;
			} else {
				unsafe = isOtherUnsafeVariableReference(simpleName);
			}
		}
		return true;
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
	 * @return the {@link VariableDeclarationFragment} of the
	 *         {@link java.sql.Statement} to be replaced with
	 *         {@link java.sql.PreparedStatement}.
	 */
	public VariableDeclarationFragment getDeclarationFragment() {
		return localVariableDeclarationFragment;
	}

	public Block getBlockOfLocalVariableDeclaration() {
		return blockOfLocalVariableDeclaration;
	}

	protected abstract boolean isOtherUnsafeVariableReference(SimpleName simpleName);
}
