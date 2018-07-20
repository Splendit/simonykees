package eu.jsparrow.core.visitor.optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;

/**
 * A visitor for checking if {@link ASTNode} contains any of flow control
 * statements (return, break, continue, throw).
 * 
 * @since 2.6
 *
 */
public class FlowBreakersVisitor extends ASTVisitor {

	private boolean hasReturn = false;
	private boolean hasBreak = false;
	private boolean hasContinue = false;
	private boolean hasThrow = false;

	@Override
	public boolean preVisit2(ASTNode node) {
		return !hasFlowBreakerStatement();
	}

	@Override
	public boolean visit(ReturnStatement returnStatement) {
		hasReturn = true;
		return false;
	}

	@Override
	public boolean visit(ThrowStatement throwStatement) {
		hasThrow = true;
		return false;
	}

	@Override
	public boolean visit(ContinueStatement continueStatement) {
		hasContinue = true;
		return false;
	}

	@Override
	public boolean visit(BreakStatement breakStatement) {
		hasBreak = true;
		return false;
	}

	public boolean hasFlowBreakerStatement() {
		return hasReturn || hasBreak || hasContinue || hasThrow;
	}

}
