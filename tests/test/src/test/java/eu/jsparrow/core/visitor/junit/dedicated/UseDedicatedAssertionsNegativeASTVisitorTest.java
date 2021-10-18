package eu.jsparrow.core.visitor.junit.dedicated;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

class UseDedicatedAssertionsNegativeASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		setVisitor(new UseDedicatedAssertionsASTVisitor());
	}

	@Test
	void visit_NotResolvedMethod_shouldNotTransform() throws Exception {

		String original = ""
				+ "		Object a = new Object();\n"
				+ "		Object b = a;\n"
				+ "		assertTrue(a == b);";

		assertNoChange(original);
	}

	@Test
	void visit_AssertTrueNotDeclaredInJUnit_shouldNotTransform() throws Exception {

		fixture.addMethod("assertTrue");

		String original = "assertTrue();";

		assertNoChange(original);
	}

	@Test
	void visit_NotSupportedAssertionMethodName_shouldNotTransform() throws Exception {
		fixture.addImport("org.junit.Assert.assertEquals", true, false);

		String original = ""
				+ "		Object a = new Object();\n"
				+ "		Object b = a;\n"
				+ "		assertEquals(a,b);";

		assertNoChange(original);
	}

	@Test
	void visit_AssertTrueOnBooleanVariable_shouldNotTransform() throws Exception {
		fixture.addImport("org.junit.Assert.assertTrue", true, false);

		String original = ""
				+ "		boolean condition = true;\n"
				+ "		assertTrue(condition);";

		assertNoChange(original);
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

	@Test
	void visit_AssertionWithEqualsWithoutExpression_shouldNotTransform() throws Exception {
		fixture.addImport("org.junit.Assert.assertTrue", true, false);

		String original = ""
				+ "		class LocalClass {\n"
				+ "			public boolean equals(Object o) {\n"
				+ "				return false;\n"
				+ "			}\n"
				+ "\n"
				+ "			void test() {\n"
				+ "				Object o = new Object();\n"
				+ "				assertTrue(equals(o));\n"
				+ "			}\n"
				+ "		}";

		assertNoChange(original);
	}

	@Test
	void visit_AssertionWithEqualsWithoutArgument_shouldNotTransform() throws Exception {
		fixture.addImport("org.junit.Assert.assertTrue", true, false);

		String original = ""
				+ "		class LocalClass {\n"
				+ "			public boolean equals() {\n"
				+ "				return false;\n"
				+ "			}\n"
				+ "\n"
				+ "			void test() {\n"
				+ "				assertTrue(this.equals());\n"
				+ "			}\n"
				+ "		}";

		assertNoChange(original);
	}

	@Test
	void visit_AssertionWithNotSupportedMethod_shouldNotTransform() throws Exception {
		fixture.addImport("org.junit.Assert.assertTrue", true, false);

		String original = ""
				+ "		class LocalClass {\n"
				+ "			public boolean isEqual(Object o) {\n"
				+ "				return false;\n"
				+ "			}\n"
				+ "\n"
				+ "			void test() {\n"
				+ "				Object o = new Object();\n"
				+ "				assertTrue(this.isEqual(o));\n"
				+ "			}\n"
				+ "		}";

		assertNoChange(original);
	}

	@Test
	void visit_AssertionWithInfixAnd_shouldNotTransform() throws Exception {
		fixture.addImport("org.junit.Assert.assertTrue", true, false);

		String original = ""
				+ "		boolean condition1 = true;\n"
				+ "		boolean condition2 = true;\n"
				+ "		assertTrue(condition1 && condition2);";

		assertNoChange(original);
	}

	@Test
	void visit_IntegerObjectInfixEqualsPrimitiveInt_shouldNotTransform() throws Exception {
		fixture.addImport("org.junit.Assert.assertTrue", true, false);

		String original = ""
				+ "		Integer integerZero = Integer.valueOf(0);\n"
				+ "		int intZero = 0;\n"
				+ "		assertTrue(integerZero == intZero);";

		assertNoChange(original);
	}

	@Test
	void visit_UnresolvedMethodInvocationAsAssertionArgument_shouldNotTransform() throws Exception {
		fixture.addImport("org.junit.Assert.assertTrue", true, false);

		String original = ""
				+ "		Object a = new Object();\n"
				+ "		Object b = a;\n"
				+ "		assertTrue(checkEquality(a,b));";

		assertNoChange(original);
	}
	
	@Test
	void visit_infixComparingUnboxedWithPrimitive_shouldNotTransform() throws Exception {
		fixture.addImport("org.junit.Assert.assertTrue", true, false);

		String original = ""
				+ "Integer i = 10;\n"
				+ "assertTrue(i == 10);";

		assertNoChange(original);
	}
	
	@Test
	void visit_equalsComparingUnboxedWithPrimitive_shouldNotTransform() throws Exception {
		fixture.addImport("org.junit.Assert.assertTrue", true, false);

		String original = ""
				+ "Integer i = 10;\n"
				+ "assertTrue(i.equals(10));";

		assertNoChange(original);
	}
}