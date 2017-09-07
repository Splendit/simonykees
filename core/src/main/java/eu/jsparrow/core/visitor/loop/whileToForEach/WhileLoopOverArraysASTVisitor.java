package eu.jsparrow.core.visitor.loop.whileToForEach;

import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * A visitor for checking the precondition of replacing a while loop iterating
 * over a {@link List} with an {@link EnhancedForStatement}.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
class WhileLoopOverArraysASTVisitor extends WhileLoopIteratingIndexASTVisitor {

	public WhileLoopOverArraysASTVisitor(SimpleName iteratingIndexName, SimpleName iterableName,
			WhileStatement whileStatement, Block parentBlock) {
		super(iteratingIndexName, iterableName, whileStatement, parentBlock);
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		visitIndexOfArrayAccess(simpleName);
		return true;
	}

}
