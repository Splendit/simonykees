package eu.jsparrow.core.visitor.junit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

class ReplaceExpectedExceptionByAssertThrowsASTVisitorTest extends UsesJDTUnitFixture {

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
		setDefaultVisitor(new ReplaceExpectedExceptionByAssertThrowsASTVisitor());
	}
	
	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_methodInvocation_shouldTransform() throws Exception {
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void methodInvocation() throws IOException {\n"
				+ "		expectedException.expect(IOException.class);\n"
				+ "		throwIOException();\n"
				+ "}";
		String expected = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void methodInvocation() throws IOException {\n"
				+ "		assertThrows(IOException.class, () -> throwIOException());\n"
				+ "}";
		assertChange(original, expected);
	}
	
	@Test
	void visit_throwStatement_shouldTransform() throws Exception {
		String original = ""
				+ "	@Rule\n"
				+ "	public ExpectedException expectedException = ExpectedException.none();\n"
				+ "\n"
				+ "@Test\n"
				+ "public void throwStatement() throws IOException {\n"
				+ "		expectedException.expect(IOException.class);\n"
				+ "		throw new IOException();"
				+ "}";
		String expected = ""
				+ "	@Rule\n"
				+ "	public ExpectedException expectedException = ExpectedException.none();\n"
				+ "\n"
				+ "	@Test\n"
				+ "	public void throwStatement() throws IOException {\n"
				+ "		assertThrows(IOException.class, () -> {\n"
				+ "			throw new IOException();\n"
				+ "		});\n"
				+ "	}";
		assertChange(original, expected);
	}
	
	@Test
	void visit_newInstanceCreation_shouldTransform() throws Exception {
		String original = ""
				+ "	@Rule\n"
				+ "	public ExpectedException expectedException = ExpectedException.none();\n"
				+ "	\n"
				+ "	@Test\n"
				+ "	public void newInstanceCreation() throws IOException {\n"
				+ "		expectedException.expect(IOException.class);\n"
				+ "		new InnerClass();\n"
				+ "	}\n"
				+ "\n"
				+ "	class InnerClass {\n"
				+ "		public InnerClass() throws IOException {}\n"
				+ "	}";
		String expected = ""
				+ "	@Rule\n"
				+ "	public ExpectedException expectedException = ExpectedException.none();\n"
				+ "\n"
				+ "	@Test\n"
				+ "	public void newInstanceCreation() throws IOException {\n"
				+ "		assertThrows(IOException.class, () -> new InnerClass());\n"
				+ "	}\n"
				+ "\n"
				+ "	class InnerClass {\n"
				+ "		public InnerClass() throws IOException {}\n"
				+ "	}\n"
				+ "";
		assertChange(original, expected);
	}
	
	@Test
	void visit_throwableTypeMatcher_shouldTransform() throws Exception {
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void throwableMatcher() throws IOException {\n"
				+ "	Matcher<Throwable> causeMatcher = null;\n"
				+ "	expectedException.expect(IOException.class);\n"
				+ "	expectedException.expectCause(causeMatcher);\n"
				+ "	throwIOException();\n"
				+ "}";
		String expected = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void throwableMatcher() throws IOException {\n"
				+ "	Matcher<Throwable> causeMatcher = null;\n"
				+ "	Exception exception = assertThrows(IOException.class, () -> throwIOException());\n"
				+ "	assertThat(exception.getCause(), causeMatcher);\n"
				+ "}";
		
		assertChange(original, expected);
	}
	
	@Test
	void visit_undefinedMatcherType_shouldNotTransform() throws Exception {
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void undefinedMatcherType() throws IOException {\n"
				+ "	Matcher<?> causeMatcher = null;\n"
				+ "	expectedException.expect(IOException.class);\n"
				+ "	expectedException.expectCause(causeMatcher);\n"
				+ "	throwIOException();\n"
				+ "}";
		assertNoChange(original);
	}
	
	@Test
	void visit_captureTypeMatcher_shouldNotTransform() throws Exception {
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void captureTypeMatcher() throws IOException {\n"
				+ "	Matcher<? extends Throwable> causeMatcher = null;\n"
				+ "	expectedException.expect(IOException.class);\n"
				+ "	expectedException.expectCause(causeMatcher);\n"
				+ "	throwIOException();\n"
				+ "}";
		assertNoChange(original);
	}
}
