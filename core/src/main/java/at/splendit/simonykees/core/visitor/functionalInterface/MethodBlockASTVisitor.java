package at.splendit.simonykees.core.visitor.functionalInterface;

import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * Find the MethodDeclaration of the Functionalinterface
 * 
 * @author Martin Huter, Ardit Ymeri
 * @since 1.2
 *
 */
class MethodBlockASTVisitor extends AbstractASTRewriteASTVisitor {
	private Block methodBlock = null;
	private List<SingleVariableDeclaration> parameters = null;

	@Override
	public boolean visit(Block node) {
		methodBlock = (Block) astRewrite.createMoveTarget(node);
		astRewrite.remove(node, null);
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (!node.parameters().isEmpty()) {
			/**
			 * node.parameters() ensures that the List contains only
			 * SingleVariableDeclaration
			 */
			parameters = ASTNodeUtil.returnTypedList(node.parameters(), SingleVariableDeclaration.class);
		}
		methodBlock = node.getBody();
		return false;
	}

	public Block getMethodBlock() {
		return methodBlock;
	}

	public List<SingleVariableDeclaration> getParameters() {
		return parameters;
	}
}
