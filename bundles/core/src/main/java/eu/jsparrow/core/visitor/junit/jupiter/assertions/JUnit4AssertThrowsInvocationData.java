package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import org.eclipse.jdt.core.dom.Type;
import java.util.Optional;

class JUnit4AssertThrowsInvocationData {
	private final SupportedJUnit4InvocationData jUnitInvocationData;
	private Type typeOfThrowingRunnableToReplace;

	JUnit4AssertThrowsInvocationData(SupportedJUnit4InvocationData junitInvocationData,
			Type typeOfThrowingRunnableToReplace) {
		this(junitInvocationData);
		this.typeOfThrowingRunnableToReplace = typeOfThrowingRunnableToReplace;
	}

	JUnit4AssertThrowsInvocationData(SupportedJUnit4InvocationData jUnitInvocationData) {
		this.jUnitInvocationData = jUnitInvocationData;
	}

	SupportedJUnit4InvocationData getJUnit4InvocationData() {
		return jUnitInvocationData;
	}

	Optional<Type> getTypeOfThrowingRunnableToReplace() {
		return Optional.ofNullable(typeOfThrowingRunnableToReplace);
	}
}
