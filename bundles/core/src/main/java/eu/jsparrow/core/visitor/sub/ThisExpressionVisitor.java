package eu.jsparrow.core.visitor.sub;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ThisExpression;

/**
 * A visitor to search for occurrences of {@link ThisExpression}.
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

	/**
	 * 
	 * @return if at least one occurrence of {@link ThisExpression} was found
	 */
	public boolean hasThisExpression() {
		return containsThisExpression;
	}

}
