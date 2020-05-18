package eu.jsparrow.core.visitor.functionalinterface;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ThisExpression;

/**
 * A visitor to search for occurrences of {@link ThisExpression}s. Does NOT
 * include {@link ThisExpression}s on {@link FieldAccess} nodes.
 * 
 * @since 3.17.0
 *
 */
public class ThisExpressionVisitor extends ASTVisitor {

	private boolean containsThisExpression = false;

	@Override
	public boolean visit(ThisExpression thisExpression) {
		this.containsThisExpression = true;
		return true;
	}

	@Override
	public boolean visit(FieldAccess fieldAccess) {
		return false;
	}

	/**
	 * 
	 * @return if there is at least one occurrence of {@link ThisExpression}
	 *         which is not part of a {@link FieldAccess}.
	 */
	public boolean hasThisExpression() {
		return containsThisExpression;
	}

}
