package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;

@SuppressWarnings({ "nls" })
public class InlineLocalVariablesASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setDefaultVisitor(new InlineLocalVariablesASTVisitor());
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_usedForReturn_shouldTransform() throws Exception {
		String original = "" +
				"	int returnLocalX() {\n" +
				"		int x = 1;\n" +
				"		return x;\n" +
				"	}";
		String expected = "" +
				"	int returnLocalX() {\n" +
				"		return 1;\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	void visit_usedAsAssignmentRHS_shouldTransform() throws Exception {
		String original = "" +
				"	void assignLocalX() {\n" +
				"		int x = 1;\n" +
				"		int x2 = x;\n" +
				"	}";
		String expected = "" +
				"	void assignLocalX() {\n" +
				"		int x2 = 1;\n" +
				"	}";

		assertChange(original, expected);
	}
}
