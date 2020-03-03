package eu.jsparrow.core.visitor.loop;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * A visitor for checking whether the values in a collection are (potentially)
 * updated.
 * 
 * 
 * @since 3.15.0
 *
 */
public class IterableNodeVisitor extends ASTVisitor {

	private SimpleName iterableNode;
	private boolean updated = false;

	public IterableNodeVisitor(SimpleName iterableNode) {
		this.iterableNode = iterableNode;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		String identifier = simpleName.getIdentifier();
		if (!identifier.equals(iterableNode.getIdentifier())) {
			return true;
		}
		if (simpleName.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
			updated = true;
		}
		if (simpleName.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY) {
			MethodInvocation methodInvocation = (MethodInvocation) simpleName.getParent();
			if (isUpdateCollectionInvocation(methodInvocation)) {
				updated = true;
			}
		}
		return true;
	}

	private boolean isUpdateCollectionInvocation(MethodInvocation methodInvocation) {
		SimpleName methodName = methodInvocation.getName();
		String identifier = methodName.getIdentifier();
		return identifier.matches("^(add|clear|remove|replace|retain|set|sort).*$"); //$NON-NLS-1$
	}

	/**
	 * @return if the collection is potentially updated. Reasons may include:
	 *         <ul>
	 *         <li>Methods starting with {@code add|clear|remove|replace|retain|set|sort} are
	 *         invoked in the collection</li>
	 *         <li>The collection is used as a parameter in another method
	 *         invocation.
	 *         </ul>
	 * 
	 */
	public boolean isUpdated() {
		return updated;
	}

}
