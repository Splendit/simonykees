package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import org.eclipse.jdt.core.dom.Type;
import java.util.Optional;

class JUnit4AssertThrowsInvocationAnalysisResult {
	private final JUnit4MethodInvocationAnalysisResult jUnitInvocationData;
	private Type typeOfThrowingRunnableToReplace;

	JUnit4AssertThrowsInvocationAnalysisResult(JUnit4MethodInvocationAnalysisResult junitInvocationData,
			Type typeOfThrowingRunnableToReplace) {
		this(junitInvocationData);
		this.typeOfThrowingRunnableToReplace = typeOfThrowingRunnableToReplace;
	}

	JUnit4AssertThrowsInvocationAnalysisResult(JUnit4MethodInvocationAnalysisResult jUnitInvocationData) {
		this.jUnitInvocationData = jUnitInvocationData;
	}

	JUnit4MethodInvocationAnalysisResult getJUnit4InvocationData() {
		return jUnitInvocationData;
	}

	Optional<Type> getTypeOfThrowingRunnableToReplace() {
		return Optional.ofNullable(typeOfThrowingRunnableToReplace);
	}
}
