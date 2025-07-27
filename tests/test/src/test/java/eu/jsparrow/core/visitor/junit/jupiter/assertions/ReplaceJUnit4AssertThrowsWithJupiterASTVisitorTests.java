package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.junit.jupiter.AbstractReplaceJUnit4WithJupiterASTVisitorTest;

class ReplaceJUnit4AssertThrowsWithJupiterASTVisitorTests
		extends AbstractReplaceJUnit4WithJupiterASTVisitorTest {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		addDependency("org.junit.jupiter", "junit-jupiter-api", "5.4.0");
		defaultFixture.addImport("org.junit.Assert.assertThrows", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());
		setDefaultVisitor(new ReplaceJUnit4AssertionsWithJupiterASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_assertThrowsWithoutMessage_shouldTransform() throws Exception {
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
	void visit_assertThrowsWithMessage_shouldTransform() throws Exception {
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
	void visit_assertThrowsAsAssignmentRHS_shouldTransform() throws Exception {
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
	void visit_assertThrowsInFieldInitializer_shouldNotTransform() throws Exception {
		String original = ""
				+ "	IOException exception = assertThrows(IOException.class, () -> throwsIOException(\"Simply throw an IOException\"));\n"
				+ "\n"
				+ "	private void throwsIOException(String message) throws IOException {\n"
				+ "		throw new IOException(message);\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	void visit_assertThrowsInLocalClassInitializer_shouldNotTransform() throws Exception {
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
	void visit_assertThrowsInLocalClassTestMethod_shouldNotTransform() throws Exception {
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
	void visit_HiddenLocalThrowingRunnable_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.function.ThrowingRunnable");

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
