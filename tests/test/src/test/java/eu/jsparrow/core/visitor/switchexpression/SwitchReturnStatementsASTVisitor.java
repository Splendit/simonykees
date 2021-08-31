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

}
