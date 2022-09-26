package org.eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.ifstatement.ReplaceMultiBranchIfBySwitchASTVisitor;

/**
 * focuses on covering {@code SwitchCaseExpressionsVisitor}.
 */
class ReplaceMultiBranchIfBySwitchIfConditionsASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	void setUp() {
		setDefaultVisitor(new ReplaceMultiBranchIfBySwitchASTVisitor());
		setJavaVersion(JavaCore.VERSION_14);
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	public static Stream<Arguments> arguments_IfStatementWithStrings() throws Exception {
		return Stream.of(
				Arguments.of(
						""
								+ "		if (value.equals(\"A\")) {\n"
								+ "		} else if (value.equals(\"B\") || value.equals(\"C\") || value.equals(\"D\")) {\n"
								+ "		} else {\n"
								+ "		}",
						""
								+ "		switch (value) {\n"
								+ "		case \"A\" -> {break;}\n"
								+ "		case \"B\", \"C\", \"D\" -> {break;}\n"
								+ "		default -> {break;}\n"
								+ "		}"),
				Arguments.of(
						""
								+ "		if (\"A\".equals(value)) {\n"
								+ "		} else if (\"B\".equals(value) || \"C\".equals(value) || \"D\".equals(value)) {\n"
								+ "		} else {\n"
								+ "		}",
						""
								+ "		switch (value) {\n"
								+ "		case \"A\" -> {break;}\n"
								+ "		case \"B\", \"C\", \"D\" -> {break;}\n"
								+ "		default -> {break;}\n"
								+ "		}"),
				Arguments.of(
						""
								+ "		if (\"A\".equals(value)) {\n"
								+ "		} else if (\"B\".equals(value) || \"C\".equals(value) || \"D\".equals(value)) {\n"
								+ "		} else if (value.equals(\"E\") || value.equals(\"F\") ){\n"
								+ "		}",
						""
								+ "		switch (value) {\n"
								+ "		case \"A\" -> {break;}\n"
								+ "		case \"B\", \"C\", \"D\" -> {break;}\n"
								+ "		case \"E\", \"F\" -> {break;}\n"
								+ "		}"));

	}

	@ParameterizedTest
	@MethodSource("arguments_IfStatementWithStrings")
	void visit_IfStatementWithStrings_shouldTransform(String ifStatement, String expectedSwitch) throws Exception {
		String original = ""
				+ "	void testIfWithStrings(String value) {\n"
				+ ifStatement + "\n"
				+ "	}";
		String expected = ""
				+ "	void testIfWithStrings(String value) {\n"
				+ expectedSwitch + "\n"
				+ "	}";

		assertChange(original, expected);
	}

	public static Stream<Arguments> arguments_IfStatementWithCharValues() throws Exception {
		return Stream.of(
				Arguments.of(
						""
								+ "		if (value == 'A') {\n"
								+ "		} else if (value == 'B' || value == 'C' || value == 'D') {\n"
								+ "		} else {\n"
								+ "		}",
						""
								+ "		switch (value) {\n"
								+ "		case 'A' -> {break;}\n"
								+ "		case 'B', 'C', 'D' -> {break;}\n"
								+ "		default -> {break;}\n"
								+ "		}"),
				Arguments.of(
						""
								+ "		if('A' == value) {\n"
								+ "		} else if('B' == value || 'C' == value || 'D' == value) {\n"
								+ "		} else {\n"
								+ "		}",
						""
								+ "		switch (value) {\n"
								+ "		case 'A' -> {break;}\n"
								+ "		case 'B', 'C', 'D' -> {break;}\n"
								+ "		default -> {break;}\n"
								+ "		}"),
				Arguments.of(
						""
								+ "		if('A' == value) {\n"
								+ "		} else if ('B' == value || 'C' == value || 'D' == value) {\n"
								+ "		} else if (value == 'E' || value == 'F') {\n"
								+ "		}",
						""
								+ "		switch (value) {\n"
								+ "		case 'A' -> {break;}\n"
								+ "		case 'B', 'C', 'D' -> {break;}\n"
								+ "		case 'E', 'F' -> {break;}\n"
								+ "		}"));
	}

	@ParameterizedTest
	@MethodSource("arguments_IfStatementWithCharValues")
	void visit_IfStatementWithCharValues_shouldTransform(String ifStatement, String expectedSwitch) throws Exception {
		String original = ""
				+ "	void testIfWithCharValues(char value) {\n"
				+ ifStatement + "\n"
				+ "	}";
		String expected = ""
				+ "	void testIfWithCharValues(char value) {\n"
				+ expectedSwitch + "\n"
				+ "	}";

		assertChange(original, expected);
	}

