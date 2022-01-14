package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Optional;

import org.eclipse.jdt.core.dom.SimpleType;

/**
 * Stores all informations needed for the construction of a new AssertJ
 * assertThat invocation combined with an assertion, for example
 * 
 * <pre>
 * assertThat(string).isEqualTo("Hello World!");
 * </pre>
 * 
 *
 */
class NewAssertJAssertThatWithAssertionData {
	private final MethodInvocationData newAssertThatData;
	private MethodInvocationData newAssertionData;
	private SimpleType instanceofRightOperand;

	public NewAssertJAssertThatWithAssertionData(MethodInvocationData newAssertThatData,
			MethodInvocationData newAssertionData) {
		this.newAssertThatData = newAssertThatData;
		this.newAssertionData = newAssertionData;
	}

	public NewAssertJAssertThatWithAssertionData(MethodInvocationData newAssertThatData,
			SimpleType instanceofRightOperand) {
		this.newAssertThatData = newAssertThatData;
		this.instanceofRightOperand = instanceofRightOperand;
	}

	public MethodInvocationData getNewAssertThatData() {
		return newAssertThatData;
	}

	public Optional<MethodInvocationData> getNewAssertionData() {
		return Optional.ofNullable(newAssertionData);
	}

	public Optional<SimpleType> getInstanceofRightOperand() {
		return Optional.ofNullable(instanceofRightOperand);
	}
}
