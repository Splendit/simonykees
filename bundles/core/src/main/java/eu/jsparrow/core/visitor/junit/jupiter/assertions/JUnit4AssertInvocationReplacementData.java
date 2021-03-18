package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Stores a {@link MethodInvocation} to be replaced together with the
 * corresponding {@link Supplier} to create its replacement.
 * 
 * @since 3.28.0
 *
 */
public class JUnit4AssertInvocationReplacementData {
	private final MethodInvocation originalMethodInvocation;
	private final Supplier<MethodInvocation> methodInvocationReplacementSupplier;

	public JUnit4AssertInvocationReplacementData(MethodInvocation originalMethodInvocation,
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
