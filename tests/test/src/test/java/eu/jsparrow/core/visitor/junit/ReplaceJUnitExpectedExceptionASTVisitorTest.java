package eu.jsparrow.core.visitor.junit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

class ReplaceJUnitExpectedExceptionASTVisitorTest extends UsesJDTUnitFixture {

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
		setDefaultVisitor(new ReplaceJUnitExpectedExceptionASTVisitor("org.junit.Assert.assertThrows"));
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
				+ "public void methodInvocation() {\n"
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
				+ "	public void throwStatement() {\n"
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
				+ "	public void newInstanceCreation() {\n"
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
				+ "public void expectMessageString() {\n"
				+ "	IOException exception = assertThrows(IOException.class,() -> throwIOException());\n"
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
				+ "public void expectMessage() {\n"
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
				+ "public void expectMessageContainsMatcher() {\n"
				+ "	IOException exception = assertThrows(IOException.class,() -> throwIOException());\n"
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
				+ "public void throwableMatcher() {\n"
				+ "	Matcher<Throwable> causeMatcher = null;\n"
				+ "	IOException exception = assertThrows(IOException.class, () -> throwIOException());\n"
				+ "	assertThat(exception.getCause(), causeMatcher);\n"
				+ "}";

		assertChange(original, expected);
	}

	@Test
	void visit_fieldAccessExpectedException_shouldTransform() throws Exception {
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();\n"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void fieldAccess() throws IOException {\n"
				+ "	this.expectedException.expect(IOException.class);\n"
				+ "	throwIOException();\n"
				+ "}";
		String expected = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();\n"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void fieldAccess() {\n"
				+ "	assertThrows(IOException.class, () -> throwIOException());\n"
				+ "}";
		assertChange(original, expected);
	}

	@Test
	void visit_expectingExceptionSuperType_shouldTransform() throws Exception {
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();\n"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void expectingExceptionSuperType() throws IOException {\n"
				+ "	expectedException.expect(Exception.class);\n"
				+ "	throwIOException();\n"
				+ "}";
		String expected = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();\n"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void expectingExceptionSuperType() throws IOException {\n"
				+ "	assertThrows(Exception.class, () -> throwIOException());\n"
				+ "}";
		assertChange(original, expected);
	}

	@Test
	void visit_existingVarNameException_shouldTransform() throws Exception {
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();\n"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void existingVarNameException() throws IOException {\n"
				+ "	String exception = \"\";\n"
				+ "	expectedException.expect(IOException.class);\n"
				+ "	expectedException.expectMessage(\"value\");\n"
				+ "	throwIOException();\n"
				+ "}";
		String expected = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();\n"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void existingVarNameException() {\n"
				+ "	String exception = \"\";\n"
				+ "	IOException exception1 = assertThrows(IOException.class, () -> throwIOException());\n"
				+ "	assertTrue(exception1.getMessage().contains(\"value\"));\n"
				+ "}";
		assertChange(original, expected);
	}

	@Test
	void visit_multipleExpectMessage_shouldTransform() throws Exception {
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();\n"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void multipleExpectMessage() throws IOException {\n"
				+ "	expectedException.expect(IOException.class);\n"
				+ "	expectedException.expectMessage(\"value\");\n"
				+ "	expectedException.expectMessage(\"value2\");\n"
				+ "	throwIOException();\n"
				+ "}";
		String expected = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();\n"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void multipleExpectMessage() {\n"
				+ "	IOException exception = assertThrows(IOException.class, () -> throwIOException());\n"
				+ "	assertTrue(exception.getMessage().contains(\"value\"));\n"
				+ "	assertTrue(exception.getMessage().contains(\"value2\"));\n"
				+ "}";
		assertChange(original, expected);
	}

	@Test
	void visit_multipleExpectCause_shouldTransform() throws Exception {
		defaultFixture.addImport("org.hamcrest.Matchers");
		defaultFixture.addImport(java.io.FileNotFoundException.class.getName());
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();\n"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void multipleExpectCause() throws IOException {\n"
				+ "	Matcher<Throwable> isIO = Matchers.is(new IOException());\n"
				+ "	Matcher<Throwable> isNotFileException = Matchers.not(Matchers.is(new FileNotFoundException()));\n"
				+ "	expectedException.expect(IOException.class);\n"
				+ "	expectedException.expectCause(isIO);\n"
				+ "	expectedException.expectCause(isNotFileException);\n"
				+ "	throwIOException();\n"
				+ "}\n"
				+ "";
		String expected = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();\n"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void multipleExpectCause() {\n"
				+ "	Matcher<Throwable> isIO = Matchers.is(new IOException());\n"
				+ "	Matcher<Throwable> isNotFileException = Matchers.not(Matchers.is(new FileNotFoundException()));\n"
				+ "	IOException exception = assertThrows(IOException.class, () -> throwIOException());\n"
				+ "	assertThat(exception.getCause(), isIO);\n"
				+ "	assertThat(exception.getCause(), isNotFileException);\n"
				+ "}";
		assertChange(original, expected);
	}

	@Test
	void visit_throwExceptionSubtype_shouldTransform() throws Exception {
		defaultFixture.addImport("org.hamcrest.Matchers");
		defaultFixture.addImport(java.io.FileNotFoundException.class.getName());
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();\n"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ "\n"
				+ "@Test\n"
				+ "public void throwExceptionSubtype() throws IOException {\n"
				+ "	expectedException.expect(Exception.class);\n"
				+ "	throw new IOException();\n"
				+ "}";
		String expected = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();\n"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void throwExceptionSubtype() throws IOException {\n"
				+ "	assertThrows(Exception.class, () -> {\n"
				+ "		throw new IOException();\n"
				+ "	});\n"
				+ "}\n"
				+ "";
		assertChange(original, expected);
	}

	/*
	 * Negative tests
	 */

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

	@Test
	void visit_getterMethodForExpectedExceptions_shouldNotTransform() throws Exception {
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();\n"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void getterMethodForExpectedExceptions() throws IOException {\n"
				+ "	getExpectedeException().expect(IOException.class);\n"
				+ "	throwIOException();\n"
				+ "}\n"
				+ "\n"
				+ "private ExpectedException getExpectedeException() {\n"
				+ "	return this.expectedException;\n"
				+ "}";
		assertNoChange(original);
	}

	@Test
	void visit_unsupportedMethodInvocation_shouldNotTransform() throws Exception {
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();\n"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void unsupportedMethodInvocation() throws IOException {\n"
				+ "	expectedException.expect(IOException.class);\n"
				+ "	expectedException.apply(null, null);\n"
				+ "	throwIOException();\n"
				+ "}";
		assertNoChange(original);
	}

	@Test
	void visit_missingExceptionClassName_shouldNotTransform() throws Exception {
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();\n"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void expectMatcher() throws IOException {\n"
				+ "	Matcher<Exception> matcher = null;\n"
				+ "	expectedException.expect(matcher);\n"
				+ "	throwIOException();\n"
				+ "}";
		assertNoChange(original);
	}

	@Test
	void visit_multipleExpectInvocations_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.io.FileNotFoundException.class.getName());
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();\n"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void multipleExpectInvocations() throws IOException {\n"
				+ "	expectedException.expect(IOException.class);\n"
				+ "	expectedException.expect(FileNotFoundException.class);\n"
				+ "	throwIOException();\n"
				+ "}";
		assertNoChange(original);
	}

	@Test
	void visit_multipleNodesThrowingExceptions_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.io.FileNotFoundException.class.getName());
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();\n"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void multipleNodesThrowingException() throws IOException {\n"
				+ "	expectedException.expect(IOException.class);\n"
				+ "	throwIOException();\n"
				+ "	throwIOException();\n"
				+ "}";
		assertNoChange(original);
	}

	@Test
	void visit_statementsAfterThrowingException_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.io.FileNotFoundException.class.getName());
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();\n"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void statementsAfterThrowingException() throws IOException {\n"
				+ "	expectedException.expect(IOException.class);\n"
				+ "	throwIOException();\n"
				+ "	System.out.println();\n"
				+ "}";
		assertNoChange(original);
	}

	@Test
	void visit_multipleExpectedExceptionRules_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.io.FileNotFoundException.class.getName());
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();\n"
				+ ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException2 = ExpectedException.none();\n"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void multipleExpectedExceptionRules() throws IOException {\n"
				+ "	expectedException.expect(IOException.class);\n"
				+ "	expectedException2.expect(IOException.class);\n"
				+ "	throwIOException();\n"
				+ "}";
		assertNoChange(original);
	}
	
	@Test
	void visit_throwRtExceptions_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.io.FileNotFoundException.class.getName());
		String original = ""
				+ "	@Rule\n"
				+ "	public ExpectedException expectedException = ExpectedException.none();\n"
				+ "	\n"
				+ "	@Test\n"
				+ "	public void expectingRuntimeException() {\n"
				+ "		expectedException.expect(NullPointerException.class);\n"
				+ "		throwRtException();\n"
				+ "	}\n"
				+ "	\n"
				+ "	private void throwRtException() {\n"
				+ "		throw new NullPointerException();\n"
				+ "	}";
		assertNoChange(original);
	}
	
	@Test
	void visit_nonEffectivelyFinalVariable_shouldNotTransform() throws Exception {
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();\n"
				+ ""
				+ "private void throwIOException(String message) throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void multipleExpectMessage() throws IOException {\n"
				+ " String message = \"\";"
				+ " message += \"value\";"
				+ "	expectedException.expect(IOException.class);\n"
				+ "	throwIOException(message);\n"
				+ "}";
		assertNoChange(original);
	}

}
