package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;

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
	private AssumeNotNullWithNullableArray assumptionThatEveryItemNotNull;
	private Type typeOfThrowingRunnableToReplace;

	JUnit4InvocationReplacementData(JUnit4InvocationReplacementAnalysis jUnit4InvocationAnalysisResult,
			Supplier<MethodInvocation> newMethodInvocationSupplier) {
		this(jUnit4InvocationAnalysisResult);
		this.methodInvocationReplacementSupplier = newMethodInvocationSupplier;
	}

	JUnit4InvocationReplacementData(JUnit4InvocationReplacementAnalysis jUnit4InvocationAnalysisResult) {
		this.originalMethodInvocation = jUnit4InvocationAnalysisResult.getMethodInvocation();
		this.assumptionThatEveryItemNotNull = jUnit4InvocationAnalysisResult.getAssumeNotNullWithNullableArray()
			.orElse(null);
		this.typeOfThrowingRunnableToReplace = jUnit4InvocationAnalysisResult.getTypeOfThrowingRunnableToReplace()
			.orElse(null);
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

	Optional<AssumeNotNullWithNullableArray> getAssumptionThatEveryItemNotNull() {
		return Optional.ofNullable(assumptionThatEveryItemNotNull);
	}

	Optional<Type> getTypeOfThrowingRunnableToReplace() {
		return Optional.ofNullable(typeOfThrowingRunnableToReplace);
	}
}
