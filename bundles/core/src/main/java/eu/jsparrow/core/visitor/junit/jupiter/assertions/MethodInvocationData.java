package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Stores a {@link MethodInvocation} together with its corresponding
 * {@link IMethodBinding}. This helps to avoid resolving the same method binding
 * repeatedly.
 * 
 * @since 3.28.0
 *
 */
class MethodInvocationData {
	private final MethodInvocation methodInvocation;
	private final IMethodBinding methodBinding;

	MethodInvocationData(MethodInvocation methodInvocation) {
		this.methodInvocation = methodInvocation;
		this.methodBinding = methodInvocation.resolveMethodBinding();
	}

	MethodInvocation getMethodInvocation() {
		return methodInvocation;
	}

	IMethodBinding getMethodBinding() {
		return methodBinding;
	}
}
