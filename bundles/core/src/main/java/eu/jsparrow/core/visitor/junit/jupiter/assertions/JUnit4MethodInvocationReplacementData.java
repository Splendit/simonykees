package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Stores a {@link MethodInvocation} to be replaced together with the
 * corresponding {@link Supplier} to create its replacement.
 * 
 * @since 3.28.0
 *
 */
class JUnit4MethodInvocationReplacementData {
	private final MethodInvocation originalMethodInvocation;
	private Supplier<MethodInvocation> methodInvocationReplacementSupplier;
	private String staticMethodImport;
	
	
	JUnit4MethodInvocationReplacementData(JUnit4MethodInvocationReplacementData other) {
		this.originalMethodInvocation = other.originalMethodInvocation;
		this.methodInvocationReplacementSupplier = other.methodInvocationReplacementSupplier;
		this.staticMethodImport  = other.staticMethodImport;
	}

	JUnit4MethodInvocationReplacementData(MethodInvocation originalMethodInvocation,
			Supplier<MethodInvocation> newMethodInvocationSupplier, String staticMethodImport) {
		this(originalMethodInvocation);
		this.methodInvocationReplacementSupplier = newMethodInvocationSupplier;
		this.staticMethodImport = staticMethodImport;
	}

	JUnit4MethodInvocationReplacementData(MethodInvocation originalMethodInvocation,
			Supplier<MethodInvocation> newMethodInvocationSupplier) {
		this(originalMethodInvocation);
		this.methodInvocationReplacementSupplier = newMethodInvocationSupplier;
	}

	JUnit4MethodInvocationReplacementData(MethodInvocation originalMethodInvocation, String staticMethodImport) {
		this(originalMethodInvocation);
		this.staticMethodImport = staticMethodImport;
	}

	JUnit4MethodInvocationReplacementData(MethodInvocation originalMethodInvocation) {
		this.originalMethodInvocation = originalMethodInvocation;
	}

	MethodInvocation getOriginalMethodInvocation() {
		return originalMethodInvocation;
	}

	Optional<MethodInvocation> createMethodInvocationReplacement() {
		if (methodInvocationReplacementSupplier != null) {
			return Optional.of(methodInvocationReplacementSupplier.get());
		}
		return Optional.empty();
	}

	Optional<String> getStaticMethodImport() {
		if (staticMethodImport != null) {
			return Optional.of(staticMethodImport);
		}
		return Optional.empty();
	}
}
