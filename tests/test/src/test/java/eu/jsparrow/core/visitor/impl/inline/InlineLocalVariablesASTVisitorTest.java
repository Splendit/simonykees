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

	// @Test
	// void visit__shouldTransform() throws Exception {
	// String original = "" +
	// "";
	// String expected = "" +
	// "";
	// assertChange(original, expected);
	// }
}
