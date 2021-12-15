package eu.jsparrow.core.visitor.assertj.dedicated;

class DedicatedAssertionData {
	private final MethodInvocationData newAssertThatData;
	private final MethodInvocationData newAssertionData;

	public DedicatedAssertionData(MethodInvocationData newAssertThatData, MethodInvocationData newAssertionData) {
		this.newAssertThatData = newAssertThatData;
		this.newAssertionData = newAssertionData;
	}

	public MethodInvocationData getNewAssertThatData() {
		return newAssertThatData;
	}

	public MethodInvocationData getNewAssertionData() {
		return newAssertionData;
	}

}
