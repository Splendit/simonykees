package eu.jsparrow.core.visitor.junit.jupiter.assertthrows;

import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Immutable class storing all necessary informations about the given invocation
 * of {@code org.junit.Assert.assertThrows} which may be replaced by an
 * invocation of {@code org.junit.jupiter.api.Assertions.assertThrows}.
 * 
 * @since 3.29.0
 *
 */
public class JUnit4AssertThrowsInvocationAnalysisResult {
	private final MethodInvocation methodInvoocation;
	private final boolean messageAsFirstParameter;
	private final boolean transformableInvocation;

	public JUnit4AssertThrowsInvocationAnalysisResult(MethodInvocation methodInvoocation,
			boolean messageAsFirstParameter, boolean transformableInvocation) {
		this.methodInvoocation = methodInvoocation;
		this.messageAsFirstParameter = messageAsFirstParameter;
		this.transformableInvocation = transformableInvocation;
	}

	public MethodInvocation getMethodInvoocation() {
		return methodInvoocation;
	}

	public boolean isMessageAsFirstParameter() {
		return messageAsFirstParameter;
	}

	public boolean isTransformableInvocation() {
		return transformableInvocation;
	}
}
