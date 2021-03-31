package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.junit.jupiter.AbstractReplaceJUnit4WithJupiterASTVisitorTest;

public class ReplaceJUnit4AssertionsWithJupiterNegativeASTVisitorTest
		extends AbstractReplaceJUnit4WithJupiterASTVisitorTest {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		addDependency("org.junit.jupiter", "junit-jupiter-api", "5.4.0");
		setDefaultVisitor(new ReplaceJUnit4AssertionsWithJupiterASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_assertEqualsInTestMethodOfAnonymousClass_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertEquals", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	void test() {\n"
				+ "		Runnable r = new Runnable() {\n"
				+ "			@Test\n"
				+ "			@Override\n"
				+ "			public void run() {\n"
				+ "				assertEquals(10L, 10L);\n"
				+ "			}\n"
				+ "		};\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_assertEqualsInTestMethodOfEnum_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertEquals", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	enum TestsEnum {\n"
				+ "		TEST;\n"
				+ "\n"
				+ "		@Test\n"
				+ "		void test() {\n"
				+ "			assertEquals(10L, 10L);\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_assertEqualsAsLambdaBody_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertEquals", true, false);
		String original = "" +
				"	void test() {\n"
				+ "		Runnable r = () -> assertEquals(10L, 10L);\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_assertEqualsWithQualifierAsLambdaBody_shouldNotTransform() throws Exception {
		defaultFixture.addImport(org.junit.Assert.class.getName());
		String original = "" +
				"	void test() {\n"
				+ "		Runnable r = () -> Assert.assertEquals(10L, 10L);\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_assertEqualsInTestMethodOfLocalClass_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertEquals", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	void test() {\n"
				+ "		class LocalClass {\n"
				+ "			@Test\n"
				+ "			void test() {\n"
				+ "				assertEquals(10L, 10L);\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_assertThrowsWithoutMessage_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertThrows", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());
		String original = "" +
				"	@Test\n"
				+ "	public void testAssertThrows() {\n"
				+ "		assertThrows(IOException.class, () -> throwsIOException(\"Simply throw an IOException\"));\n"
				+ "	}\n"
				+ "\n"
				+ "	private void throwsIOException(String message) throws IOException {\n"
				+ "		throw new IOException(message);\n"
				+ "	}";

		String expected = "" +
				"	@Test\n"
				+ "	public void testAssertThrows() {\n"
				+ "		assertThrows(IOException.class, () -> throwsIOException(\"Simply throw an IOException\"));\n"
				+ "	}\n"
				+ "\n"
				+ "	private void throwsIOException(String message) throws IOException {\n"
				+ "		throw new IOException(message);\n"
				+ "	}";
		assertChange(original, expected, Arrays.asList("import java.io.IOException;",
				"import org.junit.jupiter.api.Test;", "import static org.junit.jupiter.api.Assertions.assertThrows;"));
	}

	@Test
	public void visit_assertThrowsWithMessage_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertThrows", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());
		String original = "" +
				"	@Test\n"
				+ "	public void testAssertThrows() {\n"
				+ "		assertThrows(\"Expecting IOException.\", IOException.class, () -> throwsIOException(\"Simply throw an IOException\"));\n"
				+ "	}\n"
				+ "\n"
				+ "	private void throwsIOException(String message) throws IOException {\n"
				+ "		throw new IOException(message);\n"
				+ "	}";

		String expected = "" +
				"	@Test\n"
				+ "	public void testAssertThrows() {\n"
				+ "		assertThrows(IOException.class,() -> throwsIOException(\"Simply throw an IOException\"),\"Expecting IOException.\");\n"
				+ "	}\n"
				+ "\n"
				+ "	private void throwsIOException(String message) throws IOException {\n"
				+ "		throw new IOException(message);\n"
				+ "	}";

		assertChange(original, expected, Arrays.asList("import java.io.IOException;",
				"import org.junit.jupiter.api.Test;", "import static org.junit.jupiter.api.Assertions.assertThrows;"));
	}

	@Test
	public void visit_assertThrowsAsAssignmentRHS_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertThrows", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());
		String original = "" +
				"	@Test\n"
				+ "	public void testAssertThrows() {\n"
				+ "		IOException exception = assertThrows(IOException.class, () -> throwsIOException(\"Simply throw an IOException\"));\n"
				+ "	}\n"
				+ "\n"
				+ "	private void throwsIOException(String message) throws IOException {\n"
				+ "		throw new IOException(message);\n"
				+ "	}";

		String expected = "" +
				"	@Test\n"
				+ "	public void testAssertThrows() {\n"
				+ "		IOException exception = assertThrows(IOException.class, () -> throwsIOException(\"Simply throw an IOException\"));\n"
				+ "	}\n"
				+ "\n"
				+ "	private void throwsIOException(String message) throws IOException {\n"
				+ "		throw new IOException(message);\n"
				+ "	}";

		assertChange(original, expected, Arrays.asList("import java.io.IOException;",
				"import org.junit.jupiter.api.Test;", "import static org.junit.jupiter.api.Assertions.assertThrows;"));
	}

	@Test
	public void visit_assertThrowsInFieldInitializer_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertThrows", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());
		String original = ""
				+ "	IOException exception = assertThrows(IOException.class, () -> throwsIOException(\"Simply throw an IOException\"));\n"
				+ "\n"
				+ "	private void throwsIOException(String message) throws IOException {\n"
				+ "		throw new IOException(message);\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_assertThrowsInLocalClassInitializer_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertThrows", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());
		String original = ""
				+ "	@Test\n"
				+ "	public void test() {\n"
				+ "		class LocalClass {\n"
				+ "			{\n"
				+ "				assertThrows(IOException.class, () -> throwsIOException(\"Simply throw an IOException\"));\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	private void throwsIOException(String message) throws IOException {\n"
				+ "		throw new IOException(message);\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_assertThrowsInLocalClassTestMethod_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertThrows", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());
		String original = ""
				+ "	@Test\n"
				+ "	public void test() {\n"
				+ "		class LocalClass  {\n"
				+ "			@Test\n"
				+ "			void test() {\n"
				+ "				assertThrows(IOException.class, () -> throwsIOException(\"Simply throw an IOException\"));\n"
				+ "			}\n"
				+ "		}		\n"
				+ "	}\n"
				+ "\n"
				+ "	private void throwsIOException(String message) throws IOException {\n"
				+ "		throw new IOException(message);\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_HiddenLocalThrowingRunnable_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertThrows", true, false);
		defaultFixture.addImport(org.junit.function.ThrowingRunnable.class.getName());
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());

		String original = ""
				+ "	ThrowingRunnable runnable = () -> throwsIOException(\"Simply throw an IOException\");"
				+ "	@Test\n"
				+ "	public void testHidingLocalThrowingRunnable() {\n"
				+ "		{\n"
				+ "			ThrowingRunnable runnable = () -> throwsIOException(\"Simply throw an IOException\");\n"
				+ "		}\n"
				+ "		assertThrows(\"Expecting IOException.\", IOException.class, runnable);\n"
				+ "	}\n"
				+ "\n"
				+ "	private void throwsIOException(String message) throws IOException {\n"
				+ "		throw new IOException(message);\n"
				+ "	}";

		assertNoChange(original);
	}
}