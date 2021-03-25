package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.junit.jupiter.AbstractReplaceJUnit4WithJupiterASTVisitorTest;

public class ReplaceJUnit4AssertThrowsWithJupiterNegativeASTVisitorTest
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
	public void visit_assertThrowsWithThrowingRunnableVariable_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertThrows", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());
		String original = ""
				+ "	@Test\n"
				+ "	public void test() {\n"
				+ "		ThrowingRunnable runnable = () -> throwsIOException(\"Simply throw an IOException\");\n"
				+ "		assertThrows(IOException.class, runnable);\n"
				+ "	}\n"
				+ "\n"
				+ "	private void throwsIOException(String message) throws IOException {\n"
				+ "		throw new IOException(message);\n"
				+ "	}";

		assertNoChange(original);
	}
}
