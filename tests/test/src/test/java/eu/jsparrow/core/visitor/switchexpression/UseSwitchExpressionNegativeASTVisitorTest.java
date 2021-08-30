package eu.jsparrow.core.visitor.switchexpression;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.UseSwitchExpressionASTVisitor;

class UseSwitchExpressionNegativeASTVisitorTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	void setUp() {
		setJavaVersion(JavaCore.VERSION_15);
		setVisitor(new UseSwitchExpressionASTVisitor());
	}

	@Test
	void visit_multipleBreakStatement_shouldNotTransform() throws Exception {
		String original = ""
				+ "int digit = 10;"
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

		assertNoChange(original);
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
}
