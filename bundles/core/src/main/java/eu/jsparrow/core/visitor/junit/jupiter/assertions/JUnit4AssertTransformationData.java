package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.MethodInvocation;

public class JUnit4AssertTransformationData {
	private final MethodInvocation originalMethodInvocation;
	private final String staticImportForNewInvocation;
	private final Supplier<MethodInvocation> methodInvocationReplacementSupplier;

	public JUnit4AssertTransformationData(MethodInvocation originalMethodInvocation,
			String staticImportForNewInvocation,
			Supplier<MethodInvocation> newMethodInvocationSupplier) {
		this.originalMethodInvocation = originalMethodInvocation;
		this.staticImportForNewInvocation = staticImportForNewInvocation;
		this.methodInvocationReplacementSupplier = newMethodInvocationSupplier;
	}

	public JUnit4AssertTransformationData(MethodInvocation originalMethodInvocation,
			String staticImportForNewInvocation) {
		this.originalMethodInvocation = originalMethodInvocation;
		this.staticImportForNewInvocation = staticImportForNewInvocation;
		this.methodInvocationReplacementSupplier = null;
	}

	public JUnit4AssertTransformationData(MethodInvocation originalMethodInvocation,
			Supplier<MethodInvocation> newMethodInvocationSupplier) {
		this.originalMethodInvocation = originalMethodInvocation;
		this.staticImportForNewInvocation = null;
		this.methodInvocationReplacementSupplier = newMethodInvocationSupplier;
	}

	public MethodInvocation getOriginalMethodInvocation() {
		return originalMethodInvocation;
	}

	public Optional<String> getNewStaticMethodImport() {
		return Optional.ofNullable(staticImportForNewInvocation);
	}

	public Optional<MethodInvocation> createMethodInvocationReplacement() {
		if (methodInvocationReplacementSupplier != null) {
			return Optional.of(methodInvocationReplacementSupplier.get());
		}
		return Optional.empty();
	}

}
