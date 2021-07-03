package eu.jsparrow.core.visitor.junit.junit3;

import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.MethodInvocation;

public class JUnit3AssertionReplacementData {

	private final MethodInvocation originalMethodInvocation;

	private final Supplier<MethodInvocation> newMethodInvocationSupplier;

	public JUnit3AssertionReplacementData(MethodInvocation originalMethodInvocation,
			Supplier<MethodInvocation> newMethodInvocationSupplier) {
		super();
		this.originalMethodInvocation = originalMethodInvocation;
		this.newMethodInvocationSupplier = newMethodInvocationSupplier;
	}

	public MethodInvocation getOriginalMethodInvocation() {
		return originalMethodInvocation;
	}

	public MethodInvocation createMethodInvocationReplecement() {
		return newMethodInvocationSupplier.get();
	}
}
