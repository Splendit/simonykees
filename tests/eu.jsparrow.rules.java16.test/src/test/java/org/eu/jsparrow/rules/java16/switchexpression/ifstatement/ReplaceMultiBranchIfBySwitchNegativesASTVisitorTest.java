package org.eu.jsparrow.rules.java16.switchexpression.ifstatement;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.ifstatement.ReplaceMultiBranchIfBySwitchASTVisitor;

class ReplaceMultiBranchIfBySwitchNegativesASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	void setUp() {
		setDefaultVisitor(new ReplaceMultiBranchIfBySwitchASTVisitor());
		setJavaVersion(JavaCore.VERSION_14);
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_ContinueStatementWithinIfStatement_shouldNotTransform() throws Exception {
		String original = ""
				+ "	void continueStatementWithinIfStatement(String[] strings) {\n"
				+ "		for(String value : strings) {\n"
				+ "			if (value.equals(\"a\") || value.equals(\"b\") || value.equals(\"c\")) {\n"
				+ "				System.out.println(1);\n"
				+ "				continue;\n"
				+ "			} else if (value.equals(\"d\")) {\n"
				+ "				System.out.println(2);\n"
				+ "			} else {\n"
				+ "				System.out.println(3);\n"
				+ "			}			\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}


}
