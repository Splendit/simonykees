package org.eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.ifstatement.ReplaceMultiBranchIfBySwitchASTVisitor;

class ReplaceMultiBranchIfBySwitchASTVisitorTest extends UsesJDTUnitFixture {

	private static final String STRING_CONSTANTS = ""
			+ "	static final String NEGATIVE = \"NEGATIVE\";\n"
			+ "	static final String ZERO = \"ZERO\";\n"
			+ "	static final String ONE = \"ONE\";\n"
			+ "	static final String TWO = \"TWO\";		\n"
			+ "	static final String GREATER_THAN_TWO = \"GREATER THAN TWO\";\n"
			+ "	static final String OTHER = \"OTHER\";		";

	@BeforeEach
	void setUp() {
		setDefaultVisitor(new ReplaceMultiBranchIfBySwitchASTVisitor());
		setJavaVersion(JavaCore.VERSION_14);
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_IfStatementWithEmptyStatement_shouldTransform() throws Exception {
		String original = ""
				+ "		void ifStatementWithEmptyStatements(char value) {\n"
				+ "			if (value == 'A')\n"
				+ "				;\n"
				+ "			else if (value == 'B' || value == 'C' || value == 'D')\n"
				+ "				;\n"
				+ "			else\n"
				+ "				;\n"
				+ "		}";
		String expected = ""
				+ "		void ifStatementWithEmptyStatements(char value) {\n"
				+ "			switch (value) {\n"
				+ "			case 'A' -> {\n"
				+ "				;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			case 'B', 'C', 'D' -> {\n"
				+ "				;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			default -> {\n"
				+ "				;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			}\n"
				+ "		}";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"		String result;",
			"		String result = \"\";",
			"		String result = null;",
			"		String result = stringField;"
	})
	void visit_expectInitializationWithSwitchExpression_shouldTransform(String codecContainingResultDeclaration)
			throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "	void expectInitializationWithSwitchExpression(int value) {\n"
				+ codecContainingResultDeclaration + "\n"
				+ "		if (value == 0) {\n"
				+ "			result = ZERO;\n"
				+ "		} else if (value == 1) {\n"
				+ "			result = ONE;\n"
				+ "		} else if (value == 2) {\n"
				+ "			result = TWO;\n"
				+ "		} else {\n"
				+ "			result = OTHER;\n"
				+ "		}\n"
				+ "	}";

		String expected = STRING_CONSTANTS + "\n"
				+ "	void expectInitializationWithSwitchExpression(int value) {\n"
				+ "		String result = switch (value) {\n"
				+ "		case 0 -> ZERO;\n"
				+ "		case 1 -> ONE;\n"
				+ "		case 2 -> TWO;\n"
				+ "		default -> OTHER;\n"
				+ "		};\n"
				+ "	}";

