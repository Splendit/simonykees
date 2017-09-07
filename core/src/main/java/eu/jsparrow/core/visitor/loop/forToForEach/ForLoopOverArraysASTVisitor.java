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
class ForLoopOverArraysASTVisitor extends ForLoopIteratingIndexASTVisitor {


	public ForLoopOverArraysASTVisitor(SimpleName iteratingIndexName, SimpleName iterableName,
			ForStatement forStatement, Block scopeBloc) {
		super(iteratingIndexName, iterableName, forStatement, scopeBloc);
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		visitIndexOfArrayAccess(simpleName);
		return true;
	}

}
