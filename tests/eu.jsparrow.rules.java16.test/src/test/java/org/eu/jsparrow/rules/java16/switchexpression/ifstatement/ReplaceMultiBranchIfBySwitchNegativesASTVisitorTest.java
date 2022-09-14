package org.eu.jsparrow.rules.java16.switchexpression.ifstatement;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.ifstatement.ReplaceMultiBranchIfBySwitchASTVisitor;

class ReplaceMultiBranchIfBySwitchNegativesASTVisitorTest extends UsesJDTUnitFixture {

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
	void visit_BreakStatementWithinIfStatement_shouldNotTransform() throws Exception {
		String original = ""
				+ "	void breakLoopWithinIfStatement(String[] strings) {\n"
				+ "		for(String value : strings) {\n"
				+ "			if (value.equals(\"a\") || value.equals(\"b\") || value.equals(\"c\")) {\n"
				+ "				System.out.println(1);\n"
				+ "				break;\n"
				+ "			} else if (value.equals(\"d\")) {\n"
				+ "				System.out.println(2);\n"
				+ "			} else {\n"
				+ "				System.out.println(3);\n"
				+ "			}			\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	void visit_ContinueStatementWithinIfStatement_shouldNotTransform() throws Exception {
		String original = ""
				+ "	void continueStatementWithinIfStatement(String[] strings) {\n"
				+ "		for(String value : strings) {\n"
				+ "			if (value.equals(\"a\") || value.equals(\"b\") || value.equals(\"c\")) {\n"
				+ "				System.out.println(1);\n"
				+ "				continue;\n"
				+ "			} else if (value.equals(\"d\")) {\n"
				+ "				System.out.println(2);\n"
				+ "			} else {\n"
				+ "				System.out.println(3);\n"
				+ "			}			\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	void visit_YieldStatementWithinIfStatement_shouldNotTransform() throws Exception {
		String original = ""
				+ "	String yieldWithinIfStatement(int value, int value2) {\n"
				+ "		return switch (value) {\n"
				+ "		case 0 -> \"value == ZERO\";\n"
				+ "		case 1 -> \"value == ONE\";\n"
				+ "		case 2 -> \"value == TWO\";\n"
				+ "		// break out of switch expression not allowed\n"
				+ "		// break;\n"
				+ "		default -> {\n"
				+ "			if (value2 == 0) {\n"
				+ "				yield \"value2 == ZERO\";\n"
				+ "			} else if (value2 == 1) {\n"
				+ "				yield \"value2 == ONE\";\n"
				+ "			} else if (value2 == 2) {\n"
				+ "				yield \"value2 == TWO\";\n"
				+ "			} else {\n"
				+ "				yield \"value == OTHER,  value2 == OTHER\";\n"
				+ "			}\n"
				+ "		}\n"
				+ "		};\n"
				+ "	}";

		assertNoChange(original);
	}

	/**
	 * this test may fail in case of relaxations in connection with labels and
	 * labeled statements.
	 * 
	 * @throws Exception
	 */
	@Test
	void visit_LoopWithLabelWithinIfStatement_shouldNotTransform() throws Exception {
		String original = ""
				+ "	void loopWithLabelWithinIfStatement(String value, String... otherValues) {\n"
				+ "		if (value.equals(\"a\")) {\n"
				+ "			loop: for (String otherValue : otherValues) {\n"
				+ "				if (otherValue.equals(value)) {\n"
				+ "					System.out.println(\"otherValue.equals(value)\");\n"
				+ "					break loop;\n"
				+ "				}\n"
				+ "			}\n"
				+ "			System.out.println(1);\n"
				+ "		} else if (value.equals(\"b\")) {\n"
				+ "			System.out.println(2);\n"
				+ "		} else {\n"
				+ "			System.out.println(3);\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

}
