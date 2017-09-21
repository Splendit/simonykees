package eu.jsparrow.core.matcher;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * {@link BijectiveSimpleNameASTMatcher} allows a bijective binding between
 * two SimpleNames. Tree[{@link SimpleName} A] equals Tree[{@link SimpleName} B]
 * if the only difference is that each A is replaced with a B
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */

public class BijectiveSimpleNameASTMatcher extends ASTMatcher {

	private SimpleName ownSimpleName;
	private SimpleName otherSimpleName;

	public BijectiveSimpleNameASTMatcher(SimpleName ownSimpleName, SimpleName otherSimpleName) {
		this.ownSimpleName = ownSimpleName;
		this.otherSimpleName = otherSimpleName;
	}

	@Override
	public boolean match(SimpleName node, Object other) {
		if (!(other instanceof SimpleName)) {
			return false;
		}
		SimpleName o = (SimpleName) other;

		/*
		 * a one side relation is sufficient, because the matcher is evaluated
		 * on the referenceType, therefore the origin of reference Type is from
		 * the reference block
		 */
		boolean differnetExceptionnameMatch = node.getIdentifier().equals(ownSimpleName.getIdentifier())
				&& o.getIdentifier().equals(otherSimpleName.getIdentifier());

		boolean defaultMatch = node.getIdentifier().equals(o.getIdentifier());

		return defaultMatch || differnetExceptionnameMatch;
	}
}
