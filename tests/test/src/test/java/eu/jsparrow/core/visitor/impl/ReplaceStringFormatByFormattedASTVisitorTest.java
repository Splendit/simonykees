package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.rules.java16.ReplaceStringFormatByFormattedASTVisitor;

@SuppressWarnings("nls")
public class ReplaceStringFormatByFormattedASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setJavaVersion(JavaCore.VERSION_15);
		setVisitor(new ReplaceStringFormatByFormattedASTVisitor());
	}

	/**
	 * SIM-1998: this test will fail as soon as
	 * {@link ReplaceStringFormatByFormattedASTVisitor} is implemented.
	 * 
	 */
	@Test
	public void visit_StringFormatInvocation_notTransformingYet() throws Exception {
		String original = "" +
				"		String output = String.format(\n" +
				"				\"Name: %s, Phone: %s, Address: %s, Salary: $%.2f\",\n" +
				"				name, phone, address, salary);";
		assertNoChange(original);
	}

	// @Test
	// public void visit__shouldTransform() throws Exception {
	// String original = "" +
	// "";
	//
	// String expected = "" +
	// "";
	//
	// assertChange(original, expected);
	// }

	// @Test
	// public void visit_CastingOtherVariable_shouldNotTransform() throws
	// Exception {
	// String original = "" +
	// "";
	//
	// assertNoChange(original);
	// }
}