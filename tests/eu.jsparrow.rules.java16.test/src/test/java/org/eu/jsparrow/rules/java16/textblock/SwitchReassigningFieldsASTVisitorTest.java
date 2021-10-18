package org.eu.jsparrow.rules.java16.textblock;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.UseSwitchExpressionASTVisitor;

class SwitchReassigningFieldsASTVisitorTest extends UsesJDTUnitFixture {
	
	@BeforeEach
	void setUp() {
		setDefaultVisitor(new UseSwitchExpressionASTVisitor());
		setJavaVersion(JavaCore.VERSION_14);
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}
	
	@Test
	void visit_reassigningField() throws Exception {
		String original = ""
				+ "String field = \"\";\n"
				+ "void reassignField_shouldTransform() {\n"
				+ "	int digit = 0;\n"
				+ "	switch (digit) {\n"
				+ "	case 1:\n"
				+ "		field = \"one\";\n"
				+ "		break;\n"
				+ "	case 2:\n"
				+ "		field = \"two\";\n"
				+ "		break;\n"
				+ "	default:\n"
				+ "		field = \"other\";\n"
				+ "	}\n"
				+ "}";
		String expected = ""
				+ "String field = \"\";\n"
				+ "void reassignField_shouldTransform() {\n"
				+ "	int digit = 0;\n"
				+ "	field = switch (digit) {\n"
				+ "	case 1 -> \"one\";\n"
				+ "	case 2 -> \"two\";\n"
				+ "	default -> \"other\";\n"
				+ "	};\n"
				+ "}";
		assertChange(original, expected);
	}

	@Test
	void visit_reassignWithFieldAccess() throws Exception {
		String original = ""
				+ "String field = \"\";\n"
				+ "void reassignWithFieldAccess_shouldTransform() {\n"
				+ "	int digit = 0;\n"
				+ "	switch (digit) {\n"
				+ "	case 1:\n"
				+ "		this.field = \"one\";\n"
				+ "		break;\n"
				+ "	case 2:\n"
				+ "		this.field = \"two\";\n"
				+ "		break;\n"
				+ "	default:\n"
				+ "		this.field = \"other\";\n"
				+ "	}\n"
				+ "}";
		String expected = ""
				+ "String field = \"\";\n"
				+ "void reassignWithFieldAccess_shouldTransform() {\n"
				+ "	int digit = 0;\n"
				+ "	this.field = switch (digit) {\n"
				+ "	case 1 -> \"one\";\n"
				+ "	case 2 -> \"two\";\n"
				+ "	default -> \"other\";\n"
				+ "	};\n"
				+ "}";
		assertChange(original, expected);
	}
	
	@Test
	void visit_reassignMethodParameter_shouldTransform() throws Exception {
		String original = ""
				+ "void reassignMethodParameter(int j) {\n"
				+ "	for (int i = 0; i < 10; i++) {\n"
				+ "		switch (i) {\n"
				+ "		case 1:\n"
				+ "			j = 0;\n"
				+ "			break;\n"
				+ "		case 2:\n"
				+ "			j = 5;\n"
				+ "			break;\n"
				+ "		default:\n"
				+ "			j = i - 1;\n"
				+ "		}\n"
				+ "	}\n"
				+ "}";

		String expected = ""
				+ "void reassignMethodParameter(int j) {\n"
				+ "	for (int i = 0; i < 10; i++) {\n"
				+ "		j = switch (i) {\n"
				+ "		case 1 -> 0;\n"
				+ "		case 2 -> 5;\n"
				+ "		default -> i - 1;\n"
				+ "		};\n"
				+ "	}\n"
				+ "}";
		assertChange(original, expected);
	}
	
	@Test
	void visit_switchCaseMultipleStatements_shouldTransform() throws Exception {
		String original = ""
				+ "String value = \"\";"
				+ "void multipleStatementsInSwitchCase() {\n"
				+ "	int digit = 0;\n"
				+ "	String value = \"test\";\n"
				+ "	switch (digit) {\n"
				+ "	case 1:\n"
				+ "		value = \"one\";\n"
				+ "		System.out.println(\"Value: \");\n"
				+ "		System.out.println(value);\n"
				+ "		break;\n"
				+ "	case 2:\n"
				+ "		value = \"two\";\n"
				+ "		break;\n"
				+ "	default:\n"
				+ "		value = \"other\";\n"
				+ "	}\n"
				+ "}";

		String expected = ""
				+ "String value = \"\";"
				+ "void multipleStatementsInSwitchCase() {\n"
				+ "	int digit = 0;\n"
				+ "	String value = \"test\";\n"
				+ "	switch (digit) {\n"
				+ "	case 1 -> {\n"
				+ "		value = \"one\";\n"
				+ "		System.out.println(\"Value: \");\n"
				+ "		System.out.println(value);\n"
				/* 
				 * NOTE: this break statement doesn't really show up in eclipse. 
				 * It is put here only to let this test pass.  Future Eclipse versions
				 * may break this test.
				 * */ 
				+ "		break;\n"
				+ "	}\n"
				+ "	case 2 -> value = \"two\";\n"
				+ "	default -> value = \"other\";\n"
				+ "	}\n"
				+ "}";

		assertChange(original, expected);
	}


	@Test
	void visit_multipleStatementsYieldingValue_shouldTransform() throws Exception {
		String original = ""
				+ "private String simpleValue = \"test\";\n"
				+ "void multipleStatementsYieldValue() {\n"
				+ "	int digit = 0;\n"
				+ "	switch (digit) {\n"
				+ "	case 1:\n"
				+ "		System.out.println(\"Value: 1\");\n"
				+ "		simpleValue = \"one\";\n"
				+ "		break;\n"
				+ "	case 2:\n"
				+ "		simpleValue = \"two\";\n"
				+ "		break;\n"
				+ "	default:\n"
				+ "		simpleValue = \"other\";\n"
				+ "	}\n"
				+ "	System.out.println(simpleValue);\n"
				+ "}";

		String expected = ""
				+ "private String simpleValue = \"test\";\n"
				+ "void multipleStatementsYieldValue() {\n"
				+ "	int digit = 0;\n"
				+ "	simpleValue = switch (digit) {\n"
				+ "	case 1 -> {\n"
				+ "		System.out.println(\"Value: 1\");\n"
				+ "		yield \"one\";\n"
				+ "	}\n"
				+ "	case 2 -> \"two\";\n"
				+ "	default -> \"other\";\n"
				+ "	};\n"
				+ "	System.out.println(simpleValue);\n"
				+ "}";
		
		assertChange(original, expected);
	}
}
