package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MigrateJUnit4ToJupiterASTVisitorTest extends AbstractMigrateJUnit4ToJupiterASTVisitorTest {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		setDefaultVisitor(new MigrateJUnit4ToJupiterASTVisitor());
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

}
