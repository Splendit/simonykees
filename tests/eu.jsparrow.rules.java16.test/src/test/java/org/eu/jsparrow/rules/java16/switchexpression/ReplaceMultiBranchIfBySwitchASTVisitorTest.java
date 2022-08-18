package org.eu.jsparrow.rules.java16.switchexpression;

import static org.junit.Assert.assertEquals;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.ReplaceMultiBranchIfBySwitchASTVisitor;

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
				+ "		if (value.equals(\"a\")) {\n"
				+ "			System.out.println(1);\n"
				+ "		} else if (value.equals(\"b\")) {\n"
				+ "			System.out.println(2);\n"
				+ "		} else {\n"
				+ "			System.out.println(3);\n"
				+ "		}\n"
				+ "	}";
		assertChange(original, expected);
	}

	@Test
	void visit_ifStatementWithOr() throws Exception {
		String original = ""
				+ "	void exampleWithSystemOutPrintln(String value) {\n"
				+ "		if (value.equals(\"a1\") || value.equals(\"a2\") || value.equals(\"a3\")) {\n"
				+ "			System.out.println(1);\n"
				+ "		} else if (value.equals(\"b1\") || value.equals(\"b2\")) {\n"
				+ "			System.out.println(2);\n"
				+ "		} else {\n"
				+ "			System.out.println(3);\n"
				+ "		}\n"
				+ "	}";
		String expected = ""
				+ "	void exampleWithSystemOutPrintln(String value) {\n"
				+ "		if (value.equals(\"a1\") || value.equals(\"a2\") || value.equals(\"a3\")) {\n"
				+ "			System.out.println(1);\n"
				+ "		} else if (value.equals(\"b1\") || value.equals(\"b2\")) {\n"
				+ "			System.out.println(2);\n"
				+ "		} else {\n"
				+ "			System.out.println(3);\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_ifStatementWithNumericLiterals_shouldTransform() throws Exception {
		String original = ""
				+ "	void ifStatementWithOrExpression(int value) {\n"
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
				+ "	void ifStatementWithOrExpression(int value) {\n"
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

		assertChange(original, expected);
	}

	String getResultBySubsequentIfReturnStatements(String value) {
		if ("A".equals(value) || "B".equals(value) || "C".equals(value)) {
			return "A | B | C";
		}
		if ("D".equals(value)) {
			return "D";
		}
		if ("E".equals(value) || "F".equals(value)) {
			return "E | F";
		}
		return "";
	}

	String getResultBySwitch(String value) {
		switch (value) {
		case "A":
		case "B":
		case "C":
			return "A | B | C";
		case "D":
			return "D";
		case "E":
		case "F":
			return "E | F";
		}

		return "";

	}

	@Test
	void testEquivalence() throws Exception {
		assertEquals("A | B | C", getResultBySubsequentIfReturnStatements("A"));
		assertEquals("A | B | C", getResultBySwitch("A"));
		
		assertEquals("A | B | C", getResultBySubsequentIfReturnStatements("B"));
		assertEquals("A | B | C", getResultBySwitch("B"));

		assertEquals("A | B | C", getResultBySubsequentIfReturnStatements("C"));
		assertEquals("A | B | C", getResultBySwitch("C"));

		assertEquals("D", getResultBySubsequentIfReturnStatements("D"));
		assertEquals("D", getResultBySwitch("D"));

		assertEquals("E | F", getResultBySubsequentIfReturnStatements("E"));
		assertEquals("E | F", getResultBySwitch("E"));
		
		assertEquals("E | F", getResultBySubsequentIfReturnStatements("F"));
		assertEquals("E | F", getResultBySwitch("F"));
	}

}
