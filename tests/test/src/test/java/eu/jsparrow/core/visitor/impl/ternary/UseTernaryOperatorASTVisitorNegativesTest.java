package eu.jsparrow.core.visitor.impl.ternary;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;

@SuppressWarnings({ "nls" })
public class UseTernaryOperatorASTVisitorNegativesTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setDefaultVisitor(new UseTernaryOperatorASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_AssignmentWhenTrueWithoutElse_shouldNotTransform() throws Exception {
		String original = ""
				+ "		void test() {\n"
				+ "			boolean condition = true;\n"
				+ "			int x;\n"
				+ "			if (condition) {\n"
				+ "				x = 1;\n"
				+ "			}\n"
				+ "		}";
		assertNoChange(original);
	}

	@Test
	void visit_AssignmentWhenTrueEmptyElse_shouldNotTransform() throws Exception {
		String original = ""
				+ "		void test() {\n"
				+ "			boolean condition = true;\n"
				+ "			int x;\n"
				+ "			if (condition) {\n"
				+ "				x = 1;\n"
				+ "			} else {\n"
				+ "			}\n"
				+ "		}";
		assertNoChange(original);
	}

	@Test
	void visit_AssignmentWhenTrueTwoAssignmentsWhenElse_shouldNotTransform() throws Exception {
		String original = ""
				+ "		void test() {\n"
				+ "			boolean condition = true;\n"
				+ "			int x;\n"
				+ "			if (condition) {\n"
				+ "				x = 1;\n"
				+ "			} else {\n"
				+ "				x = 2;\n"
				+ "				x = 3;\n"
				+ "			}\n"
				+ "		}";
		assertNoChange(original);
	}

	@Test
	void visit_AssignmentWhenTrueMethodInvocationWhenElse_shouldNotTransform() throws Exception {
		String original = ""
				+ "		void test() {\n"
				+ "			boolean condition = true;\n"
				+ "			int x;\n"
				+ "			if (condition) {\n"
				+ "				x = 1;\n"
				+ "			} else {\n"
				+ "				System.out.println();\n"
				+ "			}\n"
				+ "		}";
		assertNoChange(original);
	}

	@Test
	void visit_AssigningDifferentVariables_shouldNotTransform() throws Exception {
		String original = ""
				+ "	void test() {\n"
				+ "		boolean condition = true;\n"
				+ "		int x1;\n"
				+ "		int x2;\n"
				+ "		if (condition) {\n"
				+ "			x1 = 1;\n"
				+ "		} else {\n"
				+ "			x2 = 0;\n"
				+ "		}\n"
				+ "	}";
		assertNoChange(original);
	}

	@Test
	void visit_usingDifferentAssignOperators_shouldNotTransform() throws Exception {
		String original = ""
				+ "	void test() {\n"
				+ "		boolean condition = true;\n"
				+ "		int x;\n"
				+ "		if (condition) {\n"
				+ "			x += 1;\n"
				+ "		} else {\n"
				+ "			x -= 1;\n"
				+ "		}\n"
				+ "	}";
		assertNoChange(original);
	}

	@Test
	void visit_assignIntWhenTrueIntegerWhenFalse_shouldNotTransform() throws Exception {
		String original = ""
				+ "		void test() {\n"
				+ "			boolean condition = true;\n"
				+ "			int x;\n"
				+ "			if (condition) {\n"
				+ "				x = 1;\n"
				+ "			} else {\n"
				+ "				x = Integer.valueOf(0);\n"
				+ "			}\n"
				+ "		}";
		assertNoChange(original);
	}

	@Test
	void visit_assignIntegerWhenTrueIntWhenFalse_shouldNotTransform() throws Exception {
		String original = ""
				+ "		void test() {\n"
				+ "			boolean condition = true;\n"
				+ "			int x;\n"
				+ "			if (condition) {\n"
				+ "				x = Integer.valueOf(1);\n"
				+ "			} else {\n"
				+ "				x = 0;\n"
				+ "			}\n"
				+ "		}";
		assertNoChange(original);
	}
	
	@Test
	void visit_returnWhenTrueEmptyElse_shouldNotTransform() throws Exception {
		String original = ""
				+ "		int test() {\n"
				+ "			boolean condition = true;\n"
				+ "			if (condition) {\n"
				+ "				return 1;\n"
				+ "			} else {\n"
				+ "			}\n"
				+ "			return 0;\n"
				+ "		}";
		assertNoChange(original);
	}
	
	@Test
	void visit_returnWhenTrueMethodCallWhenElse_shouldNotTransform() throws Exception {
		String original = ""
				+ "		int test() {\n"
				+ "			boolean condition = true;\n"
				+ "			if (condition) {\n"
				+ "				return 1;\n"
				+ "			} else {\n"
				+ "				System.out.println();\n"
				+ "			}\n"
				+ "			return 0;\n"
				+ "		}";
		assertNoChange(original);
	}
	
	@Test
	void visit_returnWhenTrueNoElseAndNoReturnAfterIf_shouldNotTransform() throws Exception {
		String original = ""
				+ "		int test() {\n"
				+ "			boolean condition = true;\n"
				+ "			if (condition) {\n"
				+ "				return 1;\n"
				+ "			}\n"
				+ "			System.out.println();\n"
				+ "			return 0;\n"
				+ "		}";
		assertNoChange(original);
	}
	
	@Test
	void visit_returnIntWhenTrueIntegerWhenFalse_shouldNotTransform() throws Exception {
		String original = ""
				+ "		int test() {\n"
				+ "			boolean condition = true;\n"
				+ "			if (condition) {\n"
				+ "				return 1;\n"
				+ "			} else {\n"
				+ "				return Integer.valueOf(0);\n"
				+ "			}\n"
				+ "		}";
		assertNoChange(original);
	}
}
