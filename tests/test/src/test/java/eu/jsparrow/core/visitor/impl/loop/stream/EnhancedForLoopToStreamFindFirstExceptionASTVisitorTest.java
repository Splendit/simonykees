package eu.jsparrow.core.visitor.impl.loop.stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamFindFirstASTVisitor;


@SuppressWarnings("nls")
class EnhancedForLoopToStreamFindFirstExceptionASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setDefaultVisitor(new EnhancedForLoopToStreamFindFirstASTVisitor());
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_loopWithBreakToFindFirst_shouldTransform() throws Exception {

		defaultFixture.addImport("java.util.List");		
		String original = "" +
				"	String loopWithBreakToFindFirstWithRuntimeException(List<String> strings) {\n"
				+ "		String result = \"\";\n"
				+ "		for (String string : strings) {\n"
				+ "			if (checkStringThrowingRuntimeException(string)) {\n"
				+ "				result = string;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "		}\n"
				+ "		return result;\n"
				+ "	}\n"
				+ "\n"
				+ "	boolean checkStringThrowingRuntimeException(String s) throws RuntimeException {\n"
				+ "		throw new RuntimeException();\n"
				+ "	}";
		
		String expected = "" +
				"	String loopWithBreakToFindFirstWithRuntimeException(List<String> strings) {\n"
				+ "		String result = strings.stream().filter(string -> checkStringThrowingRuntimeException(string)).findFirst().orElse(\"\");\n"
				+ "		return result;\n"
				+ "	}\n"
				+ "\n"
				+ "	boolean checkStringThrowingRuntimeException(String s) throws RuntimeException {\n"
				+ "		throw new RuntimeException();\n"
				+ "	}";

		assertChange(original, expected);
	}
}
