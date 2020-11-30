package eu.jsparrow.core.visitor.junit;

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

	@Test
	public void baseCase() throws Exception {
		String original = "	"
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void exampleTest3() throws IOException {\n"
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
				+ "public void exampleTest3() throws IOException {\n"
				+ "		assertThrows(IOException.class, () -> throwIOException());\n"
				+ "}";
		assertChange(original, expected);
	}

}
