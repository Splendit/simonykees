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
		setJavaVersion(JavaCore.VERSION_14);
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
	void visit_reassignValue_shouldTransform() throws Exception {
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
	void visit_reassignParentBlockVariable_shouldTransform() throws Exception {
		String original = ""
				+ "int digit = 0;\n"
				+ "String value = \"test\";\n"
				+ "if(digit > 0) {\n"
				+ "	System.out.println(value);\n"
				+ "	switch (digit) {\n"
				+ "	case 1:\n"
				+ "		value = \"one\";\n"
				+ "		break;\n"
				+ "	case 2:\n"
				+ "		value = \"two\";\n"
				+ "		break;\n"
				+ "	default:\n"
				+ "		value = \"other\";\n"
				+ "	}\n"
				+ "}";
		String expected = ""
				+ "int digit = 0;\n"
				+ "String value = \"test\";\n"
				+ "if(digit > 0) {\n"
				+ "	System.out.println(value);\n"
				+ "	value = switch (digit) {\n"
				+ "	case 1 -> \"one\";\n"
				+ "	case 2 -> \"two\";\n"
				+ "	default -> \"other\";\n"
				+ "	};\n"
				+ "}";
		assertChange(original, expected);
	}
	
	@Test
	void visit_methodInvocationInitializer_shouldTransform() throws Exception {
		String original = ""
				+ "int digit = 0;\n"
				+ "String value = \"test\".substring(2);\n"
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
				+ "String value = \"test\".substring(2);\n"
				+ "value = switch (digit) {\n"
				+ "case 1 -> \"one\";\n"
				+ "case 2 -> \"two\";\n"
				+ "default -> \"other\";\n"
				+ "};";
		assertChange(original, expected);
	}
	
	@Test
	void visit_noInitializer_shouldTransform() throws Exception {
		String original = ""
				+ "int digit = 0;\n"
				+ "String value;\n"
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
				+ "String value = switch (digit) {\n"
				+ "case 1 -> \"one\";\n"
				+ "case 2 -> \"two\";\n"
				+ "default -> \"other\";\n"
				+ "};";
		assertChange(original, expected);
	}
	
	@Test
	void visit_multipleFragments_shouldTransform() throws Exception {
		String original = ""
				+ "int digit = 0;\n"
				+ "String value = \"test\", value2 = \"test2\";\n"
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
				+ "String value = \"test\", value2 = \"test2\";\n"
				+ "value = switch (digit) {\n"
				+ "case 1 -> \"one\";\n"
				+ "case 2 -> \"two\";\n"
				+ "default -> \"other\";\n"
				+ "};";
		assertChange(original, expected);
	}
	
	@Test
	void visit_assigningDifferentVariables_shouldTransform() throws Exception {
		String original = ""
				+ "int digit = 0;\n"
				+ "String value = \"test\";\n"
				+ "String value2 = \"value2\";\n"
				+ "switch (digit) {\n"
				+ "case 1:\n"
				+ "	value = \"one\";\n"
				+ "	break;\n"
				+ "case 2:\n"
				+ "	value2= \"two\";\n"
				+ "	break;\n"
				+ "default:\n"
				+ "	value = \"other\";\n"
				+ "}";
		String expected = ""
				+ "int digit = 0;\n"
				+ "String value = \"test\";\n"
				+ "String value2 = \"value2\";\n"
				+ "switch (digit) {\n"
				+ "case 1 -> value = \"one\";\n"
				+ "case 2 -> value2= \"two\";\n"
				+ "default -> value = \"other\";\n"
				+ "}";
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
	
	@Test
	void visit_reassignLoopVariable_shouldTransform() throws Exception {
		String original = ""
				+ "for (int i =0, j = 1; i<10; i++, j++) {\n"
				+ "	switch(i) {\n"
				+ "	case 1: j=0; break;\n"
				+ "	case 2: j = 5; break;\n"
				+ "	default: j = i-1;\n"
				+ "	}\n"
				+ "}";

		String expected = ""
				+ "for (int i =0, j = 1; i<10; i++, j++) {\n"
				+ "	j = switch (i) {\n"
				+ "	case 1 -> 0;\n"
				+ "	case 2 -> 5;\n"
				+ "	default -> i-1;\n"
				+ "	};\n"
				+ "}";

		assertChange(original, expected);
	}
}
