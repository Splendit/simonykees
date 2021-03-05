package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.MethodInvocation;

public class JUnit4AssertTransformationData {
	private final MethodInvocation originalMethodInvocation;
	private final Supplier<MethodInvocation> methodInvocationReplacementSupplier;

	public JUnit4AssertTransformationData(MethodInvocation originalMethodInvocation,
			Supplier<MethodInvocation> newMethodInvocationSupplier) {
		this.originalMethodInvocation = originalMethodInvocation;
		this.methodInvocationReplacementSupplier = newMethodInvocationSupplier;
	}

	public MethodInvocation getOriginalMethodInvocation() {
		return originalMethodInvocation;
	}

	public MethodInvocation createMethodInvocationReplacement() {
		return methodInvocationReplacementSupplier.get();
	}
}
