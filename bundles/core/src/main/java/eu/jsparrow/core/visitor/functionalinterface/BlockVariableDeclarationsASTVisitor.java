package eu.jsparrow.core.visitor.functionalinterface;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * Collects the Variable Declaration SimpleNames within a Block
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
class BlockVariableDeclarationsASTVisitor extends ASTVisitor {
	private List<SimpleName> blockVariableNames;

	public BlockVariableDeclarationsASTVisitor() {
		blockVariableNames = new ArrayList<>();
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		blockVariableNames.add(node.getName());
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		return false;
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		blockVariableNames.add(node.getName());
		return true;
	}

	public List<SimpleName> getBlockVariableDeclarations() {
		return blockVariableNames;
	}
}
