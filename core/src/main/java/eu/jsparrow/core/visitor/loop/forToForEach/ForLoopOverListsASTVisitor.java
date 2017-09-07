package eu.jsparrow.core.visitor.loop.forToForEach;

import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * A visitor for checking the precondition of replacing a for loop iterating
 * over a {@link List} with an {@link EnhancedForStatement}.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
class ForLoopOverListsASTVisitor extends ForLoopIteratingIndexASTVisitor {
	
	public ForLoopOverListsASTVisitor(SimpleName iteratingIndexName, SimpleName iterableName,
			ForStatement forStatement, Block scopeBlock) {
		super(iteratingIndexName, iterableName, forStatement, scopeBlock);	
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		visitIndexOfIterableGet(simpleName);
		return true;
	}
}
