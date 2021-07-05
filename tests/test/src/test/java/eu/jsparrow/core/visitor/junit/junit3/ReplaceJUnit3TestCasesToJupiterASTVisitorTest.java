package eu.jsparrow.core.visitor.junit.junit3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

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
		String original = ""
				+ " class MyTestcase extends TestCase {\n"
				+ "\n"
				+ "	void test() {\n"
				+ "		junit.framework.Assert.assertTrue(true);\n"
				+ "	}\n"
				+ "\n"
				+ "}";

		String expected = ""
				+ " class MyTestcase {\n"
				+ "\n"
				+ "	@Test\n"
				+ "	void test() {\n"
				+ "		assertTrue(true);\n"
				+ "	}\n"
				+ "\n"
				+ "}";
		assertChange(original, expected);
	}
}
