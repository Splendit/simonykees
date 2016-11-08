package at.splendit.simonykees.core.matcher.tryStatement;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.SimpleName;

public class DifferentExceptionNameASTMatcher extends ASTMatcher {

	SimpleName referenceExeptionName;
	SimpleName currentExceptionName;

	public DifferentExceptionNameASTMatcher(SimpleName referenceExeptionName, SimpleName currentExceptionName) {
		this.referenceExeptionName = referenceExeptionName;
		this.currentExceptionName = currentExceptionName;
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
		boolean differnetExceptionnameMatch = node.getIdentifier().equals(referenceExeptionName.getIdentifier())
				&& o.getIdentifier().equals(currentExceptionName.getIdentifier());

		boolean defaultMatch = node.getIdentifier().equals(o.getIdentifier());

		return defaultMatch || differnetExceptionnameMatch;
	}
}
