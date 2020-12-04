package eu.jsparrow.core.visitor.junit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

class ReplaceExpectedExceptionASTVisitorTest extends UsesJDTUnitFixture {

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
		setDefaultVisitor(new ReplaceExpectedExceptionASTVisitor());
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
	void visit_expectMessageString_shouldTransform() throws Exception {
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void expectMessageString() throws IOException {\n"
				+ "	expectedException.expect(IOException.class);\n"
				+ "	expectedException.expectMessage(\"some message\");\n"
				+ "	throwIOException();\n"
				+ "}";
		String expected = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void expectMessageString() throws IOException {\n"
				+ "	IOException exception=assertThrows(IOException.class,() -> throwIOException());\n"
				+ "	assertTrue(exception.getMessage().contains(\"some message\"));"
				+ "}";
		assertChange(original, expected);
	}
	
	@Test
	void visit_expectMessage_shouldTransform() throws Exception {
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void expectMessage() throws IOException {\n"
				+ "	Matcher<String> causeMatcher = null;\n"
				+ "	expectedException.expect(IOException.class);\n"
				+ "	expectedException.expectMessage(causeMatcher);\n"
				+ "	throwIOException();\n"
				+ "}";
		String expected = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void expectMessage() throws IOException {\n"
				+ "	Matcher<String> causeMatcher = null;\n"
				+ "	IOException exception = assertThrows(IOException.class, () -> throwIOException());\n"
				+ "	assertThat(exception.getMessage(), causeMatcher);\n"
				+ "}";
		assertChange(original, expected);
	}
	
	@Test
	void visit_expectMessageContainsMatcher_shouldTransform() throws Exception {
		defaultFixture.addImport("org.hamcrest.Matchers");
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void expectMessageContainsMatcher() throws IOException {\n"
				+ "	expectedException.expect(IOException.class);\n"
				+ "	expectedException.expectMessage(Matchers.containsString(\"\"));\n"
				+ "	throwIOException();\n"
				+ "}";
		String expected = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void expectMessageContainsMatcher() throws IOException {\n"
				+ "	IOException exception=assertThrows(IOException.class,() -> throwIOException());\n"
				+ "	assertThat(exception.getMessage(),Matchers.containsString(\"\"));"
				+ "}";
		assertChange(original, expected);
	}
	
	@Test
	void visit_expectCause_shouldTransform() throws Exception {
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
				+ "	IOException exception = assertThrows(IOException.class, () -> throwIOException());\n"
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
