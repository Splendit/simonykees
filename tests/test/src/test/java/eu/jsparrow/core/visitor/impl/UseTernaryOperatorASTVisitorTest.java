package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;

@SuppressWarnings({ "nls" })
public class UseTernaryOperatorASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setDefaultVisitor(new UseTernaryOperatorASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	/**
	 * This test is expected to fail as soon as the corresponding visitor will
	 * have been optimized:
	 * 
	 * The following code
	 * 
	 * <pre>
	 * int x;
	 * if (condition) {
	 * 	x = 1;
	 * } else {
	 * 	x = 0;
	 * }
	 * </pre>
	 * 
	 * should be transformed to
	 * 
	 * <pre>
	 * int x = condition ? 1 : 0;
	 * </pre>
	 * 
	 * and not to
	 * 
	 * <pre>
	 * int x;
	 * x = condition ? 1 : 0;
	 * </pre>
	 */
	@Test
	public void visit_firstExampleWithAssignment_shouldTransform() throws Exception {
		String original = ""
				+ "	void test() {\n"
				+ "		boolean condition = true;\n"
				+ "		int x;\n"
				+ "		if (condition) {\n"
				+ "			x = 1;\n"
				+ "		} else {\n"
				+ "			x = 0;\n"
				+ "		}\n"
				+ "	}";
		String expected = ""
				+ "	void test() {\n"
				+ "		boolean condition = true;\n"
				+ "		int x;\n"
				+ "		x=condition ? 1 : 0;\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_firstExampleWithReturn_shouldTransform() throws Exception {
		String original = ""
				+ "	int test() {\n"
				+ "		boolean condition = true;\n"
				+ "		if (condition) {\n"
				+ "			return 1;\n"
				+ "		} else {\n"
				+ "			return 0;\n"
				+ "		}\n"
				+ "	}";
		String expected = ""
				+ "	int test() {\n"
				+ "		boolean condition = true;\n"
				+ " 	return condition ? 1 : 0;\n"
				+ "	}";

		assertChange(original, expected);
	}

	/**
	 * This test is expected to fail as soon as the corresponding visitor will
	 * have been implemented.
	 * 
	 */
	@Test
	public void visit_exampleWithReturnInsteadOfElse_shouldTransform() throws Exception {
		String original = ""
				+ "	int test() {\n"
				+ "		boolean condition = true;\n"
				+ "		if (condition) {\n"
				+ "			return 1;\n"
				+ "		}\n"
				+ "		return 0;\n"
				+ "	}";
		String expected = ""
				+ "	int test() {\n"
				+ "		boolean condition = true;\n"
				+ " 	return condition ? 1 : 0;\n"
				+ "	}";

		assertChange(original, expected);
	}
}
