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
class JUnit4InvocationReplacementData {
	private final MethodInvocation originalMethodInvocation;
	private Supplier<MethodInvocation> methodInvocationReplacementSupplier;

	JUnit4InvocationReplacementData(JUnit4InvocationReplacementAnalysis jUnit4InvocationAnalysisResult,
			Supplier<MethodInvocation> newMethodInvocationSupplier) {
		this(jUnit4InvocationAnalysisResult);
		this.methodInvocationReplacementSupplier = newMethodInvocationSupplier;
	}

	JUnit4InvocationReplacementData(JUnit4InvocationReplacementAnalysis jUnit4InvocationAnalysisResult) {
		this.originalMethodInvocation = jUnit4InvocationAnalysisResult.getMethodInvocation();
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
}
