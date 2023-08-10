package eu.jsparrow.core.visitor.impl.inline;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.common.UsesJDTUnitFixture;

@SuppressWarnings({ "nls" })
public class InlineLocalVariablesASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setDefaultVisitor(new InlineLocalVariablesASTVisitor());
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_PrimitiveIntVariableUsedForReturn_shouldTransform() throws Exception {
		String original = "" +
				"		int getResult(int a, int b) {\n" +
				"			int result = a + b;\n" +
				"			return result;\n" +
				"		}";
		String expected = "" +
				"		int getResult(int a, int b) {\n" +
				"			return a + b;\n" +
				"		}";

		assertChange(original, expected);
	}

	@Test
	void visit_StringVariableUsedForReturn_shouldTransform() throws Exception {
		String original = "" +
				"		String getResult(String a, String b) {\n" +
				"			String result = a + b;\n" +
				"			return result;\n" +
				"		}";
		String expected = "" +
				"		String getResult(String a, String b) {\n" +
				"			return a + b;\n" +
				"		}";

		assertChange(original, expected);
	}

	@Test
	void visit_ExceptionUsedForThrow_shouldTransform() throws Exception {
		String original = "" +
				"		void throwException(String message) throws Exception {\n" +
				"			Exception exception = new Exception(message);\n" +
				"			throw exception;\n" +
				"		}";
		String expected = "" +
				"		void throwException(String message) throws Exception {\n" +
				"			throw new Exception(message);\n" +
				"		}";

		assertChange(original, expected);
	}

	private static Stream<Arguments> arrayInitializerToArrayCreation() {
		String testWithParam = "test (int a)";
		return Stream.of(
				Arguments.of(
						"" +
								"	int[] " + testWithParam + " {\n" +
								"		int[] counts = { a, a + a, a * 3 };\n" +
								"		return counts;\n" +
								"	}",
						"" +
								"	int[] " + testWithParam + " {\n" +
								"		return new int[]{ a, a + a, a * 3 };\n" +
								"	}"),
				Arguments.of(
						"" +
								"	int[] " + testWithParam + " {\n" +
								"		int counts[] = { a, a + a, a * 3 };\n" +
								"		return counts;\n" +
								"	}",
						"" +
								"	int[] " + testWithParam + " {\n" +
								"		return new int[]{ a, a + a, a * 3 };\n" +
								"	}"),
				Arguments.of(
						"" +
								"	int[][] " + testWithParam + " {\n" +
								"		int[] counts[] = { { a, a * 2 }, { a * 3, a * 4 } };\n" +
								"		return counts;\n" +
								"	}",
						"" +
								"	int[][] " + testWithParam + " {\n" +
								"		return new int[][]{ { a, a * 2 }, { a * 3, a * 4 } };\n" +
								"	}"));
	}

	@ParameterizedTest
	@MethodSource("arrayInitializerToArrayCreation")
	void visit_arrayInitializerToArrayCreation_shouldTransform(String original, String expected) throws Exception {
		assertChange(original, expected);
	}

	private static Stream<Arguments> subsequentDeclarations() {

		return Stream.of(
				Arguments.of(
						"" +
								"	void subsequentDeclarations() {\n" +
								"		int a = 1;\n" +
								"		int b = a;\n" +
								"		int c = b;\n" +
								"	}",
						"" +
								"	void subsequentDeclarations() {\n" +
								"		int b = 1;\n" +
								"		int c = b;\n" +
								"	}"),
				Arguments.of(
						"" +
								"	void subsequentDeclarations() {\n" +
								"		int a = 1;\n" +
								"		int b = a;\n" +
								"		int c = b;\n" +
								"		int d = c;\n" +
								"	}",
						"" +
								"	void subsequentDeclarations() {\n" +
								"		int b = 1;\n" +
								"		int d = b;\n" +
								"	}"),
				Arguments.of(
						"" +
								"	int subsequentDeclarations3() {\n" +
								"		int a = 1;\n" +
								"		int b = a;\n" +
								"		return b;\n" +
								"	}",
						"" +
								"	int subsequentDeclarations3() {\n" +
								"		int b = 1;\n" +
								"		return b;\n" +
								"	}"),
				Arguments.of(
						"" +
								"	int subsequentDeclarations4() {\n" +
								"		int a = 1;\n" +
								"		int b = a;\n" +
								"		int c = b;\n" +
								"		return c;\n" +
								"	}",
						"" +
								"	int subsequentDeclarations4() {\n" +
								"		int b = 1;\n" +
								"		return b;\n" +
								"	}"));
	}

	@ParameterizedTest
	@MethodSource("subsequentDeclarations")
	void visit_subsequentDeclarations_shouldPartiallyTransform(String original, String expected) throws Exception {
		assertChange(original, expected);
	}

	@Test
	void visit_variableUsedInAssignment_shouldTransform() throws Exception {
		String original = "" +
				"		int x;\n" +
				"		void useInAssignment() {\n" +
				"			int a = 1;\n" +
				"			x = a;\n" +
				"		}";

		String expected = "" +
				"		int x;\n" +
				"		void useInAssignment() {\n" +
				"			x = 1;\n" +
				"		}";

		assertChange(original, expected);
	}

	@Test
	void visit_variableWithSameNameInBlockBefore_shouldTransform() throws Exception {
		String original = "" +
				"	void variableWithSameNameInBlockBefore() {\n" +
				"		{\n" +
				"			int i = 10;\n" +
				"		}\n" +
				"		int i = 0;\n" +
				"		int x = i;\n" +
				"	}";

		String expected = "" +
				"	void variableWithSameNameInBlockBefore() {\n" +
				"		{\n" +
				"			int i = 10;\n" +
				"		}\n" +
				"		int x = 0;\n" +
				"	}";

		assertChange(original, expected);
	}

	// @Test
	// void visit__shouldTransform() throws Exception {
	// String original = "" +
	// "";
	// String expected = "" +
	// "";
	// assertChange(original, expected);
	// }
}
