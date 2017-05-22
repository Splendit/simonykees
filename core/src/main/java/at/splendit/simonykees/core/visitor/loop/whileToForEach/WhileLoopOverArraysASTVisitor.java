package at.splendit.simonykees.core.visitor.loop.whileToForEach;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class WhileLoopOverArraysASTVisitor extends WhileLoopIteratingIndexASTVisitor {

	protected WhileLoopOverArraysASTVisitor(SimpleName iteratingIndexName, SimpleName iterableName,
			WhileStatement whileStatement, Block parentBlock) {
		super(iteratingIndexName, iterableName, whileStatement, parentBlock);
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		return visitIndexOfArrayAccess(simpleName);
	}

}
