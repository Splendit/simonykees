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

	/**
	 * At the moment the if statement in this test is transformed to the
	 * assignment of a switch expression, but this produces invalid code. This
	 * test is expected to fail as soon as the corner case has been fixed.
	 */
	@Test
	void visit_ConditionalReturnBeforeLastAssignment_shouldTransformToSwitchStatement() throws Exception {
		String original = ""
				+ "		String result;\n"
				+ "		String assignToFieldByIf(int value) {\n"
				+ "			if (value == 0) {\n"
				+ "				result = \"ZERO\";\n"
				+ "			} else if (value == 1) {\n"
				+ "				result = \"ONE\";\n"
				+ "			} else if (value == 2) {\n"
				+ "				result = \"TWO\";\n"
				+ "			} else {\n"
				+ "				if (value < 0) {\n"
				+ "					return \"NEGATIVE\";\n"
				+ "				}\n"
				+ "				result = \"GREATER THAN TWO\";\n"
				+ "			}\n"
				+ "			return result;\n"
				+ "		}";

		String expected = ""
				+ "		String result;\n"
				+ "		String assignToFieldByIf(int value) {\n"
				+ "			result = switch (value) {\n"
				+ "			case 0 -> \"ZERO\";\n"
				+ "			case 1 -> \"ONE\";\n"
				+ "			case 2 -> \"TWO\";\n"
				+ "			default -> {\n"
				+ "				if (value < 0) {\n"
				+ "					return \"NEGATIVE\";\n"
				+ "				}\n"
				+ "				yield \"GREATER THAN TWO\";\n"
				+ "			}\n"
				+ "			};\n"
				+ "			return result;\n"
				+ "		}";

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
