package eu.jsparrow.core.visitor.impl.loop.stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamAnyMatchASTVisitor;

@SuppressWarnings("nls")
class EnhancedForLoopToStreamAnyMatchExceptionASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setDefaultVisitor(new EnhancedForLoopToStreamAnyMatchASTVisitor());
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_loopWithBreakToAllMatchWithRuntimeException_shouldTransform() throws Exception {

		defaultFixture.addImport("java.util.List");
		
		String original = "" +
				"	boolean loopWithBreakToAllMatchWithRuntimeException(List<String> strings) {\n"
				+ "		boolean allValid = true;\n"
				+ "		for (String string : strings) {\n"
				+ "			if (!checkStringThrowingRuntimeException(string)) {\n"
				+ "				allValid = false;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "		}\n"
				+ "		return allValid;\n"
				+ "	}\n"
				+ "\n"
				+ "	boolean checkStringThrowingRuntimeException(String s) throws RuntimeException {\n"
				+ "		throw new RuntimeException();\n"
				+ "	}";
		
		String expected = "" +
				"	boolean loopWithBreakToAllMatchWithRuntimeException(List<String> strings) {\n"
				+ "		boolean allValid = strings.stream().allMatch(string -> checkStringThrowingRuntimeException(string));\n"
				+ "		return allValid;\n"
				+ "	}\n"
				+ "\n"
				+ "	boolean checkStringThrowingRuntimeException(String s) throws RuntimeException {\n"
				+ "		throw new RuntimeException();\n"
				+ "	}";

		assertChange(original, expected);
	}
}
