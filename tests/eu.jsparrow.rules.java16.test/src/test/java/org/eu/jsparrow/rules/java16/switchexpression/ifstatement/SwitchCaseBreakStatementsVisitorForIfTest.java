package org.eu.jsparrow.rules.java16.switchexpression.ifstatement;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.ifstatement.ReplaceMultiBranchIfBySwitchASTVisitor;

/**
 * Tests the {@link ReplaceMultiBranchIfBySwitchASTVisitor} but with a focus on
 * the code coverage for the type {@code SwitchCaseBreakStatementsVisitor}.
 *
 */
class SwitchCaseBreakStatementsVisitorForIfTest extends UsesJDTUnitFixture {

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
	void visit_breakForStatementEnclosingIfStatement_shouldNotTransform() throws Exception {
		String original = ""
				+ "	void breakForStatementEnclosingIfStatement(int value) {\n"
				+ "		for (;;) {\n"
				+ "			if (value == 1) {\n"
				+ "				break;\n"
				+ "			} else if (value == 2) {\n"
				+ "			} else {\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			""
					+ "			while (true) {\n"
					+ "				break;\n"
					+ "			}",
			""
					+ "			for (;;) {\n"
					+ "				break;\n"
					+ "			}",
			""
					+ "			do {\n"
					+ "				break;\n"
					+ "			} while (true);",
			""
					+ "			for (int i : new int[100]) {\n"
					+ "				break;\n"
					+ "			}",
			""
					+ "			int value2 = new java.util.Random().nextInt();\n"
					+ "			switch (value2) {\n"
					+ "			case 1:\n"
					+ "				break;\n"
					+ "			}",
			""
					+ "			Runnable r = () -> {\n"
					+ "				while (true) {\n"
					+ "					break;\n"
					+ "				}\n"
					+ "			};",
			""
					+ "			Runnable r = new Runnable() {\n"
					+ "				@Override\n"
					+ "				public void run() {\n"
					+ "					while (true) {\n"
					+ "						break;\n"
					+ "					}\n"
					+ "				}\n"
					+ "			};",
			""
					+ "			class LocalCass {\n"
					+ "				public void run() {\n"
					+ "					while (true) {\n"
					+ "						break;\n"
					+ "					}\n"
					+ "				}\n"
					+ "			}",
	})
	void visit_escapedBreakStatement_shouldTransform(String codeEscapingBreakStatement) throws Exception {
		String original = ""
				+ "	void supportedBreakStatement(int value) {\n"
				+ "		if (value == 1) {\n"
				+ codeEscapingBreakStatement + "\n"
				+ "		} else if (value == 2) {\n"
				+ "		} else {\n"
				+ "		}\n"
				+ "	}";

		String expected = ""
				+ "	void supportedBreakStatement(int value) {\n"
				+ "		switch (value) {\n"
				+ "		case 1 -> {\n"
				+ codeEscapingBreakStatement + "\n"
				+ "			break;\n"
				+ "		}\n"
				+ "		case 2 -> {break;}\n"
				+ "		default -> {break;}\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}
}
