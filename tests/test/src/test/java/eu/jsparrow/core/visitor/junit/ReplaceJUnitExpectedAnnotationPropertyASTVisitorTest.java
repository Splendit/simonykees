package eu.jsparrow.core.visitor.junit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

class ReplaceJUnitExpectedAnnotationPropertyASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		addDependency("org.hamcrest", "hamcrest-library", "1.3");
		addDependency("org.hamcrest", "hamcrest-core", "1.3");

		defaultFixture.addImport("org.junit.Rule");
		defaultFixture.addImport("org.junit.Test");
		defaultFixture.addImport("org.junit.rules.ExpectedException");
		defaultFixture.addImport("java.io.IOException");
		defaultFixture.addImport("org.hamcrest.Matcher");
		setDefaultVisitor(new ReplaceJUnitExpectedAnnotationPropertyASTVisitor("org.junit.Assert.assertThrows"));
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_singleMemberValuePair_shouldTransform() throws Exception {
		String original = ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test(expected = IOException.class)\n"
				+ "public void methodInvocation() throws IOException {\n"
				+ "		throwIOException();\n"
				+ "}";
		String expected = ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void methodInvocation() {\n"
				+ "		assertThrows(IOException.class, () -> throwIOException());\n"
				+ "}";
		assertChange(original, expected);
	}
	
	
	@Test
	void visit_constructorThrowingException_shouldTransform() throws Exception {
		String original = ""
				+ "@Test(expected = IOException.class)\n"
				+ "public void constructorThrowingException() throws IOException {\n"
				+ "	new InnerClass();\n"
				+ "}"
				+ ""
				+ "class InnerClass {\n"
				+ "	public InnerClass() throws IOException {throw new IOException();}\n"
				+ "}";
		String expected = ""
				+ "@Test\n"
				+ "public void constructorThrowingException() {\n"
				+ "	assertThrows(IOException.class, () -> new InnerClass());\n"
				+ "}"
				+ ""
				+ "class InnerClass {\n"
				+ "	public InnerClass() throws IOException {throw new IOException();}\n"
				+ "}";
		assertChange(original, expected);
	}
	
	@Test
	void visit_throwStatement_shouldTransform() throws Exception {
		String original = ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test(expected = IOException.class)\n"
				+ "public void methodInvocation() throws IOException {\n"
				+ "		throw new IOException();\n"
				+ "}";
		String expected = ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void methodInvocation() {\n"
				+ "		assertThrows(IOException.class, () -> {throw new IOException();});\n"
				+ "}";
		assertChange(original, expected);
	}

	@Test
	void visit_methodInvocation_shouldTransform() throws Exception {
		String original = ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test(expected = IOException.class, timeout = 500L)\n"
				+ "public void methodInvocation() throws IOException {\n"
				+ "		throwIOException();\n"
				+ "}";
		String expected = ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test(timeout = 500L)\n"
				+ "public void methodInvocation() {\n"
				+ "		assertThrows(IOException.class, () -> throwIOException());\n"
				+ "}";
		assertChange(original, expected);
	}

	/*
	 * Negative tests
	 */

	@Test
	void visit_missingExpectedProperty_shouldNotTransform() throws Exception {
		String original = ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test(timeout = 500L)\n"
				+ "public void methodInvocation() throws IOException {\n"
				+ "		throwIOException();\n"
				+ "}";
		assertNoChange(original);
	}

	@Test
	void visit_emptyNormalAnnotationsProperty_shouldNotTransform() throws Exception {
		String original = ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test()\n"
				+ "public void methodInvocation() throws IOException {\n"
				+ "		throwIOException();\n"
				+ "}";
		assertNoChange(original);
	}

	@Test
	void visit_markerAnnotationsProperty_shouldNotTransform() throws Exception {
		String original = ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void methodInvocation() throws IOException {\n"
				+ "		throwIOException();\n"
				+ "}";
		assertNoChange(original);
	}
	
	@Test
	void visit_multipleNodesThrowingException_shouldNotTransform() throws Exception {
		String original = ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test(expected = IOException.class)\n"
				+ "public void methodInvocation() throws IOException {\n"
				+ "		throwIOException();\n"
				+ "		throw new IOException();\n"
				+ "}";
		assertNoChange(original);
	}
	
	@Test
	void visit_statementAfterThrowingException_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.fail", true, false);
		String original = ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test(expected = IOException.class)\n"
				+ "public void statementsAfterThrowingException() throws IOException {\n"
				+ "		throwIOException();\n"
				+ "		fail();\n"
				+ "}"
				;
		assertNoChange(original);
	}
	
	@Test
	void visit_multipleStatementAfterThrowingException_shouldNotTransform() throws Exception {
		String original = ""
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test(expected = IOException.class)\n"
				+ "public void statementsAfterThrowingException() throws IOException {\n"
				+ "		throwIOException();\n"
				+ "		System.out.println(\"Nothing\");\n"
				+ "		System.out.println(\"Should not reach here\");\n"
				+ "}";
		assertNoChange(original);
	}
	
	@Test
	void visit_nonEffectivelyFinal_shouldNotTransform() throws Exception {
		String original = ""
				+ "private void throwIOException(String message) throws IOException {}"
				+ ""
				+ "@Test(expected = IOException.class)\n"
				+ "public void methodInvocation() throws IOException {\n"
				+ "		String message = \"\";"
				+ "		message = \"test\";"
				+ "		throwIOException(message);\n"
				+ "}";
		assertNoChange(original);
	}
	
	@Test
	void visit_abstractTestCase_shouldNotTransform() throws Exception {
		String original = ""
				+ "abstract class AbstractTestClass { "
				+ "	@Test(expected = IOException.class)\n"
				+ "	public void methodInvocation() throws IOException;\n"
				+ "}";
		assertNoChange(original);
	}
}
