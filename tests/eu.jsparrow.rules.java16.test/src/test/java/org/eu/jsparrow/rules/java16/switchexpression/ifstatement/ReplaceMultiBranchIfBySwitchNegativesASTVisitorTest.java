package org.eu.jsparrow.rules.java16.switchexpression.ifstatement;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
	void visit_differentVariableNames_shouldNotTransform() throws Exception {
		String original = ""
				+ "	void exampleWithSystemOutPrintln(String value, String anotherValue) {\n"
				+ "		if (value.equals(\"a\") || anotherValue.equals(\"b\")) {\n"
				+ "			System.out.println(1);\n"
				+ "		} else if (value.equals(\"c\")) {\n"
				+ "			System.out.println(2);\n"
				+ "		} else {\n"
				+ "			System.out.println(3);\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = { "\"a\"", "\"\\u0061\"", "\"\\141\""
	// breaks the test:
	// , "\"\\143\""
	})
	void visit_StringLiteralsWithSameValue_shouldNotTransform(String redundantLiteral) throws Exception {
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
	@ValueSource(strings = { "'a'",
	// TODO: include again
	// "'\\u0061'", "'\\141'"
	// breaks the test:
	// , "'\\143'"
	})
	void visit_CharacterLiteralsWithSameValue_shouldNotTransform(String redundantLiteral) throws Exception {
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
	void visit_IntLiteralsWithSameValue_shouldNotTransform(String redundantLiteral) throws Exception {
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

}
