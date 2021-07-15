package eu.jsparrow.core.visitor.junit.junit3;

import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Stores all information needed to replace a JUnit 3 assertion by a JUnit 4
 * assertion or by a JUnit Jupiter assertion.
 *
 * @since 4.1.0
 *
 */
public class JUnit3AssertionReplacementData {

	private final MethodInvocation originalMethodInvocation;

	private final Supplier<MethodInvocation> newMethodInvocationSupplier;

	public JUnit3AssertionReplacementData(MethodInvocation originalMethodInvocation,
			Supplier<MethodInvocation> newMethodInvocationSupplier) {
		this.originalMethodInvocation = originalMethodInvocation;
		this.newMethodInvocationSupplier = newMethodInvocationSupplier;
	}

	public MethodInvocation getOriginalMethodInvocation() {
		return originalMethodInvocation;
	}

	public MethodInvocation createMethodInvocationReplacement() {
		return newMethodInvocationSupplier.get();
	}
}
