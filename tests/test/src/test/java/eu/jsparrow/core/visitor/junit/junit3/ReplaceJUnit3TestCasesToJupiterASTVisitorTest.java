package eu.jsparrow.core.visitor.junit.junit3;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;
import eu.jsparrow.jdtunit.JdtUnitException;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class ReplaceJUnit3TestCasesToJupiterASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "3.8.2");
		addDependency("org.junit.jupiter", "junit-jupiter-api", "5.4.0");
		Junit3MigrationConfiguration configuration = new Junit3MigrationConfigurationFactory()
			.createJUnit4ConfigurationValues();
		setDefaultVisitor(new ReplaceJUnit3TestCasesASTVisitor(configuration));
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_fullyQualifiedAssertMethod_shouldTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.setSuperClassType("TestCase");

		String expected = "" +
				"@Test\n" +
				"public void test() {\n" +
				"	assertTrue(true);\n" +
				"}";
		String original = "" +
				"public void test() {\n" +
				"	junit.framework.Assert.assertTrue(true);\n" +
				"}";

		String expectedCompilationUnitFormat = ""
				+ "package %s;\n"
				+ "import static org.junit.Assert.assertTrue;\n"
				+ "import org.junit.Test;"
				+ "public class %s {\n"
				+ "	%s \n"
				+ "}";

		assertCompilationUnitMatch(original, expected, expectedCompilationUnitFormat);
	}

	@Test
	public void visit_UnqualifiedTestCase_shouldTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.setSuperClassType("TestCase");
		String original = ""
				+ "public void test() {\n"
				+ "	int number = 1;\n"
				+ "	assertEquals(1, number);\n"
				+ "}";

		String expected = ""
				+ "@Test"
				+ "public void test(){\n"
				+ "	int number = 1;\n"
				+ "	assertEquals(1,number);\n"
				+ "}";
		String expectedCompilationUnitFormat = ""
				+ "package %s;\n"
				+ "import static org.junit.Assert.assertTrue;\n"
				+ "import org.junit.Test;"
				+ "public class %s {\n"
				+ "	%s \n"
				+ "}";

		assertCompilationUnitMatch(original, expected, expectedCompilationUnitFormat);

	}

	@Disabled
	@Test
	public void visit_TestCaseThisFieldAccess_shouldTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	public static class UnqualifiedFieldAccessTest extends TestCase {\n"
				+ "		private int number = 1;\n"
				+ "\n"
				+ "		public void test() {\n"
				+ "			assertEquals(1, this.number);\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	public static class UnqualifiedFieldAccessTest {\n"
				+ "		private int number = 1;\n"
				+ "\n"
				+ "		@Test"
				+ "		 public void test(){\n"
				+ "			assertEquals(1, this.number);\n"
				+ "		}\n"
				+ "	}";
		assertChange(original, expected);

	}

	@Test
	public void visit_SuperConstructorForObject_shouldTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	class NoTestCase {\n"
				+ "		NoTestCase() {\n"
				+ "			super();\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	public static class ExampleTestCase extends TestCase {\n"
				+ "\n"
				+ "	}";

		String expected = "" +
				"	class NoTestCase {\n"
				+ "		NoTestCase() {\n"
				+ "			super();\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	public static class ExampleTestCase {\n"
				+ "\n"
				+ "	}";
		assertChange(original, expected);

	}

	
	private void assertCompilationUnitMatch(String originalMethodDeclaration, String expectedMethodDeclaration, String expectedCompilationUnitFormat)
			throws JdtUnitException, JavaModelException, BadLocationException {
		defaultFixture.addMethodDeclarationFromString(originalMethodDeclaration);

		AbstractASTRewriteASTVisitor defaultVisitor = getDefaultVisitor();
		defaultVisitor.setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(getDefaultVisitor());
		String expectedCUSource = String.format(expectedCompilationUnitFormat, "fixturepackage", DEFAULT_TYPE_DECLARATION_NAME,
				expectedMethodDeclaration);
		assertMatch(
				ASTNodeBuilder.createCompilationUnitFromString(expectedCUSource),
				defaultFixture.getRootNode());
	}
}
