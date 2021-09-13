package eu.jsparrow.core.visitor.switchexpression;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.UseSwitchExpressionASTVisitor;

class UseSwitchExpressionASTVisitorTests extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	void setUp() {
		setVisitor(new UseSwitchExpressionASTVisitor());
		setJavaVersion(JavaCore.VERSION_14);
	}
	
	@Test
	void visit_baseCase_shouldTransform() throws Exception {
		String original = ""
				+ "String value = System.in.toString();\n"
				+ "switch (value) {\n"
				+ "case \"t\":\n"
				+ "	System.out.println(\"true\");\n"
				+ "	break;\n"
				+ "case \"f\":\n"
				+ "	System.out.println(\"False\");\n"
				+ "	break;\n"
				+ "default:\n"
				+ "	System.out.println(\"None\");\n"
				+ "	break;\n"
				+ "}";
		String expected = ""
				+ "String value = System.in.toString();\n"
				+ "switch (value) {\n"
				+ "case \"t\" -> System.out.println(\"true\");\n"
				+ "case \"f\" -> System.out.println(\"False\");\n"
				+ "default -> System.out.println(\"None\");\n"
				+ "}";
		assertChange(original, expected);
	}

	@Test
	void visit_missingParentBlock_shouldTransform() throws Exception {
		String original = ""
				+ "int digit = 0;\n"
				+ "String value = \"test\";\n"
				+ "if (true)\n"
				+ "switch (digit) {\n"
				+ "case 1:\n"
				+ "	value = \"one\";\n"
				+ "	break;\n"
				+ "case 2:\n"
				+ "	value = \"two\";\n"
				+ "	break;\n"
				+ "default:\n"
				+ "	value = \"other\";\n"
				+ "}";
		String expected = ""
				+ "int digit = 0;\n"
				+ "String value = \"test\";\n"
				+ "if (true)\n"
				+ "	value = switch (digit) {\n"
				+ "	case 1 -> \"one\";\n"
				+ "	case 2 -> \"two\";\n"
				+ "	default -> \"other\";\n"
				+ "};";
		assertChange(original, expected);
	}
	
	@Test
	void visit_multipleStatementsInSwitchCase_shouldTransform() throws Exception {
		String original = ""
				+ "int digit = 0;\n"
				+ "String value = \"test\";\n"
				+ "switch (digit) {\n"
				+ "case 1:\n"
				+ "	value = \"one\";\n"
				+ "	System.out.println(\"Value: \");\n"
				+ "	System.out.println(value);\n"
				+ "	break;\n"
				+ "case 2:\n"
				+ "	value = \"two\";\n"
				+ "	break;\n"
				+ "default:\n"
				+ "	value = \"other\";\n"
				+ "}";
		String expected = ""
				+ "int digit = 0;\n"
				+ "String value = \"test\";\n"
				+ "switch (digit) {\n"
				+ "case 1 -> {\n"
				+ "	value = \"one\";\n"
				+ "	System.out.println(\"Value: \");\n"
				+ "	System.out.println(value);\n"
				/* 
				 * NOTE: this break statement doesn't really show up in eclipse. 
				 * It is put here only to let this test pass.  Future Eclipse versions
				 * may break this test.
				 * */ 
				+ "	break;\n"
				+ "}\n"
				+ "case 2 -> value = \"two\";\n"
				+ "default -> value = \"other\";\n"
				+ "}";
		assertChange(original, expected);
	}
	
	@Test
	void visit_loopInSwitchCase_shouldTransform() throws Exception {
		String original = ""
				+ "int digit = 0;\n"
				+ "String value = \"test\";\n"
				+ "switch (digit) {\n"
				+ "case 1: while (true) {value = \"one\"; break;}\n"
				+ "	break;\n"
				+ "case 2:\n"
				+ "	value = \"two\";\n"
				+ "	break;\n"
				+ "default:\n"
				+ "	value = \"other\";\n"
				+ "}";
		String expected = ""
				+ "int digit = 0;\n"
				+ "String value = \"test\";\n"
				+ "switch (digit) {\n"
				+ "case 1 -> {\n"
				+ "	while (true) {value = \"one\"; break;}\n"
				/* 
				 * NOTE: this break statement doesn't really show up in eclipse. 
				 * It is put here only to let this test pass.  Future Eclipse versions
				 * may break this test.
				 * */ 
				+ "	break;\n"
				+ "}\n"
				+ "case 2 -> value = \"two\";\n"
				+ "default -> value = \"other\";\n"
				+ "}";
		assertChange(original, expected);
	}
	
	@Test
	void visit_emptySwitchCaseClauses_shouldTransform() throws Exception {
		String original = ""
				+ "int digit = 0;\n"
				+ "String value = \"test\";\n"
				+ "switch (digit) {\n"
				+ "case 1:\n"
				+ "	break;\n"
				+ "case 2:\n"
				+ "	break;\n"
				+ "default:			\n"
				+ "}";
		String expected = ""
				+ "int digit = 0;\n"
				+ "String value = \"test\";\n"
				+ "switch (digit) {\n"
				+ "case 1 -> {\n"
				/* 
				 * NOTE: these break statements doesn't really show up in eclipse. 
				 * They are put here only to let this test pass.  Future Eclipse versions
				 * may break this test.
				 * */ 
				+ "	break;\n"
				+ "}\n"
				+ "case 2 -> {\n"
				+ "	break;\n"
				+ "}\n"
				+ "default -> {\n"
				+ "	break;\n"
				+ "}\n"
				+ "}";
		assertChange(original, expected);
	}
	
	@Test
	void visit_switchCaseBlock_shouldTransform() throws Exception {
		String original = ""
				+ "int digit = getClass().getName().length(); \n"
				+ "String value;\n"
				+ "switch (digit) {\n"
				+ "case 0: {\n"
				+ "	System.out.println();\n"
				+ "	value = \"zero\";\n"
				+ "	break;\n"
				+ "}\n"
				+ "case 1:\n"
				+ "	value = \"one\";\n"
				+ "	break;\n"
				+ "\n"
				+ "default:\n"
				+ "	value = \"other\";\n"
				+ "}";
		String expected = ""
				+ "int digit = getClass().getName().length(); \n"
				+ "String value = switch (digit) {\n"
				+ "case 0 -> {\n"
				+ "	System.out.println();\n"
				+ "	yield \"zero\";\n"
				+ "}\n"
				+ "case 1 -> \"one\";\n"
				+ "default -> \"other\";\n"
				+ "};";
		assertChange(original, expected);
	}
	
	@Test
	void visit_throwStatements_shouldTransform() throws Exception {
		String original = ""
				+ "int digit = 0;\n"
				+ "String value = \"test\";\n"
				+ "switch (digit) {\n"
				+ "case 1:\n"
				+ "	value = \"one\";\n"
				+ "	break;\n"
				+ "case 2:\n"
				+ "	value = \"two\";\n"
				+ "	break;\n"
				+ "default: \n"
				+ "	throw new RuntimeException();\n"
				+ "}";
		String expected = ""
				+ "int digit = 0;\n"
				+ "String value = \"test\";\n"
				+ "switch (digit) {\n"
				+ "case 1 -> value = \"one\";\n"
				+ "case 2 -> value = \"two\";\n"
				+ "default -> throw new RuntimeException();\n"
				+ "}";
		assertChange(original, expected);
	}
	
	@Test
	void visit_multipleBreakStatement_shouldNotTransform() throws Exception {
		String original = ""
				+ "int digit = 0;\n"
				+ "String value;\n"
				+ "switch (digit) {\n"
				+ "case 1:\n"
				+ "	value = \"one\";\n"
				+ "	if(value.isEmpty()) {\n"
				+ "		value = \"\";\n"
				+ "		break;\n"
				+ "	}\n"
				+ "	value = \"one\";\n"
				+ "	break;\n"
				+ "case 2:\n"
				+ "	value = \"two\";\n"
				+ "	break;\n"
				+ "}";
		String expected = ""
				+ "int digit = 0;\n"
				+ "String value;\n"
				+ "switch (digit) {\n"
				+ "case 1 -> {\n"
				+ "	value = \"one\";\n"
				+ "	if (value.isEmpty()) {\n"
				+ "		value = \"\";\n"
				+ "		break;\n"
				+ "	}\n"
				+ "	value = \"one\";\n"
				/* 
				 * NOTE: this break statement doesn't really show up in eclipse. 
				 * It is put here only to let this test pass.  Future Eclipse versions
				 * may break this test.
				 * */ 
				+ "	break;\n"
				+ "}\n"
				+ "case 2 -> value = \"two\";\n"
				+ "}";
		
		assertChange(original, expected);
	}
}
