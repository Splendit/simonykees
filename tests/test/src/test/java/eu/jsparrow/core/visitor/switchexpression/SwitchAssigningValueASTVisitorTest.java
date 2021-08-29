package eu.jsparrow.core.visitor.switchexpression;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.UseSwitchExpressionASTVisitor;

class SwitchAssigningValueASTVisitorTest extends UsesSimpleJDTUnitFixture {

	
	@BeforeEach
	void setUp() {
		setJavaVersion(JavaCore.VERSION_15);
		setVisitor(new UseSwitchExpressionASTVisitor());
	}
	
	@Test
	void visit_assignStringValue_shouldTransform() throws Exception {
		String original = ""
				+ "int digit = 10;"
				+ "String value;\n"
				+ "switch(digit) {\n"
				+ "	case 0: value = \"zero\"; break;\n"
				+ "	case 1: value = \"one\"; break;\n"
				+ "	case 2: value = \"two\"; break;\n"
				+ "	case 3: value = \"three\"; break;\n"
				+ "	default: value = \"other\";\n"
				+ "}";

		String expected = ""
				+ "int digit = 10;"
				+ "String value = switch (digit) {\n"
				+ "	case 0 -> \"zero\";\n"
				+ "	case 1 -> \"one\";\n"
				+ "	case 2 -> \"two\";\n"
				+ "	case 3 -> \"three\";\n"
				+ "	default -> \"other\";\n"
				+ "};\n";

		assertChange(original, expected);
	}
	
	@Test
	public void visit_reassignValue_shouldTransform() throws Exception {
		String original = ""
				+ "int digit = 0;\n"
				+ "String value = \"test\";\n"
				+ "System.out.println(value);\n"
				+ "switch (digit) {\n"
				+ "case 1:\n"
				+ "	value = \"one\";\n"
				+ "	break;\n"
				+ "case 2:\n"
				+ "	value = \"two\";\n"
				+ "	break;\n"
				+ "case 3:\n"
				+ "	value = \"three\";\n"
				+ "	break;\n"
				+ "default:\n"
				+ "	value = \"other\";\n"
				+ "}";
		String expected = ""
				+ "int digit = 0;\n"
				+ "String value = \"test\";\n"
				+ "System.out.println(value);\n"
				+ "value = switch (digit) {\n"
				+ "case 1 -> \"one\";\n"
				+ "case 2 -> \"two\";\n"
				+ "case 3 -> \"three\";\n"
				+ "default -> \"other\";\n"
				+ "};\n"
				+ "	";
		assertChange(original, expected);
	}
	
	@Test
	void visit_missingDefaultClause_shouldTransform() throws Exception {
		String original = ""
				+ "int digit = 10;"
				+ "String value;\n"
				+ "switch(digit) {\n"
				+ "	case 0: value = \"zero\"; break;\n"
				+ "	case 1: value = \"one\"; break;\n"
				+ "	case 2: value = \"two\"; break;\n"
				+ "}";

		String expected = ""
				+ "int digit = 10;"
				+ "String value;\n"
				+ "switch (digit) {\n"
				+ "	case 0 -> value = \"zero\";\n"
				+ "	case 1 -> value = \"one\";\n"
				+ "	case 2 -> value = \"two\";\n"
				+ "}\n";

		assertChange(original, expected);
	}
	
	@Test
	void visit_reassigned_shouldTransform() throws Exception {
		String original = ""
				+ "int digit = 10;"
				+ "String value = \"\";\n"
				+ "	switch(digit) {\n"
				+ "	case 0: value = \"zero\"; break;\n"
				+ "	case 1: value = \"one\"; break;\n"
				+ "	case 2: value = \"two\"; break;\n"
				+ "	default: value = \"other\"; break;\n"
				+ "}";

		String expected = ""
				+ "int digit = 10;"
				+ "String value = switch (digit) {\n"
				+ "	case 0 -> \"zero\";\n"
				+ "	case 1 -> \"one\";\n"
				+ "	case 2 -> \"two\";\n"
				+ "	default -> \"other\";\n"
				+ "};";

		assertChange(original, expected);
	}
	
	@Test
	void visit_reassignedMissingDefault_shouldTransform() throws Exception {
		String original = ""
				+ "int digit = 10;"
				+ "String value = \"\";\n"
				+ "switch(digit) {\n"
				+ "	case 0: value = \"zero\"; break;\n"
				+ "	case 1: value = \"one\"; break;\n"
				+ "	case 2: value = \"two\"; break;\n"
				+ "}";

		String expected = ""
				+ "int digit = 10;"
				+ "String value = \"\";\n"
				+ "switch (digit) {\n"
				+ "	case 0 -> value = \"zero\";\n"
				+ "	case 1 -> value = \"one\";\n"
				+ "	case 2 -> value = \"two\";\n"
				+ "}";

		assertChange(original, expected);
	}
	
	@Disabled("For some reason the expected code cannot be parsed")
	@Test
	void visit_switchCaseMultipleStatements_shouldTransform() throws Exception {
		String original = ""
				+ "int digit = 10;\n"
				+ "String value;\n"
				+ "switch(digit) {\n"
				+ "case 0: \n"
				+ "	System.out.println();\n"
				+ "	value = \"zero\"; break;\n"
				+ "case 1: value = \"one\"; break;\n"
				+ "case 2: value = \"two\"; break;\n"
				+ "default: value = \"other\";\n"
				+ "}";

		String expected = ""
				+ "int digit = 10;\n"
				+ "String value = switch (digit) {\n"
				+ "case 0 -> {\n"
				+ "	System.out.println();\n"
				+ "	yield \"zero\";\n"
				+ "}\n"
				+ "case 1 -> \"one\";\n"
				+ "case 2 -> \"two\";\n"
				+ "default -> \"other\";\n"
				+ "};"
				+ "	System.out.println(value);\n"
				+ "";

		assertChange(original, expected);
	}
	
	
	@Test
	void visit_combineCaseClauses_shouldTransform() throws Exception {
		String original = ""
				+ "int digit = 10;"
				+ "String value;\n"
				+ "switch(digit) {\n"
				+ "case 0: \n"
				+ "case 1: value = \"one\"; break;\n"
				+ "case 2: value = \"two\"; break;\n"
				+ "default: value = \"other\";}";

		String expected = ""
				+ "int digit = 10;"
				+ "String value = switch (digit) {\n"
				+ "case 0, 1 -> \"one\";\n"
				+ "case 2 -> \"two\";\n"
				+ "default -> \"other\";\n"
				+ "};";

		assertChange(original, expected);
	}
}
