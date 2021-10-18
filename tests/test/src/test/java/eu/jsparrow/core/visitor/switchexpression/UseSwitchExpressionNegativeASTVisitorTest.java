package eu.jsparrow.core.visitor.switchexpression;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.UseSwitchExpressionASTVisitor;

class UseSwitchExpressionNegativeASTVisitorTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	void setUp() {
		setJavaVersion(JavaCore.VERSION_14);
		setVisitor(new UseSwitchExpressionASTVisitor());
	}
	
	@Test
	void visit_missingBreakStatement_shouldNotTransform() throws Exception {
		String original = ""
				+ "int digit = 10;\n"
				+ "String value;\n"
				+ "switch (digit) {\n"
				+ "case 0:\n"
				+ "	value = \"zero\";\n"
				+ "case 1:\n"
				+ "	value = \"one\";\n"
				+ "	break;\n"
				+ "default:\n"
				+ "	value = \"other\";\n"
				+ "}";

		assertNoChange(original);
	}
	
	@Test
	void visit_emptySwitchCase_shouldNotTransform() throws Exception {
		String original = ""
				+ "int digit = 10;\n"
				+ "String value;\n"
				+ "switch (digit) {\n"
				+ "}";

		assertNoChange(original);
	}
	
	@Test
	void visit_breakLabel_shouldNotTransform() throws Exception {
		String original = ""
				+ "int value = \"\".length();\n"
				+ "label_1: switch(value) {\n"
				+ "case 0: System.out.println();break label_1;\n"
				+ "}";

		assertNoChange(original);
	}
	
	@Test
	void visit_labeledSwitchCase_shouldNotTransform() throws Exception {
		String original = ""
				+ "int value = \"\".length();\n"
				+ "switch (value) {\n"
				+ "	case 1: {\n"
				+ "		label_1: switch(value) {\n"
				+ "		case 0: System.out.println();break label_1;\n"
				+ "		}\n"
				+ "	}\n"
				+ "}";

		assertNoChange(original);
	}
	
	@Test
	void visit_alreadySwitchExpression_shouldNotTransform() throws Exception {
		String original = ""
				+ "String t = \"\";\n"
				+ "int value = \"\".length();\n"
				+ "switch (value) {\n"
				+ "	case 0 -> t = \"\";\n"
				+ "	case 1 -> t = \"not-empty\";\n"
				+ "	case 2 -> t = \"2\";\n"
				+ "	default -> t = \"not-empty\";\n"
				+ "}\n"
				+ "System.out.println(t);";
		assertNoChange(original);
	}
	
	@Test
	void visit_variableSpanningSwitchCases_shouldNotTransform() throws Exception {
		String original = ""
				+ "int value = \"\".length();\n"
				+ "switch (value) {\n"
				+ "	case 1: String t = \"1\"; break;\n"
				+ "	case 2: t = \"2\"; break;\n"
				+ "	default: System.out.println(\"none\");\n"
				+ "}";
		assertNoChange(original);
	}
	
	@Test
	void visit_combineSwitchCaseWithDefault_shouldNotTransform() throws Exception {
		String original = ""
				+ "int digit = 0;\n"
				+ "switch (digit) {\n"
				+ "case 1: \n"
				+ "	System.out.println(\"one\");\n"
				+ "	break;\n"
				+ "case 2: \n"
				+ "default: \n"
				+ "	System.out.println(\"none\");\n"
				+ "}";
		assertNoChange(original);
	}
	
	@Test
	void visit_continueLabeledStatement_shouldNotTransform() throws Exception {
		String original = ""
				+ "String value = \"\";\n"
				+ "loop: for (int indexInner = 0; indexInner < 5; indexInner++) {\n"
				+ "	int digit = indexInner % 2;\n"
				+ "	switch (digit) {\n"
				+ "	case 1:\n"
				+ "		if (indexInner > 2) {\n"
				+ "			// causes compilation error in transformed code\n"
				+ "			continue loop;\n"
				+ "		}\n"
				+ "		value = \"one\";\n"
				+ "		break;\n"
				+ "	default:\n"
				+ "		value = \"other\";\n"
				+ "	}\n"
				+ "}";
		assertNoChange(original);
	}
}
