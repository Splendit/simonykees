package eu.jsparrow.core.visitor.junit.jupiter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

class ReplaceJUnit4CategoryWithJupiterTagASTVisitorTest extends UsesJDTUnitFixture {

	private static final String CATEGORY_CLASS_DECLARATIONS = "" +
			"\n" +
			"\n" +
			"	class FirstCategory {\n" +
			"	}\n" +
			"\n" +
			"	class SecondCategory {\n" +
			"	}";

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		setDefaultVisitor(new ReplaceJUnit4CategoryWithJupiterTagASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_SingleMemberClassLiteral_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Test.class.getName());
		defaultFixture.addImport(org.junit.experimental.categories.Category.class.getName());

		String original = "" +
				"	@Category(FirstCategory.class)\n" +
				"	@Test\n" +
				"	void test() {\n" +
				"	}" +
				CATEGORY_CLASS_DECLARATIONS;

		String expected = "" +
				"	@Tag(\"fixturepackage.TestCU.FirstCategory\")\n" +
				"	@Test\n" +
				"	void test() {\n" +
				"	}" +
				CATEGORY_CLASS_DECLARATIONS;

		assertChange(original, expected);
	}

	@Test
	public void visit_SingleMemberClassLiteralArray_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Test.class.getName());
		defaultFixture.addImport(org.junit.experimental.categories.Category.class.getName());

		String original = "" +
				"	@Category({ FirstCategory.class, SecondCategory.class })\n"
				+ "	@Test\n"
				+ "	void test() {\n"
				+ "	}" +
				CATEGORY_CLASS_DECLARATIONS;

		String expected = "" +
				"	@Tag(\"fixturepackage.TestCU.FirstCategory\")"
				+ " @Tag(\"fixturepackage.TestCU.SecondCategory\")\n"
				+ "	@Test\n"
				+ "	void test() {\n"
				+ "	}" +
				CATEGORY_CLASS_DECLARATIONS;

		assertChange(original, expected);
	}

	@Test
	public void visit_SingleMemberEmptyArray_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Test.class.getName());
		defaultFixture.addImport(org.junit.experimental.categories.Category.class.getName());

		String original = ""
				+ "	@Category({})\n"
				+ "	@Test\n"
				+ "	void test() {\n"
				+ "	}" +
				CATEGORY_CLASS_DECLARATIONS;

		String expected = ""
				+ "	@Test\n"
				+ "	void test() {\n"
				+ "	}" +
				CATEGORY_CLASS_DECLARATIONS;

		assertChange(original, expected);
	}

	@Test
	public void visit_NormalAnnotationWithClassLiteral_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Test.class.getName());
		defaultFixture.addImport(org.junit.experimental.categories.Category.class.getName());

		String original = "" +
				"	@Category(value = FirstCategory.class)\n"
				+ "	@Test\n"
				+ "	void test() {\n"
				+ "	}" +
				CATEGORY_CLASS_DECLARATIONS;

		String expected = "" +
				"	@Tag(\"fixturepackage.TestCU.FirstCategory\")\n"
				+ "	@Test\n"
				+ "	void test() {\n"
				+ "	}" +
				CATEGORY_CLASS_DECLARATIONS;

		assertChange(original, expected);
	}

	@Test
	public void visit_NormalAnnotationWithClassLiteralArray_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Test.class.getName());
		defaultFixture.addImport(org.junit.experimental.categories.Category.class.getName());

		String original = "" +
				"	@Category(value = { FirstCategory.class, SecondCategory.class })\n"
				+ "	@Test\n"
				+ "	void test() {\n"
				+ "	}" +
				CATEGORY_CLASS_DECLARATIONS;

		String expected = "" +
				"	@Tag(\"fixturepackage.TestCU.FirstCategory\")"
				+ " @Tag(\"fixturepackage.TestCU.SecondCategory\")\n"
				+ "	@Test\n"
				+ "	void test() {\n"
				+ "	}" +
				CATEGORY_CLASS_DECLARATIONS;

		assertChange(original, expected);
	}

	@Test
	public void visit_NormalAnnotationWithEmptyArray_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Test.class.getName());
		defaultFixture.addImport(org.junit.experimental.categories.Category.class.getName());

		String original = ""
				+ "	@Category(value = {})\n"
				+ "	@Test\n"
				+ "	void test_NormalAnnotationWithEmptyArray() {\n"
				+ "	}" +
				CATEGORY_CLASS_DECLARATIONS;

		String expected = ""
				+ "	@Test\n"
				+ "	void test_NormalAnnotationWithEmptyArray() {\n"
				+ "	}" +
				CATEGORY_CLASS_DECLARATIONS;

		assertChange(original, expected);
	}
}
