package eu.jsparrow.core.visitor.assertj.dedicated;

import org.eclipse.jdt.core.dom.SimpleType;

/**
 * Stores all informations needed for the construction of a new AssertJ
 * assertThat invocation combined with an {@code isInstanceOf} assertion, for
 * example
 * 
 * <pre>
 * assertThat(string).isInstanceOf(String.class);
 * </pre>
 * 
 * @since 4.7.0
 */
public class AssertThatInstanceOfAnalysisData0 {
	private final MethodInvocationData newAssertThatData;
	private final SimpleType instanceofRightOperand;

	public AssertThatInstanceOfAnalysisData0(MethodInvocationData newAssertThatData, SimpleType instanceofRightOperand) {
		this.newAssertThatData = newAssertThatData;
		this.instanceofRightOperand = instanceofRightOperand;
	}

	public MethodInvocationData getNewAssertThatData() {
		return newAssertThatData;
	}

	public SimpleType getInstanceofRightOperand() {
		return instanceofRightOperand;
	}
}