		assertChange(original, expected);
	}

	//

	/**
	 * At the moment the if statement in this test is transformed to a switch
	 * statement which is also correct but less concise than a switch
	 * expression. This test is expected to fail as soon as the if statement is
	 * transformed to a switch expression instead of a switch statement.
	 */
	@Test
	void visit_expectSwitchExpressionInitializerWithThrowStatement_shouldTransform() throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "	void expectSwitchExpressionInitializerWithThrow(int value) {\n"
				+ "		String result = ZERO;\n"
				+ "		if (value == 0) {\n"
				+ "			result = ZERO;\n"
				+ "		} else if (value == 1) {\n"
				+ "			result = ONE;\n"
				+ "		} else if (value == 2) {\n"
				+ "			result = TWO;\n"
				+ "		} else {\n"
				+ "			throw new RuntimeException();\n"
				+ "		}\n"
				+ "	}";

		String expected = STRING_CONSTANTS + "\n"
				+ "	void expectSwitchExpressionInitializerWithThrow(int value) {\n"
				+ "		String result = ZERO;\n"
				+ "		switch (value) {\n"
				+ "		case 0 -> result = ZERO;\n"
				+ "		case 1 -> result = ONE;\n"
				+ "		case 2 -> result = TWO;\n"
				+ "		default -> throw new RuntimeException();\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_expectSwitchExpressionInitializerWithYieldStatement_shouldTransform() throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "		void expectSwitchExpressionInitializerWithYield(int value) {\n"
				+ "			String result;\n"
				+ "			if (value == 0) {\n"
				+ "				System.out.println(ZERO);\n"
				+ "				result = ZERO;\n"
				+ "			} else if (value == 1) {\n"
				+ "				System.out.println(ONE);\n"
				+ "				result = ONE;\n"
				+ "			} else if (value == 2) {\n"
				+ "				System.out.println(TWO);\n"
				+ "				result = TWO;\n"
				+ "			} else {\n"
				+ "				System.out.println(OTHER);\n"
				+ "				result = OTHER;\n"
				+ "			}\n"
				+ "		}";
		String expected = STRING_CONSTANTS + "\n"
				+ "		void expectSwitchExpressionInitializerWithYield(int value) {\n"
				+ "			String result = switch (value) {\n"
				+ "			case 0 -> {\n"
				+ "				System.out.println(ZERO);\n"
				+ "				yield ZERO;\n"
				+ "			}\n"
				+ "			case 1 -> {\n"
				+ "				System.out.println(ONE);\n"
				+ "				yield ONE;\n"
				+ "			}\n"
				+ "			case 2 -> {\n"
				+ "				System.out.println(TWO);\n"
				+ "				yield TWO;\n"
				+ "			}\n"
				+ "			default -> {\n"
				+ "				System.out.println(OTHER);\n"
				+ "				yield OTHER;\n"
				+ "			}\n"
				+ "			};\n"
				+ "		}";
		assertChange(original, expected);
	}

	public static Stream<Arguments> arguments_expectAssignmentWithSwitchExpression() throws Exception {
		String multibranchIf = ""
				+ "		if (value == 0) {\n"
				+ "			result = ZERO;\n"
				+ "		} else if (value == 1) {\n"
				+ "			result = ONE;\n"
				+ "		} else if (value == 2) {\n"
				+ "			result = TWO;\n"
				+ "		} else {\n"
				+ "			result = OTHER;\n"
				+ "		}";
		String assignmentWithSwitchExpression = ""
				+ "		result = switch (value) {\n"
				+ "		case 0 -> ZERO;\n"
				+ "		case 1 -> ONE;\n"
				+ "		case 2 -> TWO;\n"
				+ "		default -> OTHER;\n"
				+ "		};";

		return Stream.of(
				Arguments.of(
						""
								+ "	void expectAssignmentWithSwitchExpression(int value) {\n"
								+ "		String result = ZERO;\n"
								+ "		System.out.println(result);\n"
								+ multibranchIf + "\n"
								+ "	}",
						""
								+ "	void expectAssignmentWithSwitchExpression(int value) {\n"
								+ "		String result = ZERO;\n"
								+ "		System.out.println(result);\n"
								+ assignmentWithSwitchExpression + "\n"
								+ "	}"),
				Arguments.of(
						""
								+ "	void expectAssignmentWithSwitchExpression(int value) {\n"
								+ "		String result = getString();\n"
								+ multibranchIf + "\n"
								+ "	}\n"
								+ "\n"
								+ "	String getString() {\n"
								+ "		return \"\";\n"
								+ "	}",
						""
								+ "	void expectAssignmentWithSwitchExpression(int value) {\n"
								+ "		String result = getString();\n"
								+ assignmentWithSwitchExpression + "\n"
								+ "	}\n"
								+ "\n"
								+ "	String getString() {\n"
								+ "		return \"\";\n"
								+ "	}"),
				Arguments.of(
						""
								+ "	String result;\n"
								+ "	void expectAssignmentWithSwitchExpression(int value) {\n"
								+ multibranchIf + "\n"
								+ "	}",
						""
								+ "	String result;\n"
								+ "	void expectAssignmentWithSwitchExpression(int value) {\n"
								+ assignmentWithSwitchExpression + "\n"
								+ "	}"),
				Arguments.of(
						""
								+ "	void expectAssignmentWithSwitchExpression(int value) {\n"
								+ "		String result, result2;\n"
								+ multibranchIf + "\n"
								+ "	}",
						""
								+ "	void expectAssignmentWithSwitchExpression(int value) {\n"
								+ "		String result, result2;\n"
								+ assignmentWithSwitchExpression + "\n"
								+ "	}"),
				Arguments.of(
						""
								+ "	void expectAssignmentWithSwitchExpression(int value) {\n"
								+ "		String result;\n"
								+ "		String result1;\n"
								+ multibranchIf + "\n"
								+ "	}",
						""
								+ "	void expectAssignmentWithSwitchExpression(int value) {\n"
								+ "		String result;\n"
								+ "		String result1;\n"
								+ assignmentWithSwitchExpression + "\n"
								+ "	}"),
				Arguments.of(
						""
								+ "	void expectAssignmentWithSwitchExpression(int value) {\n"
								+ "		String result;\n"
								+ "		{\n"
								+ multibranchIf + "\n"
								+ "		}\n"
								+ "	}",
						""
								+ "	void expectAssignmentWithSwitchExpression(int value) {\n"
								+ "		String result;\n"
								+ "		{\n"
								+ assignmentWithSwitchExpression + "\n"
								+ "		}\n"
								+ "	}"));

	}

	@ParameterizedTest
	@MethodSource("arguments_expectAssignmentWithSwitchExpression")
	void visit_expectAssignmentWithSwitchExpression_shouldTransform(String codeExampleBefore, String codeExampleAfter)
			throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ codeExampleBefore;

		String expected = STRING_CONSTANTS + "\n"
				+ codeExampleAfter;

		assertChange(original, expected);
	}

	/**
	 * The transformation result in this test is valid Java code although the
	 * resulting assignment with the switch expression is not wrapped into a
	 * block. However, it is desirable to wrap such a transformation result into
	 * a block.
	 * <p>
	 * This test is expected to fail as soon as the corresponding improvement
	 * has been implemented.
	 */
	@Test
	void visit_expectAssignmentWithSwitchExpressionInBlock_shouldTransform() throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "		void expectAssignmentWithSwitchExpression(int value) {\n"
				+ "			String result;\n"
				+ "			if (value < 0) {\n"
				+ "				result = NEGATIVE;\n"
				+ "			} else if (value == 0) {\n"
				+ "				result = ZERO;\n"
				+ "			} else if (value == 1) {\n"
				+ "				result = ONE;\n"
				+ "			} else if (value == 2) {\n"
				+ "				result = TWO;\n"
				+ "			} else {\n"
				+ "				result = GREATER_THAN_TWO;\n"
				+ "			}\n"
				+ "		}";

		String expected = STRING_CONSTANTS + "\n"
				+ "		void expectAssignmentWithSwitchExpression(int value) {\n"
				+ "			String result;\n"
				+ "			if (value < 0) {\n"
				+ "				result = NEGATIVE;\n"
				+ "			} else\n"
				+ "				result = switch (value) {\n"
				+ "				case 0 -> ZERO;\n"
				+ "				case 1 -> ONE;\n"
				+ "				case 2 -> TWO;\n"
				+ "				default -> GREATER_THAN_TWO;\n"
				+ "				};\n"
				+ "		}";

		assertChange(original, expected);
	}

	// TODO:
	@Test
	void visit_expectAssignmentWithSwitchExpressionToFormalParameter_shouldTransform() throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "	void expectSwitchExpressionAssignedToFieldAccess(int value, String result) {\n"
				+ "		if (value == 0) {\n"
				+ "			result = ZERO;\n"
				+ "		} else if (value == 1) {\n"
				+ "			result = ONE;\n"
				+ "		} else if (value == 2) {\n"
				+ "			result = TWO;\n"
				+ "		} else {\n"
				+ "			result = OTHER;\n"
				+ "		}\n"
				+ "	}";

		String expected = STRING_CONSTANTS + "\n"
				+ "	void expectSwitchExpressionAssignedToFieldAccess(int value, String result) {\n"
				+ "		result = switch (value) {\n"
				+ "		case 0 -> ZERO;\n"
				+ "		case 1 -> ONE;\n"
				+ "		case 2 -> TWO;\n"
				+ "		default -> OTHER;\n"
				+ "		};\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_expectSwitchExpressionAssignedToFieldAccess_shouldTransform()
			throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "		String result;\n"
				+ "		void expectSwitchExpressionAssignedToFieldAccess(int value) {\n"
				+ "			if (value == 0) {\n"
				+ "				this.result = ZERO;\n"
				+ "			} else if (value == 1) {\n"
				+ "				this.result = ONE;\n"
				+ "			} else if (value == 2) {\n"
				+ "				this.result = TWO;\n"
				+ "			} else {\n"
				+ "				this.result = OTHER;\n"
				+ "			}\n"
				+ "		}";

		String expected = STRING_CONSTANTS + "\n"
				+ "		String result;\n"
				+ "		void expectSwitchExpressionAssignedToFieldAccess(int value) {\n"
				+ "			this.result = switch (value) {\n"
				+ "			case 0 -> ZERO;\n"
				+ "			case 1 -> ONE;\n"
				+ "			case 2 -> TWO;\n"
				+ "			default -> OTHER;\n"
				+ "			};\n"
				+ "		}";

		assertChange(original, expected);
	}

	public static Stream<Arguments> arguments_expectSwitchStatementWithMultipleAssignments() throws Exception {

		return Stream.of(
				Arguments.of(
						""
								+ "		String result2;\n"
								+ "		if (value == 0) {\n"
								+ "			result = ZERO;\n"
								+ "			result2 = ZERO;\n"
								+ "		} else if (value == 1) {\n"
								+ "			result = ONE;\n"
								+ "		} else if (value == 2) {\n"
								+ "			result = TWO;\n"
								+ "		} else {\n"
								+ "			result = OTHER;\n"
								+ "		}",
						""
								+ "		String result2;\n"
								+ "		switch (value) {\n"
								+ "		case 0 -> {\n"
								+ "			result = ZERO;\n"
								+ "			result2 = ZERO;\n"
								+ "			break;\n"
								+ "		}\n"
								+ "		case 1 -> result = ONE;\n"
								+ "		case 2 -> result = TWO;\n"
								+ "		default -> result = OTHER;\n"
								+ "		}"),
				Arguments.of(
						""
								+ "		if (value == 0) {\n"
								+ "			result = ZERO;\n"
								+ "		} else if (value == 1) {\n"
								+ "			result = ONE;\n"
								+ "		} else if (value == 2) {\n"
								+ "			result = TWO;\n"
								+ "		}",
						""
								+ "		switch (value) {\n"
								+ "		case 0 -> result = ZERO;\n"
								+ "		case 1 -> result = ONE;\n"
								+ "		case 2 -> result = TWO;\n"
								+ "		}"),

				Arguments.of(
						""
								+ "		if (value == 0) {\n"
								+ "			result = ZERO;\n"
								+ "			System.out.println(ZERO);\n"
								+ "		} else if (value == 1) {\n"
								+ "			result = ONE;\n"
								+ "		} else if (value == 2) {\n"
								+ "			result = TWO;\n"
								+ "		} else {\n"
								+ "			result = OTHER;\n"
								+ "		}",
						""
								+ "		switch (value) {\n"
								+ "		case 0 -> {\n"
								+ "			result = ZERO;\n"
								+ "			System.out.println(ZERO);\n"
								+ "			break;\n"
								+ "		}\n"
								+ "		case 1 -> result = ONE;\n"
								+ "		case 2 -> result = TWO;\n"
								+ "		default -> result = OTHER;\n"
								+ "		}"),

				Arguments.of(
						""
								+ "		if (value == 0) {\n"
								+ "			result = ZERO;\n"
								+ "		} else if (value == 1) {\n"
								+ "			result = ONE;\n"
								+ "		} else if (value == 2) {\n"
								+ "			result = TWO;\n"
								+ "		} else {\n"
								+ "			System.out.println(NEGATIVE);\n"
								+ "			if (value < 0) {\n"
								+ "				return NEGATIVE;\n"
								+ "			}\n"
								+ "			result = GREATER_THAN_TWO;\n"
								+ "		}",
						""
								+ "		switch (value) {\n"
								+ "		case 0 -> result = ZERO;\n"
								+ "		case 1 -> result = ONE;\n"
								+ "		case 2 -> result = TWO;\n"
								+ "		default -> {\n"
								+ "			System.out.println(NEGATIVE);\n"
								+ "			if (value < 0) {\n"
								+ "				return NEGATIVE;\n"
								+ "			}\n"
								+ "			result = GREATER_THAN_TWO;\n"
								+ "			break;\n"
								+ "		}\n"
								+ "		}"),
				Arguments.of(
						""
								+ "		result = \"result: \";\n"
								+ "		if (value == 0) {\n"
								+ "			result += ZERO;\n"
								+ "		} else if (value == 1) {\n"
								+ "			result += ONE;\n"
								+ "		} else if (value == 2) {\n"
								+ "			result += TWO;\n"
								+ "		} else {\n"
								+ "			result += OTHER;\n"
								+ "		}",
						""
								+ "		result = \"result: \";\n"
								+ "		switch (value) {\n"
								+ "		case 0 -> result += ZERO;\n"
								+ "		case 1 -> result += ONE;\n"
								+ "		case 2 -> result += TWO;\n"
								+ "		default -> result += OTHER;\n"
								+ "		}")

		);

	}

	@ParameterizedTest
	@MethodSource("arguments_expectSwitchStatementWithMultipleAssignments")
	void visit_expectSwitchStatementWithMultipleAssignments_shouldTransform(String codeExampleBefore,
			String codeExampleAfter) throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "	String expectSwitchStatementWithMultipleAssignments(int value) {\n"
				+ "		String result;\n"
				+ codeExampleBefore + "\n"
				+ "\n"
				+ "		System.out.println(result);\n"
				+ "		return result;\n"
				+ "	}";
		String expected = STRING_CONSTANTS + "\n"
				+ "	String expectSwitchStatementWithMultipleAssignments(int value) {\n"
				+ "		String result;\n"
				+ codeExampleAfter + "\n"
				+ "\n"
				+ "		System.out.println(result);\n"
				+ "		return result;\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_expectReturnSwitchExpression_shouldTransform() throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "	String expectReturnSwitchExpression(int value) {\n"
				+ "		if (value == 0) {\n"
				+ "			return ZERO;\n"
				+ "		} else if (value == 1) {\n"
				+ "			return ONE;\n"
				+ "		} else if (value == 2) {\n"
				+ "			return TWO;\n"
				+ "		} else {\n"
				+ "			return OTHER;\n"
				+ "		}\n"
				+ "	}";
		String expected = STRING_CONSTANTS + "\n"
				+ "		String expectReturnSwitchExpression(int value) {\n"
				+ "			return switch (value) {\n"
				+ "			case 0 -> ZERO;\n"
				+ "			case 1 -> ONE;\n"
				+ "			case 2 -> TWO;\n"
				+ "			default -> OTHER;\n"
				+ "			};\n"
				+ "		}";

		assertChange(original, expected);
	}

	@Test
	void visit_expectReturnSwitchExpressionWithThrow_shouldTransform() throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "	String expectReturnSwitchExpression(int value) {\n"
				+ "		if (value == 0) {\n"
				+ "			return ZERO;\n"
				+ "		} else if (value == 1) {\n"
				+ "			return ONE;\n"
				+ "		} else if (value == 2) {\n"
				+ "			return TWO;\n"
				+ "		} else {\n"
				+ "			throw new RuntimeException();\n"
				+ "		}\n"
				+ "	}";
		String expected = STRING_CONSTANTS + "\n"
				+ "	String expectReturnSwitchExpression(int value) {\n"
				+ "		return switch (value) {\n"
				+ "		case 0 -> ZERO;\n"
				+ "		case 1 -> ONE;\n"
				+ "		case 2 -> TWO;\n"
				+ "		default -> throw new RuntimeException();\n"
				+ "		};\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_expectReturnSwitchExpressionWithYield_shouldTransform() throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "	String expectReturnSwitchExpression(int value) {\n"
				+ "		if (value == 0) {\n"
				+ "			System.out.println(ZERO);\n"
				+ "			return ZERO;\n"
				+ "		} else if (value == 1) {\n"
				+ "			System.out.println(ONE);\n"
				+ "			return ONE;\n"
				+ "		} else if (value == 2) {\n"
				+ "			System.out.println(TWO);\n"
				+ "			return TWO;\n"
				+ "		} else {\n"
				+ "			System.out.println(OTHER);\n"
				+ "			return OTHER;\n"
				+ "		}\n"
				+ "	}";
		String expected = STRING_CONSTANTS + "\n"
				+ "	String expectReturnSwitchExpression(int value) {\n"
				+ "		return switch (value) {\n"
				+ "		case 0 -> {\n"
				+ "			System.out.println(ZERO);\n"
				+ "			yield ZERO;\n"
				+ "		}\n"
				+ "		case 1 -> {\n"
				+ "			System.out.println(ONE);\n"
				+ "			yield ONE;\n"
				+ "		}\n"
				+ "		case 2 -> {\n"
				+ "			System.out.println(TWO);\n"
				+ "			yield TWO;\n"
				+ "		}\n"
				+ "		default -> {\n"
				+ "			System.out.println(OTHER);\n"
				+ "			yield OTHER;\n"
				+ "		}\n"
				+ "		};\n"
				+ "	}";

		assertChange(original, expected);
	}

	public static Stream<Arguments> arguments_expectSwitchStatementWithMultipleReturn() throws Exception {

		return Stream.of(
				Arguments.of(
						""
								+ "		if (value == 0) {\n"
								+ "			return ZERO;\n"
								+ "		} else if (value == 1) {\n"
								+ "			return ONE;\n"
								+ "		} else if (value == 2) {\n"
								+ "			return TWO;\n"
								+ "		} else {\n"
								+ "			System.out.println(OTHER);\n"
								+ "			if (value < 0) {\n"
								+ "				return NEGATIVE;\n"
								+ "			}\n"
								+ "			return OTHER;\n"
								+ "		}",
						""
								+ "		switch (value) {\n"
								+ "		case 0 -> {\n"
								+ "			return ZERO;\n"
								+ "		}\n"
								+ "		case 1 -> {\n"
								+ "			return ONE;\n"
								+ "		}\n"
								+ "		case 2 -> {\n"
								+ "			return TWO;\n"
								+ "		}\n"
								+ "		default -> {\n"
								+ "			System.out.println(OTHER);\n"
								+ "			if (value < 0) {\n"
								+ "				return NEGATIVE;\n"
								+ "			}\n"
								+ "			return OTHER;\n"
								+ "		}\n"
								+ "		}"),
				Arguments.of(
						""
								+ "		if (value == 0) {\n"
								+ "			return ZERO;\n"
								+ "		} else if (value == 1) {\n"
								+ "			return ONE;\n"
								+ "		} else if (value == 2) {\n"
								+ "			return TWO;\n"
								+ "		} else {\n"
								+ "			System.out.println(OTHER);\n"
								+ "			if (value < 0) {\n"
								+ "				return NEGATIVE;\n"
								+ "			} else {\n"
								+ "				return GREATER_THAN_TWO;\n"
								+ "			}\n"
								+ "		}",
						""
								+ "		switch (value) {\n"
								+ "		case 0 -> {\n"
								+ "			return ZERO;\n"
								+ "		}\n"
								+ "		case 1 -> {\n"
								+ "			return ONE;\n"
								+ "		}\n"
								+ "		case 2 -> {\n"
								+ "			return TWO;\n"
								+ "		}\n"
								+ "		default -> {\n"
								+ "			System.out.println(OTHER);\n"
								+ "			if (value < 0) {\n"
								+ "				return NEGATIVE;\n"
								+ "			} else {\n"
								+ "				return GREATER_THAN_TWO;\n"
								+ "			}\n"
								+ "		}\n"
								+ "		}"),

				Arguments.of(
						""
								+ "		if (value == 0) {\n"
								+ "			return ZERO;\n"
								+ "		} else if (value == 1) {\n"
								+ "			return ONE;\n"
								+ "		} else if (value == 2) {\n"
								+ "			return TWO;\n"
								+ "		} else {\n"
								+ "			\n"
								+ "		}\n"
								+ "		\n"
								+ "		System.out.println(OTHER);\n"
								+ "		return OTHER;",
						""
								+ "		switch (value) {\n"
								+ "		case 0 -> {\n"
								+ "			return ZERO;\n"
								+ "		}\n"
								+ "		case 1 -> {\n"
								+ "			return ONE;\n"
								+ "		}\n"
								+ "		case 2 -> {\n"
								+ "			return TWO;\n"
								+ "		}\n"
								+ "		default -> {\n"
								+ "			break;\n"
								+ "		}\n"
								+ "		}\n"
								+ "		\n"
								+ "		System.out.println(OTHER);\n"
								+ "		return OTHER;"),

				Arguments.of(
						""
								+ "		if (value == 0) {\n"
								+ "			return ZERO;\n"
								+ "		} else if (value == 1) {\n"
								+ "			return ONE;\n"
								+ "		} else if (value == 2) {\n"
								+ "			return TWO;\n"
								+ "		}\n"
								+ "		System.out.println(OTHER);\n"
								+ "		return OTHER;",
						""
								+ "		switch (value) {\n"
								+ "		case 0 -> {\n"
								+ "			return ZERO;\n"
								+ "		}\n"
								+ "		case 1 -> {\n"
								+ "			return ONE;\n"
								+ "		}\n"
								+ "		case 2 -> {\n"
								+ "			return TWO;\n"
								+ "		}\n"
								+ "		}\n"
								+ "		System.out.println(OTHER);\n"
								+ "		return OTHER;"));
	}

	@ParameterizedTest
	@MethodSource("arguments_expectSwitchStatementWithMultipleReturn")
	void visit_expectSwitchStatementWithMultipleReturn_shouldTransform(String originalCode, String expectedCode)
			throws Exception {

		String original = STRING_CONSTANTS + "\n"
				+ "		String expectSwitchStatementWithMultipleReturn(int value) {\n"
				+ originalCode + "\n"
				+ "		}";

		String expected = STRING_CONSTANTS + "\n"
				+ "		String expectSwitchStatementWithMultipleReturn(int value) {\n"
				+ expectedCode + "\n"
				+ "		}";

		assertChange(original, expected);
	}

	@Test
	void visit_assignmentToEvaluatedVariable_shouldTransform() throws Exception {
		String original = ""
				+ "	public static void test() {\n"
				+ "		int i = 0;\n"
				+ "\n"
				+ "		if (i == 0) {\n"
				+ "			i = 1;\n"
				+ "		} else if (i == 1) {\n"
				+ "			i = 2;\n"
				+ "		} else if (i == 2) {\n"
				+ "			i = 3;\n"
				+ "		} else {\n"
				+ "			i = 0;\n"
				+ "		}\n"
				+ "	}";

		String expected = ""
				+ "	public static void test() {\n"
				+ "		int i = 0;\n"
				+ "\n"
				+ "		i = switch (i) {\n"
				+ "		case 0 -> 1;\n"
				+ "		case 1 -> 2;\n"
				+ "		case 2 -> 3;\n"
				+ "		default -> 0;\n"
				+ "		};\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_noEqualsOperationFound_shouldNotTransform() throws Exception {

		String original = ""
				+ "	void noEqualsOperationFound(boolean condition1, boolean condition2) {\n"
				+ "		if (condition1) {\n"
				+ "		} else if (condition2) {\n"
				+ "		} else {\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			""
					+ "		if (value == 1) {\n"
					+ "		} else {\n"
					+ "		}",
			""
					+ "		if (value == 1) {\n"
					+ "		} else if (value == 2) {\n"
					+ "		}"
	})
	void visit_lessThanThreeBranches_shouldNotTransform(String ifStatement) throws Exception {

		String original = ""
				+ "	void ifStatementWithLessThanThreeBranches(int value) {\n"
				+ ifStatement + "\n"
				+ "	}";

		assertNoChange(original);
	}

	/**
	 * Due to possible future improvements of the rule, the code examples in
	 * this test may be transformed, resulting in a test failure.
	 */
	@Test
	void visit_subsequentIfStatementsWithReturn_shouldNotTransform() throws Exception {

		String original = STRING_CONSTANTS + "\n"
				+ "	String subsequentIfStatementsWithReturn(int value) {\n"
				+ "		if (value == 0) {\n"
				+ "			return ZERO;\n"
				+ "		}\n"
				+ "		if (value == 1) {\n"
				+ "			return ONE;\n"
				+ "		}\n"
				+ "		if (value == 2) {\n"
				+ "			return TWO;\n"
				+ "		}\n"
				+ "		return OTHER;\n"
				+ "	}";

		assertNoChange(original);
	}
}
