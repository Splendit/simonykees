package org.eu.jsparrow.rules.java16.switchexpression.ifstatement;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.ifstatement.ReplaceMultiBranchIfBySwitchASTVisitor;

class ReplaceMultiBranchIfBySwitchASTVisitorTest extends UsesJDTUnitFixture {

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
	void visit_baseCase_shouldTransform() throws Exception {
		String original = ""
				+ "	void exampleWithSystemOutPrintln(String value) {\n"
				+ "		if (value.equals(\"a\")) {\n"
				+ "			System.out.println(1);\n"
				+ "		} else if (value.equals(\"b\")) {\n"
				+ "			System.out.println(2);\n"
				+ "		} else {\n"
				+ "			System.out.println(3);\n"
				+ "		}\n"
				+ "	}";
		String expected = ""
				+ "	void exampleWithSystemOutPrintln(String value) {\n"
				+ "		switch (value) {\n"
				+ "		case \"a\" -> System.out.println(1);\n"
				+ "		case \"b\" -> System.out.println(2);\n"
				+ "		default -> System.out.println(3);\n"
				+ "		}\n"
				+ "	}";
		assertChange(original, expected);
	}

	@Test
	void visit_ifStatementWithConditionalOr_shouldTransform() throws Exception {
		String original = ""
				+ "	void ifStatementWithConditionalOr(String value) {\n"
				+ "		if (value.equals(\"a1\") || value.equals(\"a2\") || value.equals(\"a3\")) {\n"
				+ "			System.out.println(1);\n"
				+ "		} else if (value.equals(\"b1\") || value.equals(\"b2\")) {\n"
				+ "			System.out.println(2);\n"
				+ "		} else {\n"
				+ "			System.out.println(3);\n"
				+ "		}\n"
				+ "	}";
		String expected = ""
				+ "	void ifStatementWithConditionalOr(String value) {\n"
				+ "		switch (value) {\n"
				+ "		case \"a1\", \"a2\", \"a3\" -> System.out.println(1);\n"
				+ "		case \"b1\", \"b2\" -> System.out.println(2);\n"
				+ "		default -> System.out.println(3);\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_ifStatementWithNumericLiterals_shouldTransform() throws Exception {
		String original = ""
				+ "	void ifStatementWithNumericLiterals(int value) {\n"
				+ "		if (value == 1 || value == 2) {\n"
				+ "			System.out.println(\"1 or 2\");\n"
				+ "		} else if (value == 3 || value == 4 || value == 5) {\n"
				+ "			System.out.println(\"3 or 4 or 5\");\n"
				+ "		} else if (value == 7) {\n"
				+ "			System.out.println(\"7\");\n"
				+ "		} else {\n"
				+ "			System.out.println(\"other\");\n"
				+ "		}\n"
				+ "	}";
		String expected = ""
				+ "	void ifStatementWithNumericLiterals(int value) {\n"
				+ "		switch (value) {\n"
				+ "		case 1, 2 -> System.out.println(\"1 or 2\");\n"
				+ "		case 3, 4, 5 -> System.out.println(\"3 or 4 or 5\");\n"
				+ "		case 7 -> System.out.println(\"7\");\n"
				+ "		default -> System.out.println(\"other\");\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_SpecialCaseWithVariableInitialization_shouldTransform() throws Exception {
		String original = ""
				+ "	void specialCaseWithInitialization(int value) {\n"
				+ "		String result = \"\";\n"
				+ "		if (value == 0) {\n"
				+ "			result = \"ZERO\";\n"
				+ "		} else if (value == 1) {\n"
				+ "			result = \"ONE\";\n"
				+ "		} else if (value == 2) {\n"
				+ "			result = \"TWO\";\n"
				+ "		} else {\n"
				+ "			result = \"< ZERO or > TWO\";\n"
				+ "		}\n"
				+ "	}";
		String expected = ""
				+ "	void specialCaseWithInitialization(int value) {\n"
				+ "		String result = switch (value) {\n"
				+ "		case 0 -> \"ZERO\";\n"
				+ "		case 1 -> \"ONE\";\n"
				+ "		case 2 -> \"TWO\";\n"
				+ "		default -> \"< ZERO or > TWO\";\n"
				+ "		};\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_SpecialCaseWithReAssignmentAfterUse_shouldTransform() throws Exception {
		String original = ""
				+ "	void specialCaseWithReAssignmentAfterUse(int value) {\n"
				+ "		String result = \"\";\n"
				+ "		System.out.println(result);\n"
				+ "		if (value == 0) {\n"
				+ "			result = \"ZERO\";\n"
				+ "		} else if (value == 1) {\n"
				+ "			result = \"ONE\";\n"
				+ "		} else if (value == 2) {\n"
				+ "			result = \"TWO\";\n"
				+ "		} else {\n"
				+ "			result = \"< ZERO or > TWO\";\n"
				+ "		}\n"
				+ "	}";

		String expected = ""
				+ "	void specialCaseWithReAssignmentAfterUse(int value) {\n"
				+ "		String result = \"\";\n"
				+ "		System.out.println(result);\n"
				+ "		result = switch (value) {\n"
				+ "		case 0 -> \"ZERO\";\n"
				+ "		case 1 -> \"ONE\";\n"
				+ "		case 2 -> \"TWO\";\n"
				+ "		default -> \"< ZERO or > TWO\";\n"
				+ "		};\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_SpecialCaseWithAssignmentsWithoutElse_shouldTransform() throws Exception {
		String original = ""
				+ "	void specialCaseWithAssignmentWithoutElse(int value) {\n"
				+ "		String result = \"\";\n"
				+ "		if (value == 0) {\n"
				+ "			result = \"ZERO\";\n"
				+ "		} else if (value == 1) {\n"
				+ "			result = \"ONE\";\n"
				+ "		} else if (value == 2) {\n"
				+ "			result = \"TWO\";\n"
				+ "		} \n"
				+ "	}";
		String expected = ""
				+ "	void specialCaseWithAssignmentWithoutElse(int value) {\n"
				+ "		String result = \"\";\n"
				+ "		switch (value) {\n"
				+ "		case 0 -> result = \"ZERO\";\n"
				+ "		case 1 -> result = \"ONE\";\n"
				+ "		case 2 -> result = \"TWO\";\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_SpecialCaseWithReturnStatements_shouldTransform() throws Exception {
		String original = ""
				+ "	String specialCaseWithReturn(int value) {\n"
				+ "		if (value == 0) {\n"
				+ "			return \"ZERO\";\n"
				+ "		} else if (value == 1) {\n"
				+ "			return \"ONE\";\n"
				+ "		} else if (value == 2) {\n"
				+ "			return \"TWO\";\n"
				+ "		} else {\n"
				+ "			return \"< ZERO or > TWO\";\n"
				+ "		}\n"
				+ "	}";
		String expected = ""
				+ "	String specialCaseWithReturn(int value) {\n"
				+ "		return switch (value) {\n"
				+ "		case 0 -> \"ZERO\";\n"
				+ "		case 1 -> \"ONE\";\n"
				+ "		case 2 -> \"TWO\";\n"
				+ "		default -> \"< ZERO or > TWO\";\n"
				+ "		};\n"
				+ "	}";

		assertChange(original, expected);
	}
}
