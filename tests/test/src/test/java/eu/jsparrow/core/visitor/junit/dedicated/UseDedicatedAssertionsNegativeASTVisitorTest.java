package eu.jsparrow.core.visitor.junit.dedicated;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

class UseDedicatedAssertionsNegativeASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		addDependency("org.junit.jupiter", "junit-jupiter-api", "5.0.0");
		setVisitor(new UseDedicatedAssertionsASTVisitor());
	}

	@Test
	void visit_AssertionPrimitiveFloatEqualsPrimitiveInt_shouldNotTransform() throws Exception {
		fixture.addImport("org.junit.Assert.assertTrue", true, false);

		String original = ""
				+ "		float floatValue = 100.0F;\n"
				+ "		assertTrue(floatValue == 100);";

		assertNoChange(original);
	}

	@Test
	void visit_AssertionPrimitiveDoubleEqualsPrimitiveInt_shouldNotTransform() throws Exception {
		fixture.addImport("org.junit.Assert.assertTrue", true, false);

		String original = ""
				+ "		double doubleValue = 100.0;\n"
				+ "		assertTrue(doubleValue == 100);";

		assertNoChange(original);
	}
}