package org.eu.jsparrow.rules.java16.switchexpression;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.UseSwitchExpressionASTVisitor;

public class SwitchAssigningValueContainingReturnASTVisitorTest extends UsesJDTUnitFixture {
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
	void visit_ConditionalReturnBeforeLastAssignment_shouldTransformToSwitchStatement() throws Exception {
		String original = ""
				+ "	String result;\n"
				+ "	String assignToFieldBySwitch(int value) {\n"
				+ "		switch (value) {\n"
				+ "		case 0:\n"
				+ "			result = \"ZERO\";\n"
				+ "			break;\n"
				+ "		case 1:\n"
				+ "			result = \"ONE\";\n"
				+ "			break;\n"
				+ "		case 2:\n"
				+ "			result = \"TWO\";\n"
				+ "			break;\n"
				+ "		default:\n"
				+ "			if (value < 0) {\n"
				+ "				return \"NEGATIVE\";\n"
				+ "			}\n"
				+ "			result = \"GREATER THAN TWO\";\n"
				+ "		}\n"
				+ "		return result;\n"
				+ "	}";

		String expected = ""
				+ "	String result;\n"
				+ "	String assignToFieldBySwitch(int value) {\n"
				+ "		switch (value) {\n"
				+ "		case 0 -> result = \"ZERO\";\n"
				+ "		case 1 -> result = \"ONE\";\n"
				+ "		case 2 -> result = \"TWO\";\n"
				+ "		default -> {\n"
				+ "			if (value < 0) {\n"
				+ "				return \"NEGATIVE\";\n"
				+ "			}\n"
				+ "			result = \"GREATER THAN TWO\";\n"
				+ "			break;\n"
				+ "		}\n"
				+ "		}\n"
				+ "		return result;\n"
				+ "	};";

		assertChange(original, expected);
	}
}
