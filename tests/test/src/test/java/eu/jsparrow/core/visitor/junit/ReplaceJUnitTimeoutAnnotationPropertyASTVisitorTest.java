package eu.jsparrow.core.visitor.junit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

public class ReplaceJUnitTimeoutAnnotationPropertyASTVisitorTest extends UsesJDTUnitFixture {
	
	@BeforeEach
	public void setUpVisitor() throws Exception {
		
		
		addDependency("org.junit.jupiter", "junit-jupiter-api", "5.0.0");
		addDependency("junit", "junit", "4.13");

		defaultFixture.addImport("java.lang.Thread");
		defaultFixture.addImport("java.time.Duration");
		defaultFixture.addImport("org.junit.Test");
		setDefaultVisitor(new ReplaceJUnitTimeoutAnnotationPropertyASTVisitor());
	}
	
	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}
	
	@Test
	void visit_singleMemberValuePair_shouldTransform() throws Exception {
		String original = ""
				+ "@Test(timeout = 1)\n"
				+ "public void methodInvocation() {\n"
				+ "		Thread.sleep(500);\n"
				+ "}";
		String expected = ""
				+ "@Test\n"
				+ "public void methodInvocation() {\n"
				+ "		assertTimeout(ofMillis(1), () -> Thread.sleep(500));\n"
				+ "}";
		assertChange(original, expected);
	}

}
