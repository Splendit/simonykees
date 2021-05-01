package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Immutable class storing all necessary informations about the given invocation
 * of a static method declared in a JUnit 4 class like {@code org.junit.Assert}
 * or {@code org.junit.Assume} which may be transformed to an invocation of a
 * the corresponding JUnit Jupiter method.
 * 
 * @since 3.28.0
 *
 */
class JUnit4MethodInvocationAnalysisResult {

	private final MethodInvocation methodInvocation;
	private final IMethodBinding methodBinding;
	private final boolean transformableInvocation;

	JUnit4MethodInvocationAnalysisResult(MethodInvocation methodInvocation, IMethodBinding methodBinding,
			boolean transformableInvocation) {
		this.methodInvocation = methodInvocation;
		this.methodBinding = methodBinding;
		this.transformableInvocation = transformableInvocation;
	}

	MethodInvocation getMethodInvocation() {
		return methodInvocation;
	}

	public IMethodBinding getMethodBinding() {
		return methodBinding;
	}

	boolean isTransformableInvocation() {
		return transformableInvocation;
	}
}
