package eu.jsparrow.core.visitor.junit.jupiter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

class MigrateJUnit4ToJupiterASTVisitorTest extends UsesJDTUnitFixture {

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
	public void visit_JUnit4TestAnnotation_forDebug() throws Exception {
		defaultFixture.addImport(org.junit.Test.class.getName());

		String original = "" +
				"\n" +
				"	@Test\n" +
				"	public void test1() {\n" +
				"	}\n" +
				"\n" +
				"	@Test()\n" +
				"	public void test2() {\n" +
				"	}\n" +
				"\n" +
				"	@Test(timeout=1000L)\n" +
				"	public void test3() {\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_NotJUnit4TestAnnotation_forDebug() throws Exception {
		defaultFixture.addImport(java.lang.annotation.Retention.class.getName());
		defaultFixture.addImport(java.lang.annotation.RetentionPolicy.class.getName());
		defaultFixture.addImport(java.lang.annotation.Target.class.getName());
		defaultFixture.addImport(java.lang.annotation.ElementType.class.getName());
		String original = "" +
				"\n" +
				"	@Test\n" +
				"	public void test1() {\n" +
				"	}\n" +
				"\n" +
				"	@Retention(RetentionPolicy.RUNTIME)\n"
				+ "	@Target({ElementType.METHOD})\n"
				+ "	@interface Test {\n"
				+ "		\n"
				+ "	}";

		assertNoChange(original);
	}

	/**
	 * SIM-1873: Expected to fail as soon as {@link org.junit.Ignore} is
	 * transformed to {@link org.junit.jupiter.api.Disabled}.
	 */
	@Test
	public void visit_MarkerAnnotationIgnore_forDebug() throws Exception {
		defaultFixture.addImport(org.junit.Ignore.class.getName());

		String original = "" +
				"	@Ignore\n" +
				"	public void test() {\n" +
				"	}";
		assertNoChange(original);
	}

	/**
	 * SIM-1873: Expected to fail as soon as {@link org.junit.Ignore} is
	 * transformed to {@link org.junit.jupiter.api.Disabled}.
	 */
	@Test
	public void visit_SingleMemberAnnotationIgnore_forDebug() throws Exception {
		defaultFixture.addImport(org.junit.Ignore.class.getName());

		String original = "" +
				"	@Ignore(\"Test is not carried out.\")\n" +
				"	public void test() {\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	public void visit_SingleMemberAnnotationRunWith_forDebug() throws Exception {
		defaultFixture.addImport(org.junit.runner.RunWith.class.getName());
		defaultFixture.addImport(org.junit.runners.Parameterized.class.getName());

		String original = "" +
				"	@RunWith(Parameterized.class)\n" +
				"	class TestRunningWithParameterized {\n" +
				"		\n" +
				"	}";
		assertNoChange(original);

	}
}
