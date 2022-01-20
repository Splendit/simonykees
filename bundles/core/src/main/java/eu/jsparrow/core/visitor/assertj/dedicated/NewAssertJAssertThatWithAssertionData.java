package eu.jsparrow.core.visitor.assertj.dedicated;

/**
 * Stores all informations needed for the construction of a new AssertJ
 * assertThat invocation combined with an assertion, for example
 * 
 * <pre>
 * assertThat(string).isEqualTo("Hello World!");
 * </pre>
 * 
 * @since 4.7.0
 * 
 */
class NewAssertJAssertThatWithAssertionData {
	private final MethodInvocationData newAssertThatData;
	private final MethodInvocationData newAssertionData;

	public NewAssertJAssertThatWithAssertionData(MethodInvocationData newAssertThatData,
			MethodInvocationData newAssertionData) {
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
