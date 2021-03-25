package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.junit.jupiter.AbstractReplaceJUnit4WithJupiterASTVisitorTest;

public class ReplaceJUnit4AssertThrowsWithJupiterASTVisitorTest
		extends AbstractReplaceJUnit4WithJupiterASTVisitorTest {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		addDependency("org.junit.jupiter", "junit-jupiter-api", "5.4.0");
		setDefaultVisitor(new ReplaceJUnit4AssertThrowsWithJupiterASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_assertThrows_shouldNotTransform() throws Exception {
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
	public void visit_assertThrowsWithMessage_shouldNotTransform() throws Exception {
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
	public void visit_assertThrowsAsAssignmentRHS_shouldNotTransform() throws Exception {
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
}
