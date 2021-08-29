package eu.jsparrow.core.visitor.switchexpression;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.UseSwitchExpressionASTVisitor;

public class UseSwitchExpressionASTVisitorTests extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	void setUp() {
		setVisitor(new UseSwitchExpressionASTVisitor());
		setJavaVersion(JavaCore.VERSION_16);
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

}