	public static Stream<Arguments> arguments_IfStatementWithIntValues() throws Exception {
		return Stream.of(
				Arguments.of(
						""
								+ "		if (value == 1) {\n"
								+ "		} else if (value == 2 || value == 3 || value == 4) {\n"
								+ "		} else {\n"
								+ "		}",
						""
								+ "		switch (value) {\n"
								+ "		case 1 -> {break;}\n"
								+ "		case 2, 3, 4 -> {break;}\n"
								+ "		default -> {break;}\n"
								+ "		}"),
				Arguments.of(
						""
								+ "		if (1 == value) {\n"
								+ "		} else if (2 == value || 3 == value || 4 == value) {\n"
								+ "		} else {\n"
								+ "		}",
						""
								+ "		switch (value) {\n"
								+ "		case 1 -> {break;}\n"
								+ "		case 2, 3, 4 -> {break;}\n"
								+ "		default -> {break;}\n"
								+ "		}"),
				Arguments.of(
						""
								+ "		if (+ - + 1 == value) {\n"
								+ "		} else if (-2 == value || -3 == value || -4 == value) {\n"
								+ "		} else if (value == +5 || value == +6) {\n"
								+ "		}",
						""
								+ "		switch (value) {\n"
								+ "		case + - + 1 -> {break;}\n"
								+ "		case -2, -3, -4 -> {break;}\n"
								+ "		case +5, +6 -> {break;}\n"
								+ "		}"));

	}

