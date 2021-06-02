package eu.jsparrow.core.visitor.junit.dedicated;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

class UseDedicatedAssertionsASTVisitorTest extends UsesJDTUnitFixture {

	
	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("org.junit.jupiter", "junit-jupiter-api", "5.0.0");
		addDependency("junit", "junit", "4.13");
		setDefaultVisitor(new UseDedicatedAssertionsASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_assertTrueWithEqualsInvocation_forDebug() throws Exception {
		defaultFixture.addImport(org.junit.Test.class.getName());

		String original = "" +
				"	@Test\n" +
				"	void test() {\n" + 
				"		assertTrue(a.equals(b));" +
				"	}";

		assertNoChange(original);
	}
}
