package eu.jsparrow.core.visitor.unused.type;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.jdtunit.JdtUnitException;

class JUnitTestMethodVisitorTest extends UsesJDTUnitFixture {

	private void createCompilationUnitRecognizedAsJUnit3Test(String testMethod)
			throws JavaModelException, BadLocationException, JdtUnitException {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, testMethod, Arrays.asList("public"));
		defaultFixture.setSuperClassType("TestCase");
	}

	@BeforeEach
	public void setUp() throws Exception {
		addDependency("junit", "junit", "4.13");
		addDependency("org.junit.jupiter", "junit-jupiter-api", "5.4.0");
		addDependency("org.junit.jupiter", "junit-jupiter-params", "5.7.0");
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_MethodWithNameTestWithinJunit3TestCase_shouldFindTestMethod()
			throws Exception {

		String testMethod = "" +
				"	public void test() {\n" +
				"	}";

		createCompilationUnitRecognizedAsJUnit3Test(testMethod);

		JUnitTestMethodVisitor visitor = new JUnitTestMethodVisitor();
		defaultFixture.accept(visitor);
		assertTrue(visitor.isJUnitTestCaseFound());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"	static class NestedClass {\n" +
					"		public void test() {\n" +
					"		}\n" +
					"	}",
			"" +
					"	public void noTest() {\n" +
					"	}",
			"" +
					"	public void setUp() {\n" +
					"	}",
			"" +
					"	public void tearDown() {\n" +
					"	}",
			"" +
					"	public void test(int i) {\n" +
					"	}",
			"" +
					"	private void test() {\n" +
					"	}",
	})
	void visit_MethodNotRecognizedAsJunit3Test_shouldNotFindTestMethod(String testMethod)
			throws Exception {

		createCompilationUnitRecognizedAsJUnit3Test(testMethod);

		JUnitTestMethodVisitor visitor = new JUnitTestMethodVisitor();
		defaultFixture.accept(visitor);
		assertFalse(visitor.isJUnitTestCaseFound());
	}

	private static Stream<Arguments> classNotRecognizedAsJUnit3Test() throws Exception {
		return Stream.of(
				Arguments.of(Arrays.asList("public"), "TestSuite"),
				Arguments.of(Arrays.asList("public", "abstract"), "TestCase"),
				Arguments.of(Arrays.asList(), "TestCase"));
	}

	@ParameterizedTest
	@MethodSource(value = "classNotRecognizedAsJUnit3Test")
	void visit_ClassNotRecognizedAsJunit3TestCase_shouldNotFindTestMethod(List<String> mainClassModifiers,
			String superClassName) throws Exception {

		String testMethod = "" +
				"	public void test() {\n" +
				"	}";

		defaultFixture.addImport("junit.framework" + "." + superClassName);
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, testMethod, mainClassModifiers);
		defaultFixture.setSuperClassType(superClassName);

		JUnitTestMethodVisitor visitor = new JUnitTestMethodVisitor();
		defaultFixture.accept(visitor);
		assertFalse(visitor.isJUnitTestCaseFound());
	}

	private static Stream<Arguments> jUnitTestAnnotationQualifiedNames() throws Exception {
		return Stream.of(
				Arguments.of("org.junit.Test"),
				Arguments.of("org.junit.jupiter.api.Test"),
				Arguments.of("org.junit.jupiter.params.ParameterizedTest"),
				Arguments.of("org.junit.jupiter.api.RepeatedTest"));

	}

	@ParameterizedTest
	@MethodSource(value = "jUnitTestAnnotationQualifiedNames")
	void visit_MethodWithQualifiedJUnitAnnotation_shouldFindTestMethod(String qualifiedAnnotationName)
			throws Exception {

		String codeWithTestMethod = "" +
				"\n" +
				"	@" + qualifiedAnnotationName + "\n" +
				"	void test() {\n" +
				"	}";

		JUnitTestMethodVisitor visitor = new JUnitTestMethodVisitor();

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, codeWithTestMethod);
		defaultFixture.accept(visitor);

		assertTrue(visitor.isJUnitTestCaseFound());

	}

}
