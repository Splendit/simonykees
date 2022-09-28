package org.eu.jsparrow.rules.java16.switchexpression.ifstatement;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.ifstatement.ReplaceMultiBranchIfBySwitchASTVisitor;

/**
 * Tests the {@link ReplaceMultiBranchIfBySwitchASTVisitor} but with a focus on
 * the code coverage for the type {@code LabeledBreakStatementsVisitor}.
 *
 */
class LabeledBreakStatementsVisitorForIfTest extends UsesJDTUnitFixture {

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
	void visit_breakLabeledForEnclosingIf_shouldNotTransform() throws Exception {
		String original = ""
				+ "	void breakLabeledForEnclosingIf(int value) {\n"
				+ "		loop: for (;;) {\n"
				+ "			if (value == 1) {\n"
				+ "				break loop;\n"
				+ "			} else if (value == 2) {\n"
				+ "			} else {\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	/**
	 * This test may fail in case of relaxations in connection with labels and
	 * labeled statements which are enclosed by the given if statement.
	 */
	@Test
	void visit_breakLabeledForEnclosedByIf_shouldNotTransform() throws Exception {
		String original = ""
				+ "	void breakLabeledForEnclosedByIf(int value) {\n"
				+ "		if (value == 1) {\n"
				+ "			loop: for (;;) {\n"
				+ "				break loop;\n"
				+ "			}\n"
				+ "		} else if (value == 2) {\n"
				+ "		} else {\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	void visit_continueLabeledForEnclosingIf_shouldNotTransform() throws Exception {
		String original = ""
				+ "	void continueLabeledForEnclosingIf(int value) {\n"
				+ "		loop: for (;;) {\n"
				+ "			if (value == 1) {\n"
				+ "				continue loop;\n"
				+ "			} else if (value == 2) {\n"
				+ "			} else {\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	/**
	 * This test may fail in case of relaxations in connection with labels and
	 * labeled statements which are enclosed by the given if statement.
	 */
	@Test
	void visit_continueLabeledForEnclosedByIf_shouldNotTransform() throws Exception {
		String original = ""
				+ "	void continueLabeledForEnclosedByIf(int value) {\n"
				+ "		if (value == 1) {\n"
				+ "			loop: for (;;) {\n"
				+ "				continue loop;\n"
				+ "			}\n"
				+ "		} else if (value == 2) {\n"
				+ "		} else {\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

}
