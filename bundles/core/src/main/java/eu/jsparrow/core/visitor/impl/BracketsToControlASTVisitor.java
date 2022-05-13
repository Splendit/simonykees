package eu.jsparrow.core.visitor.impl;

import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;

import eu.jsparrow.core.markers.common.BracketsToControlEvent;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * ASTVisitor that searches control statements for non-block bodies and wraps it
 * into a block.
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class BracketsToControlASTVisitor extends AbstractASTRewriteASTVisitor implements BracketsToControlEvent {

	private static final Predicate<Statement> NOT_BLOCK_TEST = nodeToTest -> !(nodeToTest instanceof Block);
	private static final Predicate<Statement> NOT_BLOCK_TEST_FOR_ELSE = nodeToTest -> nodeToTest != null
			&& !(nodeToTest instanceof Block) && !(nodeToTest instanceof IfStatement);

	@Override
	public boolean visit(ForStatement node) {
		checkIfBodyIsSingleStatement(node.getBody(), NOT_BLOCK_TEST);
		return true;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		checkIfBodyIsSingleStatement(node.getBody(), NOT_BLOCK_TEST);
		return true;
	}

	@Override
	public boolean visit(WhileStatement node) {
		checkIfBodyIsSingleStatement(node.getBody(), NOT_BLOCK_TEST);
		return true;
	}

	@Override
	public boolean visit(DoStatement node) {
		checkIfBodyIsSingleStatement(node.getBody(), NOT_BLOCK_TEST);
		return true;
	}

	@Override
	public boolean visit(IfStatement node) {
		checkIfBodyIsSingleStatement(node.getThenStatement(), NOT_BLOCK_TEST);
		checkIfBodyIsSingleStatement(node.getElseStatement(), NOT_BLOCK_TEST_FOR_ELSE);
		return true;
	}

	@SuppressWarnings("unchecked")
	private void checkIfBodyIsSingleStatement(Statement body, Predicate<Statement> predicate) {
		if (predicate.test(body)) {
			Block wrappingBlock = body.getAST()
				.newBlock();
			ASTNode placeholder = astRewrite.createMoveTarget(body);
			wrappingBlock.statements()
				.add(placeholder);
			astRewrite.remove(body, null);
			astRewrite.set(body.getParent(), body.getLocationInParent(), wrappingBlock, null);
			onRewrite();
			addMarkerEvent(body);
		}
	}
}
