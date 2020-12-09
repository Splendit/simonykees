package eu.jsparrow.core.visitor.junit;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

public class RemoveExpectedAnnotationPropertyASTVisitorTest extends UsesJDTUnitFixture {

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
		setDefaultVisitor(new RemoveExpectedAnnotationPropertyASTVisitor("org.junit.Assert.assertThrows"));
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
}
