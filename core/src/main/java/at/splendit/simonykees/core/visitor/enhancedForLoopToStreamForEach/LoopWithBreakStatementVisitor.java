package at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * A visitor for analyzing an {@link EnhancedForStatement} whether it 
 * consists of the shape:
 * 
 * <pre>
 * <code>
 * boolean boolVarName = false;
 * for (Object val : values) {
 *     if (condition(val)) {
 *         boolVarName = true;
 *         break;
 *     }
 * }
 * </code>
 * </pre>
 * 
 * Furthermore, it finds the declaration fragment of the assigned boolean variable
 * and checks whether it initial value is {@code false}.
 * 
 * @author Ardit Ymeri
 * @since 2.1.1
 *
 */
class LoopWithBreakStatementVisitor extends ASTVisitor {

	private EnhancedForStatement forNode;
	private SimpleName varName;
	private VariableDeclarationFragment declFragment;
	private Block parentBlock;

	private boolean beforeDeclFragment = true;
	private boolean afterDeclFragment = false;
	private boolean beforeLoop = true;
	private boolean terminate = false;

	public LoopWithBreakStatementVisitor(Block block, EnhancedForStatement forNode, SimpleName boolVarName) {
		this.forNode = forNode;
		this.varName = boolVarName;
		this.parentBlock = block;
	}

	public VariableDeclarationFragment getDeclarationBoolFragment() {
		return declFragment;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !terminate;
	}

	@Override
	public boolean visit(Block block) {
		return this.parentBlock == block || afterDeclFragment;
	}

	@Override
	public boolean visit(EnhancedForStatement loop) {
		if (loop == this.forNode) {
			this.beforeLoop = false;
		}
		return true;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		IBinding binding = simpleName.resolveBinding();
		if (IBinding.VARIABLE != binding.getKind()) {
			/*
			 * The simple name doesn't represent a variable.
			 */
			return true;
		}
		if (beforeDeclFragment && simpleName.getIdentifier().equals(varName.getIdentifier())) {
			ASTNode parent = simpleName.getParent();
			if (ASTNode.VARIABLE_DECLARATION_FRAGMENT == parent.getNodeType()) {
				VariableDeclarationFragment boolDeclFragment = (VariableDeclarationFragment) parent;
				this.declFragment = boolDeclFragment;
				this.beforeDeclFragment = false;
				this.afterDeclFragment = true;
			}
		} else if (afterDeclFragment && beforeLoop
				&& simpleName.getIdentifier().equals(varName.getIdentifier())) {
			/*
			 * The boolean variable is referenced sw between its declaration
			 * and the for loop
			 */
			terminate();
		}

		return true;
	}

	private void terminate() {
		declFragment = null;
		this.terminate = true;
	}
}
