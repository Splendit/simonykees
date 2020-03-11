package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
public class RemoveToStringOnStringASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		visitor = new RemoveToStringOnStringASTVisitor();
	}

	@Test
	public void visit_parenthesisedMethodExpression_shouldTransformButNotUnwrap() throws Exception {
		String original = "System.out.println((\"abc\".toString() + System.getProperty(\"line.separator\", \"\\n\")).toString().hashCode());";
		String expected = "System.out.println((\"abc\" + System.getProperty(\"line.separator\", \"\\n\")).hashCode());";

		assertChange(original, expected);
	}
}
