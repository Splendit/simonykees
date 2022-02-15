package eu.jsparrow.core.visitor.assertj.dedicated;

import org.eclipse.jdt.core.dom.Expression;
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
public class BooleanAssertionWithInstanceofAnalysisResult {
	private final Expression instanceOfLeftOperand;
	private final SimpleType instanceofRightOperand;

	public BooleanAssertionWithInstanceofAnalysisResult(Expression instanceOfLeftOperand,
			SimpleType instanceofRightOperand) {
		this.instanceOfLeftOperand = instanceOfLeftOperand;
		this.instanceofRightOperand = instanceofRightOperand;
	}

	public Expression getInstanceOfLeftOperand() {
		return instanceOfLeftOperand;
	}

	public SimpleType getInstanceofRightOperand() {
		return instanceofRightOperand;
	}
}
