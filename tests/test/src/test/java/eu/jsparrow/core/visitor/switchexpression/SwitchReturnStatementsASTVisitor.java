package eu.jsparrow.core.visitor.switchexpression;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.UseSwitchExpressionASTVisitor;

public class SwitchReturnStatementsASTVisitor extends UsesJDTUnitFixture {
	
	@BeforeEach
	void setUp() {
		setDefaultVisitor(new UseSwitchExpressionASTVisitor());
		setJavaVersion(JavaCore.VERSION_14);
	}
	
	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_returnStatement_shouldTransform() throws Exception {
		String original = ""
				+ "String assignStringValue_shouldTransform(int digit) {\n"
				+ "	String value;\n"
				+ "	switch (digit) {\n"
				+ "	case 1:\n"
				+ "		return \"one\";\n"
				+ "	case 2:\n"
				+ "		return \"two\";\n"
				+ "	case 3:\n"
				+ "		return \"three\";\n"
				+ "	default:\n"
				+ "		return \"other\";\n"
				+ "	}\n"
				+ "}";
		String expected = ""
				+ "String assignStringValue_shouldTransform(int digit) {\n"
				+ "	String value;\n"
				+ "	return switch (digit) {\n"
				+ "	case 1 -> \"one\";\n"
				+ "	case 2 -> \"two\";\n"
				+ "	case 3 -> \"three\";\n"
				+ "	default -> \"other\";\n"
				+ "	};\n"
				+ "}";
		assertChange(original, expected);
	}
	
	@Test
	void visit_singleReturnStatementInDefault_shouldTransform() throws Exception {
		String original = ""
				+ "private int returningStatements() {\n"
				+ "	int digit = 0;\n"
				+ "	String value = \"test\";\n"
				+ "	switch (digit) {\n"
				+ "	case 1:\n"
				+ "		value = \"one\";\n"
				+ "		break;\n"
				+ "	case 2:\n"
				+ "		value = \"two\";\n"
				+ "		break;\n"
				+ "	default:\n"
				+ "		return 0;\n"
				+ "	}\n"
				+ "	return 1;\n"
				+ "}";
		String expected = ""
				+ "private int returningStatements() {\n"
				+ "	int digit = 0;\n"
				+ "	String value = \"test\";\n"
				+ "	switch (digit){case 1 ->value = \"one\";case 2 ->value = \"two\";default -> { return 0;}}\n"
				+ "	return 1;\n"
				+ "}";
		assertChange(original, expected);
	}
	
	@Test
	void visit_multipleReturnStatements_shouldTransform() throws Exception {
		String original = ""
				+ "String multipleReturnStatements(int digit) {\n"
				+ "	int i2 = 10;\n"
				+ "	String value;\n"
				+ "	switch (digit) {\n"
				+ "	case 1:\n"
				+ "		if(digit > i2) {\n"
				+ "			return \"10\";\n"
				+ "		}\n"
				+ "		return \"one\";\n"
				+ "	case 2:\n"
				+ "		return \"two\";\n"
				+ "	default:\n"
				+ "		return \"other\";\n"
				+ "	}\n"
				+ "}";
		String expected = ""
				+ "String multipleReturnStatements(int digit) {\n"
				+ "	int i2 = 10;\n"
				+ "	String value;\n"
				+ "	switch (digit) {\n"
				+ "	case 1 -> {\n"
				+ "		if(digit > i2) {\n"
				+ "			return \"10\";\n"
				+ "		}\n"
				+ "		return \"one\";\n"
				+ "	}\n"
				+ "	case 2 -> {\n"
				+ "		return \"two\";\n"
				+ "	}\n"
				+ "	default -> {\n"
				+ "		return \"other\";\n"
				+ "	}\n"
				+ "	}\n"
				+ "}";
		assertChange(original, expected);
	}

	@Test
	void visit_throwsStatement_shouldTransform() throws Exception {
		String original = ""
				+ "String usingThrowStatement(int digit) {\n"
				+ "	switch (digit) {\n"
				+ "	case 1:\n"
				+ "		return \"one\";\n"
				+ "	case 2:\n"
				+ "		return \"two\";\n"
				+ "	case 3:\n"
				+ "		return \"three\";\n"
				+ "	default:\n"
				+ "		throw new RuntimeException(\"none\");\n"
				+ "	}\n"
				+ "}";
		String expected = ""
				+ "String usingThrowStatement(int digit) {\n"
				+ "	return switch (digit) {\n"
				+ "	case 1 -> \"one\";\n"
				+ "	case 2 -> \"two\";\n"
				+ "	case 3 -> \"three\";\n"
				+ "	default -> throw new RuntimeException(\"none\");\n"
				+ "	};\n"
				+ "}";
		assertChange(original, expected);
	}
}
