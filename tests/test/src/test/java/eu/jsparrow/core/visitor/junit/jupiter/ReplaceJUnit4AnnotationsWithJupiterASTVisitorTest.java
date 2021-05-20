package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReplaceJUnit4AnnotationsWithJupiterASTVisitorTest extends AbstractReplaceJUnit4WithJupiterASTVisitorTest {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		setDefaultVisitor(new ReplaceJUnit4AnnotationsWithJupiterASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_MarkerAnnotationTest_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Test.class.getName());

		String original = "" +
				"	@Test\n" +
				"	public void test() {\n" +
				"	}";
		assertChange(original, original, Arrays.asList("import org.junit.jupiter.api.Test;"));
	}

	@Test
	public void visit_MarkerAnnotationIgnore_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Ignore.class.getName());

		String original = "" +
				"	@Ignore\n" +
				"	public void test() {\n" +
				"	}";

		String expected = "" +
				"	@Disabled\n" +
				"	public void test() {\n" +
				"	}";

		assertChange(original, expected, Arrays.asList("import org.junit.jupiter.api.Disabled;"));
	}

	@Test
	public void visit_SingleMemberAnnotationIgnore_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Ignore.class.getName());

		String original = "" +
				"	@Ignore(\"Test is not carried out.\")\n" +
				"	public void test() {\n" +
				"	}";

		String expected = "" +
				"	@Disabled(\"Test is not carried out.\")\n" +
				"	public void test() {\n" +
				"	}";
		assertChange(original, expected, Arrays.asList("import org.junit.jupiter.api.Disabled;"));

	}

	@Test
	public void visit_AllSupportedAnnotations_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.After.class.getName());
		defaultFixture.addImport(org.junit.AfterClass.class.getName());
		defaultFixture.addImport(org.junit.Before.class.getName());
		defaultFixture.addImport(org.junit.BeforeClass.class.getName());
		defaultFixture.addImport(org.junit.Ignore.class.getName());
		defaultFixture.addImport(org.junit.Test.class.getName());

		List<String> importsToStringExpected = Arrays.asList(
				"import " + org.junit.jupiter.api.AfterEach.class.getName() + ";",
				"import " + org.junit.jupiter.api.AfterAll.class.getName() + ";",
				"import " + org.junit.jupiter.api.BeforeEach.class.getName() + ";",
				"import " + org.junit.jupiter.api.BeforeAll.class.getName() + ";",
				"import " + org.junit.jupiter.api.Disabled.class.getName() + ";",
				"import " + org.junit.jupiter.api.Test.class.getName() + ";");

		String original = "" +
				"class TestStub {\n"
				+ "\n"
				+ "	@Before\n"
				+ "	public void beforeEach() throws Exception {\n"
				+ "	}\n"
				+ "\n"
				+ "	@After\n"
				+ "	public void afterEach() throws Exception {\n"
				+ "	}\n"
				+ "\n"
				+ "	@BeforeClass\n"
				+ "	public void beforeAll() throws Exception {\n"
				+ "	}\n"
				+ "\n"
				+ "	@AfterClass\n"
				+ "	public void afterAll() throws Exception {\n"
				+ "	}\n"
				+ "\n"
				+ "	@Test\n"
				+ "	public void testWithTestMarkerAnnotation() throws Exception {\n"
				+ "	}\n"
				+ "\n"
				+ "	@Test()\n"
				+ "	public void testWithTestNormalAnnotation() throws Exception {\n"
				+ "	}\n"
				+ "\n"
				+ "	@Ignore\n"
				+ "	@Test\n"
				+ "	public void testWithIgnoreMarkerAnnotation() throws Exception {\n"
				+ "	}\n"
				+ "\n"
				+ "	@Ignore(value = \"This test is ignored\")\n"
				+ "	@Test\n"
				+ "	public void testWithIgnoreNormalAnnotation() throws Exception {\n"
				+ "	}\n"
				+ "\n"
				+ "	@Ignore(\"This test is ignored\")\n"
				+ "	@Test\n"
				+ "	public void testWithIgnoreSingleMemberAnnotation() throws Exception {\n"
				+ "	}\n"
				+ "}";

		String expected = "" +
				"class TestStub {\n"
				+ "\n"
				+ "	@BeforeEach\n"
				+ "	public void beforeEach() throws Exception {\n"
				+ "	}\n"
				+ "\n"
				+ "	@AfterEach\n"
				+ "	public void afterEach() throws Exception {\n"
				+ "	}\n"
				+ "\n"
				+ "	@BeforeAll\n"
				+ "	public void beforeAll() throws Exception {\n"
				+ "	}\n"
				+ "\n"
				+ "	@AfterAll\n"
				+ "	public void afterAll() throws Exception {\n"
				+ "	}\n"
				+ "\n"
				+ "	@Test\n"
				+ "	public void testWithTestMarkerAnnotation() throws Exception {\n"
				+ "	}\n"
				+ "\n"
				+ "	@Test()\n"
				+ "	public void testWithTestNormalAnnotation() throws Exception {\n"
				+ "	}\n"
				+ "\n"
				+ "	@Disabled\n"
				+ "	@Test\n"
				+ "	public void testWithIgnoreMarkerAnnotation() throws Exception {\n"
				+ "	}\n"
				+ "\n"
				+ "	@Disabled(value = \"This test is ignored\")\n"
				+ "	@Test\n"
				+ "	public void testWithIgnoreNormalAnnotation() throws Exception {\n"
				+ "	}\n"
				+ "\n"
				+ "	@Disabled(\"This test is ignored\")\n"
				+ "	@Test\n"
				+ "	public void testWithIgnoreSingleMemberAnnotation() throws Exception {\n"
				+ "	}\n"
				+ "}";

		assertChange(original, expected, importsToStringExpected);
	}

	@Test
	public void visit_JUnit4AssertEqualsStaticImport_shouldTansform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertEquals", true, false);
		defaultFixture.addImport(org.junit.BeforeClass.class.getName());

		String original = "" +
				"	@BeforeClass\n" +
				"	public void beforeAll() {\n" +
				"		assertEquals(\"1\", \"1\");" +
				"	}";

		String expected = "" +
				"	@BeforeAll\n" +
				"	public void beforeAll() {\n" +
				"		assertEquals(\"1\", \"1\");\n" +
				"	}";
		List<String> importsToStringExpected = Arrays.asList(
				"import static org.junit.Assert.assertEquals;",
				"import org.junit.jupiter.api.BeforeAll;");

		assertChange(original, expected, importsToStringExpected);
	}

	@Test
	public void visit_JUnit4AssertImportedExplicitly_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Assert.class.getName());
		defaultFixture.addImport(org.junit.BeforeClass.class.getName());

		String original = "" +
				"	@BeforeClass\n" +
				"	public void beforeAll() {\n" +
				"		Assert.assertEquals(\"1\", \"1\");" +
				"	}";

		String expected = "" +
				"	@BeforeAll\n" +
				"	public void beforeAll() {\n" +
				"		Assert.assertEquals(\"1\", \"1\");\n" +
				"	}";

		List<String> importsToStringExpected = Arrays.asList(
				"import org.junit.Assert;",
				"import org.junit.jupiter.api.BeforeAll;");

		assertChange(original, expected, importsToStringExpected);
	}

	@Test
	public void visit_JUnit4AssertEqualsStaticImportOnDemand_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert", true, true);
		defaultFixture.addImport(org.junit.BeforeClass.class.getName());

		String original = "" +
				"	@BeforeClass\n" +
				"	public void beforeAll() throws Exception {\n" +
				"		assertEquals(\"1\", \"1\");\n" +
				"	}";

		String expected = "" +
				"	@BeforeAll\n" +
				"	public void beforeAll() throws Exception {\n" +
				"		assertEquals(\"1\", \"1\");\n" +
				"	}";

		List<String> importsToStringExpected = Arrays.asList(
				"import static org.junit.Assert.*;",
				"import org.junit.jupiter.api.BeforeAll;");

		assertChange(original, expected, importsToStringExpected);
	}

	@Test
	public void visit_JUnit4AssertAndBeforeClassImportedOnDemand_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit", false, true);

		String original = "" +
				"	@BeforeClass\n" +
				"	public void beforeAll() {\n" +
				"		Assert.assertEquals(\"1\", \"1\");" +
				"	}";

		String expected = "" +
				"	@BeforeAll\n" +
				"	public void beforeAll() {\n" +
				"		Assert.assertEquals(\"1\", \"1\");" +
				"	}";

		List<String> importsToStringExpected = Arrays.asList(
				"import org.junit.*;",
				"import org.junit.jupiter.api.BeforeAll;");

		assertChange(original, expected, importsToStringExpected);
	}

	@Test
	public void visit_IgnoreTestWithAssertClassToString_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Assert.class.getName());
		defaultFixture.addImport(org.junit.Ignore.class.getName());

		String original = "" +
				"		@Ignore\n" +
				"		void test() {\n" +
				"			Assert.class.toString();\n" +
				"		}";

		String expected = "" +
				"		@Disabled\n" +
				"		void test() {\n" +
				"			Assert.class.toString();\n" +
				"		}";

		List<String> importsToStringExpected = Arrays.asList("import org.junit.Assert;",
				"import org.junit.jupiter.api.Disabled;");
		assertChange(original, expected, importsToStringExpected);

	}

	@Test
	public void visit_IgnoreTestWithAssertAsVariable_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Assert.class.getName());
		defaultFixture.addImport(org.junit.Ignore.class.getName());

		String original = "" +
				"		@Ignore\n" +
				"		void test() {\n" +
				"			Assert xAssert;\n" +
				"		}";

		String expected = "" +
				"		@Disabled\n" +
				"		void test() {\n" +
				"			Assert xAssert;\n" +
				"		}";

		List<String> importsToStringExpected = Arrays.asList("import org.junit.Assert;",
				"import org.junit.jupiter.api.Disabled;");
		assertChange(original, expected, importsToStringExpected);
	}

	@Test
	public void visit_IgnoreImportedOnDemand_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit", false, true);

		String original = "" +
				"	@Ignore\n" +
				"	public void test() {\n" +
				"	}";

		String expected = "" +
				"	@Disabled\n" +
				"	public void test() {\n" +
				"	}";

		List<String> importsToStringExpected = Arrays.asList(
				"import org.junit.*;",
				"import org.junit.jupiter.api.Disabled;");

		assertChange(original, expected, importsToStringExpected);
	}

	/*
	 * SIM-1873: expected to fail as soon qualified annotation names will be
	 * supported
	 */
	@Test
	public void visit_JUnit4TestAnnotationNotSimpleTypeName_shouldNotTransform() throws Exception {

		String original = "" +
				"\n" +
				"	@org.junit.BeforeClass\n" +
				"	public void beforeAll() {\n" +
				"	}";

		String expected = "" +
				"\n" +
				"	@BeforeAll\n" +
				"	public void beforeAll() {\n" +
				"	}";

		List<String> importsToStringExpected = Arrays.asList(
				"import org.junit.jupiter.api.BeforeAll;");
		assertChange(original, expected, importsToStringExpected);
	}

	@Test
	public void visit_ClassWithNameDisabledAndIgnoreAnnotation_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Ignore.class.getName());
		String original = "" +
				"	@Ignore\n" +
				"	class Disabled {" +
				"	}";

		String expected = "" +
				"	@org.junit.jupiter.api.Disabled\n" +
				"	class Disabled {" +
				"	}";

		List<String> importsToStringExpected = Arrays.asList();
		assertChange(original, expected, importsToStringExpected);
	}

	@Test
	public void visit_LabeledLoopWithBreakLOOP_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Ignore.class.getName());
		String original = "" +
				"	@Ignore\n" +
				"	void test() {\n" +
				"		LOOP: for (int i = 0; i < 10; i++) {\n" +
				"			break LOOP;\n" +
				"		}\n" +
				"	}";

		String expected = "" +
				"	@Disabled\n" +
				"	void test() {\n" +
				"		LOOP: for (int i = 0; i < 10; i++) {\n" +
				"			break LOOP;\n" +
				"		}\n" +
				"	}";

		List<String> importsToStringExpected = Arrays.asList("import org.junit.jupiter.api.Disabled;");
		assertChange(original, expected, importsToStringExpected);
	}

	@Test
	public void visit_LabeledLoopWithContinueLOOP_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Ignore.class.getName());
		String original = "" +
				"	@Ignore\n" +
				"	void test() {\n" +
				"		LOOP: for (int i = 0; i < 10; i++) {\n" +
				"			continue LOOP;\n" +
				"		}\n" +
				"	}";

		String expected = "" +
				"	@Disabled\n" +
				"	void test() {\n" +
				"		LOOP: for (int i = 0; i < 10; i++) {\n" +
				"			continue LOOP;\n" +
				"		}\n" +
				"	}";

		List<String> importsToStringExpected = Arrays.asList("import org.junit.jupiter.api.Disabled;");
		assertChange(original, expected, importsToStringExpected);

	}

	@Test
	public void visit_AssumeTrueInTestMethod_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Test.class.getName());
		defaultFixture.addImport("org.junit.Assume.assumeTrue", true, false);
		String original = "" +
				"	@Test\n" +
				"	public void test() {\n" +
				"		assumeTrue(true);\n" +
				"	}";
		assertChange(original, original, Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.junit.Assume.assumeTrue;"));
	}
}
