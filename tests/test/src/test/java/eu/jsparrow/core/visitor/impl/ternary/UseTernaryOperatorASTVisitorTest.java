package eu.jsparrow.core.visitor.impl.ternary;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;

@SuppressWarnings({ "nls" })
public class UseTernaryOperatorASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setDefaultVisitor(new UseTernaryOperatorASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"", "0", "valueWhenFalse", "Integer.MIN_VALUE"
	})
	void visit_replaceWithTernaryAsInitializer_shouldTransform(String initializer) throws Exception {
		String variableDeclaration;
		if (initializer.isEmpty()) {
			variableDeclaration = "int x;";
		} else {
			variableDeclaration = "int x = " + initializer + ";";
		}
		String original = ""
				+ "	void test(boolean condition, int valueWhenTrue, int valueWhenFalse) {\n"
				+ "		" + variableDeclaration + "\n"
				+ "		if (condition) {\n"
				+ "			x = valueWhenTrue;\n"
				+ "		} else {\n"
				+ "			x = valueWhenFalse;\n"
				+ "		}\n"
				+ "	}";
		String expected = ""
				+ "	void test(boolean condition, int valueWhenTrue, int valueWhenFalse) {\n"
				+ "		int x = condition ? valueWhenTrue : valueWhenFalse;\n"
				+ "	}";

		assertChange(original, expected);
	}

	public static Stream<Arguments> arguments_replaceWithTernaryAsAssignmentRightHandSide() throws Exception {
		return Stream.of(
				Arguments.of(
						""
								+ "	void test() {\n"
								+ "		int i = 1;\n"
								+ "		if (i == 1) {\n"
								+ "			i = 10;\n"
								+ "		} else {\n"
								+ "			i = 0;\n"
								+ "		}\n"
								+ "	}",
						""
								+ "	void test() {\n"
								+ "		int i = 1;\n"
								+ "		i = i == 1 ? 10 : 0;\n"
								+ "	}"),
				Arguments.of(
						""
								+ "	void test(boolean condition) {\n"
								+ "		int x = 0;\n"
								+ "		if (condition) {\n"
								+ "			x = x;\n"
								+ "		} else {\n"
								+ "			x = 0;\n"
								+ "		}\n"
								+ "	}",
						""
								+ "	void test(boolean condition) {\n"
								+ "		int x = 0;\n"
								+ "		x = condition ? x : 0;\n"
								+ "	}"),
				Arguments.of(
						""
								+ "	void test(boolean condition) {\n"
								+ "		int x = 0;\n"
								+ "		if (condition) {\n"
								+ "			x = 1;\n"
								+ "		} else {\n"
								+ "			x = x;\n"
								+ "		}\n"
								+ "	}",
						""
								+ "	void test(boolean condition) {\n"
								+ "		int x = 0;\n"
								+ "		x = condition ? 1 : x;\n"
								+ "	}"),
				Arguments.of(
						""
								+ "	int getInitialX() {\n"
								+ "		return 0;\n"
								+ "	}"
								+ "	void test(boolean condition) {\n"
								+ "		int x = getInitialX();\n"
								+ "		if (condition) {\n"
								+ "			x = 1;\n"
								+ "		} else {\n"
								+ "			x = 0;\n"
								+ "		}\n"
								+ "	}",
						""
								+ "	int getInitialX() {\n"
								+ "		return 0;\n"
								+ "	}"
								+ "	void test(boolean condition) {\n"
								+ "		int x = getInitialX();\n"
								+ "		x = condition ? 1 : 0;\n"
								+ "	}"),
				Arguments.of(
						""
								+ "	void test(boolean condition) {\n"
								+ "		int x = 0;\n"
								+ "		if (condition) {\n"
								+ "			x += 1;\n"
								+ "		} else {\n"
								+ "			x += 0;\n"
								+ "		}\n"
								+ "	}",
						""
								+ "	void test(boolean condition) {\n"
								+ "		int x = 0;\n"
								+ "		x += condition ? 1 : 0;\n"
								+ "	}"),
				Arguments.of(
						""
								+ "	void test(boolean condition) {\n"
								+ "		int x;\n"
								+ "		int y;\n"
								+ "		if (condition) {\n"
								+ "			x = 1;\n"
								+ "		} else {\n"
								+ "			x = 0;\n"
								+ "		}\n"
								+ "	}",
						""
								+ "	void test(boolean condition) {\n"
								+ "		int x;\n"
								+ "		int y;\n"
								+ "		x = condition ? 1 : 0;\n"
								+ "	}"),
				Arguments.of(
						""
								+ "	void test(boolean condition) {\n"
								+ "		int x, y;\n"
								+ "		if (condition) {\n"
								+ "			x = 1;\n"
								+ "		} else {\n"
								+ "			x = 0;\n"
								+ "		}\n"
								+ "	}",
						""
								+ "	void test(boolean condition) {\n"
								+ "		int x, y;\n"
								+ "		x = condition ? 1 : 0;\n"
								+ "	}"),
				Arguments.of(
						""
								+ "	void test(boolean condition1, boolean condition2) {\n"
								+ "		int x;\n"
								+ "		if (condition1) if (condition2) {\n"
								+ "			x = 1;\n"
								+ "		} else {\n"
								+ "			x = 0;\n"
								+ "		}\n"
								+ "	}",
						""
								+ "	void test(boolean condition1, boolean condition2) {\n"
								+ "		int x;\n"
								+ "		if (condition1) x = condition2 ? 1 : 0;\n"
								+ "	}"),
				Arguments.of(
						""
								+ "	int x;\n"
								+ "	void test(boolean condition) {\n"
								+ "		if (condition) {\n"
								+ "			x = 1;\n"
								+ "		} else {\n"
								+ "			x = 0;\n"
								+ "		}\n"
								+ "	}",
						""
								+ "	int x;\n"
								+ "	void test(boolean condition) {\n"
								+ "		x = condition ? 1 : 0;\n"
								+ "	}"),
				Arguments.of(
						""
								+ "	int x;\n"
								+ "	void test(boolean condition) {\n"
								+ "		if (condition) {\n"
								+ "			this.x = 1;\n"
								+ "		} else {\n"
								+ "			this.x = 0;\n"
								+ "		}\n"
								+ "	}",
						""
								+ "	int x;\n"
								+ "	void test(boolean condition) {\n"
								+ "		this.x = condition ? 1 : 0;\n"
								+ "	}"));

	}

	@ParameterizedTest
	@MethodSource("arguments_replaceWithTernaryAsAssignmentRightHandSide")
	void visit_replaceWithTernaryAsAssignmentRightHandSide_shouldTransform(String original, String expected)
			throws Exception {
		assertChange(original, expected);
	}

	@Test
	void visit_firstExampleWithReturn_shouldTransform() throws Exception {
		String original = ""
				+ "	int test() {\n"
				+ "		boolean condition = true;\n"
				+ "		if (condition) {\n"
				+ "			return 1;\n"
				+ "		} else {\n"
				+ "			return 0;\n"
				+ "		}\n"
				+ "	}";
		String expected = ""
				+ "	int test() {\n"
				+ "		boolean condition = true;\n"
				+ " 	return condition ? 1 : 0;\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_exampleWithReturnInsteadOfElse_shouldTransform() throws Exception {
		String original = ""
				+ "	int test() {\n"
				+ "		boolean condition = true;\n"
				+ "		if (condition) {\n"
				+ "			return 1;\n"
				+ "		}\n"
				+ "		return 0;\n"
				+ "	}";
		String expected = ""
				+ "	int test() {\n"
				+ "		boolean condition = true;\n"
				+ " 	return condition ? 1 : 0;\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_AssignmentWithLongestPossibleKeyword_shouldTransform() throws Exception {
		String largestPossibleIdentifier = "p123456789_123456789";
		String original = ""
				+ "		void test(int " + largestPossibleIdentifier + ") {\n"
				+ "			boolean condition = true;\n"
				+ "			int x;\n"
				+ "			if (condition) {\n"
				+ "				x = " + largestPossibleIdentifier + ";\n"
				+ "			} else {\n"
				+ "				x = 0;\n"
				+ "			}\n"
				+ "		}";

		String expected = ""
				+ "		void test(int " + largestPossibleIdentifier + ") {\n"
				+ "			boolean condition = true;\n"
				+ "			int x = condition ? " + largestPossibleIdentifier + " : 0;\n"
				+ "		}";

		assertChange(original, expected);
	}
}
