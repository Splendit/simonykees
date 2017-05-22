package at.splendit.simonykees.core.visitor.loop;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class WhileLoopOverListsASTVisitor extends WhileLoopIteratingIndexASTVisitor {

	
	
	protected WhileLoopOverListsASTVisitor(SimpleName iteratingIndexName, SimpleName iterableNode, WhileStatement whileStatement,
			Block parentBlock) {
		super(iteratingIndexName, iterableNode, whileStatement, parentBlock);
	}
	
	@Override
	public boolean visit(SimpleName simpleName) {
		return visitIndexOfIterableGet(simpleName);
	}

}
