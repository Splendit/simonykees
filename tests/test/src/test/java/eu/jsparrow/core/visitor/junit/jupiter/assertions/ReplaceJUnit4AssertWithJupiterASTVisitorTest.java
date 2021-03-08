package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.junit.jupiter.AbstractReplaceJUnit4AnnotationsWithJupiterASTVisitorTest;

public class ReplaceJUnit4AssertWithJupiterASTVisitorTest
		extends AbstractReplaceJUnit4AnnotationsWithJupiterASTVisitorTest {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		addDependency("org.junit.jupiter", "junit-jupiter-api", "5.4.0");
		setDefaultVisitor(new ReplaceJUnit4AssertWithJupiterASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_qualifiedAssertEqualsForObjectArray_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Assert.class.getName());
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		Assert.assertEquals(new Object[] {}, new Object[] {});\n" +
				"	}";
		String expected = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		assertArrayEquals(new Object[]{},new Object[]{});\n" +
				"	}";

		List<String> expectedImports = Arrays.asList("import org.junit.Assert;", "import org.junit.jupiter.api.Test;",
				"import static org.junit.jupiter.api.Assertions.assertArrayEquals;");
		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_qualifiedAssertEqualsWithObjectArrayAndMessage_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Assert.class.getName());
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		Assert.assertEquals(\"Both arrays should be equal!\", new Object[] {}, new Object[] {});\n" +
				"	}";
		String expected = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		assertArrayEquals(new Object[]{},new Object[]{},() -> \"Both arrays should be equal!\");\n" +
				"	}";

		List<String> expectedImports = Arrays.asList("import org.junit.Assert;", "import org.junit.jupiter.api.Test;",
				"import static org.junit.jupiter.api.Assertions.assertArrayEquals;");
		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_AssertEqualsForObjectArrayWithoutQualifier_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertEquals", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	@Test\n"
				+ "	void test() {\n"
				+ "		assertEquals(new Object[] {}, new Object[] {});\n"
				+ "	}";
		String expected = "" +
				"	@Test\n"
				+ "	void test() {\n"
				+ "		assertArrayEquals(new Object[] {}, new Object[] {});\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.junit.jupiter.api.Assertions.assertArrayEquals;");
		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_twoStaticAssertMethodImports_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertEquals", true, false);
		defaultFixture.addImport("org.junit.Assert.assertArrayEquals", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	@Test\n"
				+ "	void test() {\n"
				+ "		assertEquals(new Object[] {}, new Object[] {});\n"
				+ "	}";
		String expected = "" +
				"	@Test\n"
				+ "	void test() {\n"
				+ "		assertArrayEquals(new Object[] {}, new Object[] {});\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.junit.jupiter.api.Assertions.assertArrayEquals;");
		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_withoutChangingAssertEqualsInvocation_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertEquals", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"@Test\n"
				+ "	void test() {\n"
				+ "		assertEquals(10L, 10L);\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.junit.jupiter.api.Assertions.assertEquals;");
		assertChange(original, original, expectedImports);
	}

	@Test
	public void visit_assertEqualsInvocationWithMessage_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertEquals", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	@Test\n"
				+ "	void test() {\n"
				+ "		assertEquals(\"10L should be equal to 10L\", 10L, 10L);\n"
				+ "	}";

		String expected = "" +
				"	@Test\n"
				+ "	void test() {\n"
				+ "		assertEquals(10L, 10L, () -> \"10L should be equal to 10L\");\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.junit.jupiter.api.Assertions.assertEquals;");
		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_failWithoutQualifierAndWithoutMessage_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.fail", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	@Test\n"
				+ "	void test() {\n"
				+ "		fail();\n"
				+ "	}";
		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.junit.jupiter.api.Assertions.fail;");
		assertChange(original, original, expectedImports);
	}

	@Test
	public void visit_assertEqualsNewStaticImportNotPossible_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Assert.class.getName());
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());

		String localClassWithAssertEquals = ""
				+ "	Object assertEqualsInAnonymousClass() {\n"
				+ "		return new Object (){\n"
				+ "			void assertEquals(String s1, String s2) {\n"
				+ "				\n"
				+ "			}\n"
				+ "		};\n"
				+ "	}\n";

		String original = localClassWithAssertEquals +
				"	@Test\n" +
				"	void test() {\n" +
				"		Assert.assertEquals(10L, 10L);\n" +
				"	}";

		String expected = localClassWithAssertEquals +
				"	@Test\n" +
				"	void test() {\n" +
				"		Assertions.assertEquals(10L, 10L);\n" +
				"	}";

		List<String> expectedImports = Arrays.asList("import org.junit.Assert;",
				"import org.junit.jupiter.api.Assertions;",
				"import org.junit.jupiter.api.Test;");
		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_assertEqualsNotAnyNewImportPossible_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Assert.class.getName());
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());

		String original = "" +
				"	@Test\n"
				+ "	void test() {\n"
				+ "		class Assertions {\n"
				+ "			void assertEquals(String s1, String s2) {\n"
				+ "\n"
				+ "			}\n"
				+ "		}\n"
				+ "		Assert.assertEquals(10L, 10L);\n"
				+ "	}";

		String expected = "" +
				"	@Test\n"
				+ "	void test() {\n"
				+ "		class Assertions {\n"
				+ "			void assertEquals(String s1, String s2) {\n"
				+ "\n"
				+ "			}\n"
				+ "		}\n"
				+ "		org.junit.jupiter.api.Assertions.assertEquals(10L, 10L);\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.Assert;",
				"import org.junit.jupiter.api.Test;");
		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_assertEqualsNewStaticImportByReplacement_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertEquals", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());

		String original = "" +
				"	@Test\n"
				+ "	void test() {\n"
				+ "		class Assertions {\n"
				+ "			void assertEquals(String s1, String s2) {\n"
				+ "\n"
				+ "			}\n"
				+ "		}\n"
				+ "		assertEquals(10L, 10L);\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.junit.jupiter.api.Assertions.assertEquals;");
		assertChange(original, original, expectedImports);
	}

	@Test
	public void visit_CannotReplaceAssertEqualsStaticImport_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertEquals", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());

		String original = "" +
				"	void methodWithoutTestAnnotation() {\n"
				+ "		assertEquals(10L, 10L);\n"
				+ "	}\n"
				+ "	@Test\n"
				+ "	void test() {\n"
				+ "		assertEquals(10L, 10L);\n"
				+ "	}";

		String expected = "" +
				"	void methodWithoutTestAnnotation() {\n"
				+ "		assertEquals(10L, 10L);\n"
				+ "	}\n"
				+ "	@Test\n"
				+ "	void test() {\n"
				+ "		Assertions.assertEquals(10L, 10L);\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Assertions;",
				"import org.junit.jupiter.api.Test;",
				"import static org.junit.Assert.assertEquals;");
		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_StaticAssertImportOnDemand_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert", true, true);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());

		String original = "" +
				"	@Test\n"
				+ "	void test() {\n"
				+ "		assertEquals(10L, 10L);\n"
				+ "	}";

		String expected = "" +
				"	@Test\n"
				+ "	void test() {\n"
				+ "		Assertions.assertEquals(10L, 10L);\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Assertions;",
				"import org.junit.jupiter.api.Test;",
				"import static org.junit.Assert.*;");
		assertChange(original, expected, expectedImports);
	}
	
	

}
