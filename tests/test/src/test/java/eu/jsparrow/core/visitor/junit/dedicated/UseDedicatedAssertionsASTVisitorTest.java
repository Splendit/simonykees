package eu.jsparrow.core.visitor.junit.dedicated;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

class UseDedicatedAssertionsASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		addDependency("org.junit.jupiter", "junit-jupiter-api", "5.0.0");
		setVisitor(new UseDedicatedAssertionsASTVisitor());
	}

	public static Stream<Arguments> createComparingObjectsParameters() {
		return Stream.of(
				Arguments.of("assertTrue(a.equals(b))", "assertEquals(a,b)"),
				Arguments.of("assertTrue(!a.equals(b))", "assertNotEquals(a,b)"),
				Arguments.of("assertTrue((a.equals(b)))", "assertEquals(a,b)"),
				Arguments.of("assertTrue((!(!a.equals(b))))", "assertEquals(a,b)"),
				Arguments.of("assertFalse(a.equals(b))", "assertNotEquals(a,b)"),
				Arguments.of("assertFalse(!a.equals(b))", "assertEquals(a,b)"),
				Arguments.of("assertTrue(a == b)", "assertSame(a,b)"),
				Arguments.of("assertTrue(a != b)", "assertNotSame(a,b)"),
				Arguments.of("assertFalse(a == b)", "assertNotSame(a,b)"),
				Arguments.of("assertFalse(a != b)", "assertSame(a,b)"),
				Arguments.of("assertTrue(\"expected: a.equals(b)\", a.equals(b))",
						"assertEquals(\"expected: a.equals(b)\",a,b)"),
				Arguments.of("assertTrue(\"expected: a == b\", a == b)", "assertSame(\"expected: a == b\",a,b)"));
	}

	@ParameterizedTest()
	@MethodSource("createComparingObjectsParameters")
	void visit_AssertionsComparingObjects_shouldTransform(String originalInvocation,
			String expectedInvocation)
			throws Exception {
		fixture.addImport("org.junit.Assert.assertFalse", true, false);
		fixture.addImport("org.junit.Assert.assertTrue", true, false);
		String original = "" +
				"	Object a = new Object();\n" +
				"	Object b = a;\n" +
				"	" + originalInvocation + ";\n";
		String expected = "" +
				"	Object a = new Object();\n" +
				"	Object b = a;\n" +
				"	" + expectedInvocation + ";\n";

		assertChange(original, expected);
	}

	public static Stream<Arguments> createComparingObjectWithNullParameters() {
		return Stream.of(
				Arguments.of("assertTrue(object == null)", "assertNull(object)"),
				Arguments.of("assertTrue(null == object)", "assertNull(object)"),
				Arguments.of("assertTrue(null == null)", "assertNull(null)"),
				Arguments.of("assertFalse(object == null)", "assertNotNull(object)"),
				Arguments.of("assertFalse(null == object)", "assertNotNull(object)"),
				Arguments.of("assertFalse(null == null)", "assertNotNull(null)"),
				Arguments.of("assertTrue(\"object == null\",object == null)", "assertNull(\"object == null\",object)"));
	}

	@ParameterizedTest()
	@MethodSource("createComparingObjectWithNullParameters")
	void visit_AssertionsComparingObjectWithNull_shouldTransform(String originalInvocation, String expectedInvocation)
			throws Exception {

		fixture.addImport("org.junit.Assert.assertFalse", true, false);
		fixture.addImport("org.junit.Assert.assertTrue", true, false);
		String original = "" +
				"	Object object = new Object();\n" +
				"	" + originalInvocation + ";\n";
		String expected = "" +
				"	Object object = new Object();\n" +
				"	" + expectedInvocation + ";\n";

		assertChange(original, expected);
	}

	@Test
	void visit_JupiterAssertionWithMessage_shouldTransform() throws Exception {

		fixture.addImport("org.junit.jupiter.api.Assertions.assertTrue", true, false);

		String original = ""
				+ "		Object a = new Object();\n"
				+ "		Object b = a;\n"
				+ "		assertTrue(a == b, \"expected: a == b\");";

		String expected = ""
				+ "		Object a = new Object();\n"
				+ "		Object b = a;\n"
				+ "		assertSame(a,b,\"expected: a == b\");";

		assertChange(original, expected);
	}

	@Test
	void visit_JupiterAssertionWithoutMessage_shouldTransform() throws Exception {

		fixture.addImport("org.junit.jupiter.api.Assertions.assertTrue", true, false);

		String original = ""
				+ "		Object a = new Object();\n"
				+ "		Object b = a;\n"
				+ "		assertTrue(a == b);";

		String expected = ""
				+ "		Object a = new Object();\n"
				+ "		Object b = a;\n"
				+ "		assertSame(a,b);";

		assertChange(original, expected);
	}

	@Test
	void visit_AssertionLongInfixEqualsLong_shouldTransform() throws Exception {
		fixture.addImport("org.junit.Assert.assertTrue", true, false);

		String original = ""
				+ "		long longvalue = 100L;\n"
				+ "		assertTrue(longvalue == 100L);";

		String expected = ""
				+ "		long longvalue = 100L;\n"
				+ "		assertEquals(longvalue,100L);";

		assertChange(original, expected);
	}

	@Test
	void visit_AssertionLongInfixNotEqualsLong_shouldTransform() throws Exception {
		fixture.addImport("org.junit.Assert.assertTrue", true, false);

		String original = ""
				+ "		long longvalue = 100L;\n"
				+ "		assertTrue(longvalue != 100L);";

		String expected = ""
				+ "		long longvalue = 100L;\n"
				+ "		assertNotEquals(longvalue,100L);";

		assertChange(original, expected);
	}

	@Test
	void visit_AssertionStringEqualsString_shouldTransform() throws Exception {
		fixture.addImport("org.junit.Assert.assertTrue", true, false);

		String original = "assertTrue(\"x\".equals(\"x\"));";
		String expected = "assertEquals(\"x\",\"x\");";

		assertChange(original, expected);
	}

	@Test
	void visit_AssertTrueArrayEqualsArray_shouldTransform() throws Exception {
		fixture.addImport("org.junit.Assert.assertTrue", true, false);

		String original = ""
				+ "		Object[] array1 = new Object[] { new Object() };\n"
				+ "		Object[] array2 = array1;\n"
				+ "		assertTrue(array1.equals(array2));";

		String expected = ""
				+ "		Object[] array1 = new Object[] { new Object() };\n"
				+ "		Object[] array2 = array1;\n"
				+ "		assertSame(array1,array2);";

		assertChange(original, expected);
	}

	@Test
	void visit_AssertFalseArrayEqualsArray_shouldTransform() throws Exception {
		fixture.addImport("org.junit.Assert.assertFalse", true, false);

		String original = ""
				+ "		Object[] array1 = new Object[] { new Object() };\n"
				+ "		Object[] array2 = new Object[] { new Object() };\n"
				+ "		assertFalse(array1.equals(array2));";
		String expected = ""
				+ "		Object[] array1 = new Object[] { new Object() };\n"
				+ "		Object[] array2 = new Object[] { new Object() };\n"
				+ "		assertNotSame(array1,array2);";

		assertChange(original, expected);
	}
}
