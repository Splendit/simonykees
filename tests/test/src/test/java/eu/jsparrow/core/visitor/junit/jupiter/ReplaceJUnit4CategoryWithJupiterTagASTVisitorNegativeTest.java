package eu.jsparrow.core.visitor.junit.jupiter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

class ReplaceJUnit4CategoryWithJupiterTagASTVisitorNegativeTest extends UsesJDTUnitFixture {

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
		addDependency("org.junit.jupiter", "junit-jupiter-api", "5.4.0");
		setDefaultVisitor(new ReplaceJUnit4CategoryWithJupiterTagASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_MethodWithoutTestAnnotation_shouldNotTransform() throws Exception {
		defaultFixture.addImport(org.junit.experimental.categories.Category.class.getName());

		String original = "" +
				"	@Category(FirstCategory.class)\n" +
				"	void test() {\n" +
				"	}" +
				CATEGORY_CLASS_DECLARATIONS;

		assertNoChange(original);
	}

	@Test
	void visit_MethodWithTwoTestAnnotations_shouldNotTransform() throws Exception {
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport(org.junit.experimental.categories.Category.class.getName());

		String original = "" +
				"	@Category(FirstCategory.class)\n" +
				"	@Test\n" +
				"	@org.junit.Test\n" +
				"	void test() {\n" +
				"	}" +
				CATEGORY_CLASS_DECLARATIONS;

		assertNoChange(original);
	}

	@Test
	void visit_CategoryAnnotationOnInterface_shouldNotTransform() throws Exception {
		defaultFixture.addImport(org.junit.experimental.categories.Category.class.getName());

		String original = ""
				+ "@Category(ExampleCategory.class)\n"
				+ "public interface InterfaceWithCategoryTest {\n"
				+ "}\n"
				+ "\n"
				+ "interface ExampleCategory {\n"
				+ "}";

		assertNoChange(original);
	}

	@Test
	void visit_CategoryAnnotationOnLocalClass_shouldNotTransform() throws Exception {
		defaultFixture.addImport(org.junit.experimental.categories.Category.class.getName());

		String original = "" +
				"void exampleWithLocalClass() {\n" +
				"	@Category(ExampleCategory.class)\n" +
				"	class LocalClassWithCategory {\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"interface ExampleCategory {\n" +
				"}";

		assertNoChange(original);
	}

	@Test
	void visit_CategoryAnnotationOnEnum_shouldNotTransform() throws Exception {
		defaultFixture.addImport(org.junit.experimental.categories.Category.class.getName());

		String original = "" +
				"	@Category(ExampleCategory.class)\n" +
				"	enum EnumWithCategory {\n" +
				"		FIRST, SECOND;\n" +
				"	}\n" +
				"\n" +
				"	interface ExampleCategory {\n" +
				"\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	void visit_CategoryAnnotationOnConstructor_shouldNotTransform() throws Exception {
		defaultFixture.addImport(org.junit.experimental.categories.Category.class.getName());

		String original = "" +
				"	class JUnit4CategoryAnnotationOnConstructorTest {\n" +
				"		@Category(ExampleCategory.class)\n" +
				"		JUnit4CategoryAnnotationOnConstructorTest() {\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				"	interface ExampleCategory {\n" +
				"\n" +
				"	}";

		assertNoChange(original);
	}
}
