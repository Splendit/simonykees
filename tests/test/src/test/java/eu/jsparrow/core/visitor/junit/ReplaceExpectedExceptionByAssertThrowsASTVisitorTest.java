package eu.jsparrow.core.visitor.junit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

public class ReplaceExpectedExceptionByAssertThrowsASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		
		defaultFixture.addImport("org.junit.Rule");
		defaultFixture.addImport("org.junit.Test");
		defaultFixture.addImport("org.junit.rules.ExpectedException");
		defaultFixture.addImport("java.io.IOException");
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


}
