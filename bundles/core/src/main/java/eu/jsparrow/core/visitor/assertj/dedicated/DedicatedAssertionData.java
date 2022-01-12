package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Optional;

import org.eclipse.jdt.core.dom.SimpleType;

class DedicatedAssertionData {
	private final MethodInvocationData newAssertThatData;
	private MethodInvocationData newAssertionData;
	private SimpleType instanceofRightOperand;

	public DedicatedAssertionData(MethodInvocationData newAssertThatData, MethodInvocationData newAssertionData) {
		this.newAssertThatData = newAssertThatData;
		this.newAssertionData = newAssertionData;
	}

	public DedicatedAssertionData(MethodInvocationData newAssertThatData,
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
