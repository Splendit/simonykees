package eu.jsparrow.core.visitor.loop.whiletoforeach;

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
class WhileLoopOverListsASTVisitor extends WhileLoopIteratingIndexASTVisitor {

	public WhileLoopOverListsASTVisitor(SimpleName iteratingIndexName, SimpleName iterableNode,
			WhileStatement whileStatement, Block parentBlock) {
		super(iteratingIndexName, iterableNode, whileStatement, parentBlock);
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		visitIndexOfIterableGet(simpleName);
		return true;
	}

}
