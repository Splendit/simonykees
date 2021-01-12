package eu.jsparrow.core.visitor.junit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;
class ReplaceJUnitTimeoutAnnotationPropertyASTVisitorTest extends UsesJDTUnitFixture {
	
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
	void visit_singleStatementTestBody_shouldTransform() throws Exception {
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
	
	@Test
	void visit_multipleStatements_shouldTransform() throws Exception {
		String original = ""
				+ "@Test(timeout = 1)\n"
				+ "public void methodInvocation() {\n"
				+ "		Thread.sleep(500);\n"
				+ "		Thread.sleep(500);\n"
				+ "}";
		String expected = ""
				+ "@Test\n"
				+ "public void methodInvocation() {\n"
				+ "		assertTimeout(ofMillis(1), () -> {"
				+ "			Thread.sleep(500);\n"
				+ "			Thread.sleep(500);\n"
				+ "		});"
				+ "}";
		assertChange(original, expected);
	}
	
	@Test
	void visit_combinedAnnotationProperties_shouldTransform() throws Exception {
		String original = ""
				+ "@Test(expected=IOException.class, timeout = 500)\n"
				+ "public void combinedAnnotationProperties() throws InterruptedException, IOException {\n"
				+ "	Thread.sleep(2);\n"
				+ "	throw new IOException();\n"
				+ "}";
		String expected = ""
				+ "@Test(expected=IOException.class)\n"
				+ "public void combinedAnnotationProperties() throws InterruptedException, IOException {\n"
				+ "	assertTimeout(ofMillis(500), () -> {\n"
				+ "		Thread.sleep(2);\n"
				+ "		throw new IOException();\n"
				+ "	});\n"
				+ "}";
		assertChange(original, expected);
	}
	
	@Test
	void visit_ifStatement_shouldTransform() throws Exception {
		defaultFixture.addImport("java.io.IOExceptio");
		String original = ""
				+ "@Test(timeout = 500)\n"
				+ "public void ifStatement() throws InterruptedException, IOException {\n"
				+ "	if(true) {\n"
				+ "		Thread.sleep(2);\n"
				+ "		throw new IOException();\n"
				+ "	}\n"
				+ "}";
		String expected = ""
				+ "@Test\n"
				+ "public void ifStatement() throws InterruptedException, IOException {\n"
				+ "	assertTimeout(ofMillis(500), () -> {\n"
				+ "		if(true) {\n"
				+ "			Thread.sleep(2);\n"
				+ "			throw new IOException();\n"
				+ "		}\n"
				+ "	});\n"
				+ "}\n"
				+ "";
		assertChange(original, expected);
	}
	
	@Test
	void visit_missingAnnotationProperties_shouldNotTransform() throws Exception {
		String original = ""
				+ "@Test\n"
				+ "public void missingAnnotationProperty() {\n"
				+ "		Thread.sleep(500);\n"
				+ "}";
		assertNoChange(original);
	}
	
	@Test
	void visit_missingTimeoutAnnotationProperty_shouldNotTransform() throws Exception {
		defaultFixture.addImport("java.io.IOException");
		String original = ""
				+ "@Test(expected=IOException.class)\n"
				+ "public void missingTimeoutAnnotationProperty() throws IOException {\n"
				+ "		Thread.sleep(500);\n"
				+ "}";
		assertNoChange(original);
	}
	
	@Test
	void visit_missingTestAnnotation_shouldNotTransform() throws Exception {
		String original = ""
				+ "@Override"
				+ "public String toString() {\n"
				+ "		return \"\";\n"
				+ "}";

		assertNoChange(original);
	}
}
