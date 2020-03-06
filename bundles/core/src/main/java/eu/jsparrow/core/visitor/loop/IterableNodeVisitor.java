package eu.jsparrow.core.visitor.loop;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

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

		StructuralPropertyDescriptor propertyDescriptor = simpleName.getLocationInParent();
		if (propertyDescriptor == Assignment.LEFT_HAND_SIDE_PROPERTY 
				|| propertyDescriptor == Assignment.RIGHT_HAND_SIDE_PROPERTY
				|| propertyDescriptor == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			updated = true;
		}

		if (propertyDescriptor == MethodInvocation.ARGUMENTS_PROPERTY
				|| propertyDescriptor == ClassInstanceCreation.ARGUMENTS_PROPERTY) {
			updated = true;
		}
		if (propertyDescriptor == MethodInvocation.EXPRESSION_PROPERTY) {
			MethodInvocation methodInvocation = (MethodInvocation) simpleName.getParent();
			if (!isSafeCollectionOperation(methodInvocation)) {
				updated = true;
			}
		}
		return true;
	}
	
	@SuppressWarnings("nls")
	private boolean isSafeCollectionOperation(MethodInvocation methodInvocation) {
		SimpleName methodName = methodInvocation.getName();
		String identifier = methodName.getIdentifier();
		return identifier.matches("^("
				+ "equals|hashCode|toString|"
				+ "iterator|forEach|spliterator|" // iterator
				+ "size|isEmpty|contains|equals|" // collection
				+ "get|indexOf|lastIndexOf).*$"); // list/stack
	}

	/**
	 * @return if the iterable object is potentially updated. Reasons may include:
	 *         <ul>
	 *         <li>invocation of methods that might change contents of the iterable objects</li>
	 *         <li>The iterable object is used as a parameter in another method
	 *         invocation.
	 *         <li>the iterable object is reassigned or is used to assign other objects</li>
	 *         </ul>
	 * 
	 */
	public boolean isUpdated() {
		return updated;
	}

}