	@ParameterizedTest
	@MethodSource("arguments_IfStatementWithIntValues")
	void visit_IfStatementWithIntValues_shouldTransform(String ifStatement, String expectedSwitch) throws Exception {
		String original = ""
				+ "	void testIfWithIntValues(int value) {\n"
				+ ifStatement + "\n"
				+ "	}";
		String expected = ""
				+ "	void testIfWithIntValues(int value) {\n"
				+ expectedSwitch + "\n"
				+ "	}";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = { "\"a\"", "\"\\u0061\"", "\"\\141\""
	// breaks the test:
	// , "\"\\143\""
	})
	void visit_NonUniqueStringLiterals_shouldNotTransform(String redundantLiteral) throws Exception {
		String original = String.format(""
				+ "	void exampleWithSystemOutPrintln(String value) {\n"
				+ "		if (value.equals(\"a\") || value.equals(%s)) {\n"
				+ "			System.out.println(1);\n"
				+ "		} else if (value.equals(\"b\")) {\n"
				+ "			System.out.println(2);\n"
				+ "		} else {\n"
				+ "			System.out.println(3);\n"
				+ "		}\n"
				+ "	}", redundantLiteral);

		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = { "'a'", "'\\u0061'", "'\\141'"
	// breaks the test:
	// , "'\\143'"
	})
	void visit_NonUniqueCharacterLiterals_shouldNotTransform(String redundantLiteral) throws Exception {
		String original = String.format(""
				+ "void nonUniqueCharacterLiterals(char value) {\n"
				+ "		if (value == 'a' || value == %s) {\n"
				+ "			System.out.println(1);\n"
				+ "		} else if (value == 'b') {\n"
				+ "			System.out.println(2);\n"
				+ "		} else {\n"
				+ "			System.out.println(3);\n"
				+ "		}\n"
				+ "	}", redundantLiteral);

		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = { "16", "0x10", "020"
	// breaks the test:
	// , "0"
	})
	void visit_NonUniqueIntLiterals_shouldNotTransform(String redundantLiteral) throws Exception {
		String original = String.format(""
				+ "	void nonUniqueIntLiterals(int value) {\n"
				+ "		if (value == 16 || value == %s) {\n"
				+ "			System.out.println(1);\n"
				+ "		} else if (value == 32) {\n"
				+ "			System.out.println(2);\n"
				+ "		} else {\n"
				+ "			System.out.println(3);\n"
				+ "		}\n"
				+ "	}", redundantLiteral);

		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			""
					+ "	void negative(char value, char value2) {\n"
					+ "		if (value == value2) {\n"
					+ "		} else if (value == 'B' || value == 'C') {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}",
			""
					+ "	void negative(char value, char value2) {\n"
					+ "		if (value == 'A') {\n"
					+ "		} else if (value == value2 || value == 'C') {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}",
			""
					+ "	void negative(char value, char value2) {\n"
					+ "		if (value == 'A') {\n"
					+ "		} else if (value == 'B' || value == value2) {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}",
			""
					+ "	void negative(int value, int value2) {\n"
					+ "		if (value == value2) {\n"
					+ "		} else if (value == 2 || value == 3) {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}",
			""
					+ "	void negative(int value, int value2) {\n"
					+ "		if (value == 1) {\n"
					+ "		} else if (value == value2 || value == 3) {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}",
			""
					+ "	void negative(int value, int value2) {\n"
					+ "		if (value == 1) {\n"
					+ "		} else if (value == 2 || value == value2) {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}",
			""
					+ "	void negative(String value, String value2) {\n"
					+ "		if (value.equals(value2)) {\n"
					+ "		} else if (value.equals(\"B\") || value.equals(\"C\")) {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}",
			""
					+ "	void negative(String value, String value2) {\n"
					+ "		if (value.equals(\"A\")) {\n"
					+ "		} else if (value.equals(value2) || value.equals(\"C\")) {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}",
			""
					+ "	void negative(String value, String value2) {\n"
					+ "		if (value.equals(\"A\")) {\n"
					+ "		} else if (value.equals(\"B\") || value.equals(value2)) {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}",
	})
	void visit_NoCaseExpressionFound_shouldNotTransform(String codeEexample)
			throws Exception {
		assertNoChange(codeEexample);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			""
					+ "	void incompatibleTypes(char value) {\n"
					+ "		if (value == 1) {\n"
					+ "		} else if (value == 'B' || value == 'C') {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}",
			""
					+ "	void incompatibleTypes(char value) {\n"
					+ "		if (value == 'A') {\n"
					+ "		} else if (value == 'B' || value == 3) {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}",
			""
					+ "	void incompatibleTypes(int value) {\n"
					+ "		if (value == '1') {\n"
					+ "		} else if (value == 2 || value == 3) {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}",
			""
					+ "	void incompatibleTypes(int value) {\n"
					+ "		if (value == 1) {\n"
					+ "		} else if (value == 2L || value == 3) {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}",
			""
					+ "	void incompatibleTypes(int value) {\n"
					+ "		if (value == 1) {\n"
					+ "		} else if (value == 2 || value == 3.0) {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}",
			""
					+ "void incompatibleTypes(long value) {\n"
					+ "			if (value == 1) {\n"
					+ "			} else if (value == 2 || value == 3) {\n"
					+ "			} else {\n"
					+ "			}\n"
					+ "		}",
			""
					+ "	void incompatibleTypes(String value) {\n"
					+ "		if (value.equals('A')) {\n"
					+ "		} else if (value.equals(\"B\") || value.equals(\"C\")) {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}",
			""
					+ "	void incompatibleTypes(String value) {\n"
					+ "		if (value.equals(\"A\")) {\n"
					+ "		} else if (value.equals(2) || value.equals(\"C\")) {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}",
			""
					+ "	void incompatibleTypes(String value) {\n"
					+ "		if (value.equals(\"A\")) {\n"
					+ "		} else if (value.equals(\"B\") || value.equals(3.0)) {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}",
			""
					+ "	void incompatibleTypes(Object value) {\n"
					+ "		if (value.equals(\"A\")) {\n"
					+ "		} else if (value.equals(\"B\") || value.equals(\"C\")) {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}",
	})
	void visit_ExpressionsOfNonCompatibleTypes_shouldNotTransform(String codeEexample)
			throws Exception {
		assertNoChange(codeEexample);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			""
					+ "	void differentVariables(char value, char value2) {\n"
					+ "		if (value == 'A') {\n"
					+ "		} else if (value2 == 'B') {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}",
			""
					+ "	void differentVariables(int value, int value2) {\n"
					+ "		if (value == 1) {\n"
					+ "		} else if (value2 == 2) {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}",
			""
					+ "	void differentVariables(String value, String value2) {\n"
					+ "		if (value.equals(\"A\")) {\n"
					+ "		} else if (value2.equals(\"B\")) {\n"
					+ "		} else {\n"
					+ "		}\n"
					+ "	}"
	})
	void visit_DifferentVariablesEvaluated_shouldNotTransform(String codeEexample)
			throws Exception {
		assertNoChange(codeEexample);
	}

}
