package eu.jsparrow.core.visitor.impl.ternary;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

	@Test
	void visit_returnInMultibranchIf_shouldNotTransform() throws Exception {
		String original = ""
				+ "	int test(boolean condition1, boolean condition2) {\n"
				+ "		if (condition1) {\n"
				+ "			return 1;\n"
				+ "		} else if (condition2) {\n"
				+ "			return 2;\n"
				+ "		} else {\n"
				+ "			return 0;\n"
				+ "		}\n"
				+ "	}";
		assertNoChange(original);
	}

	@Test
	void visit_emptyBlockWhenTrue_shouldNotTransform() throws Exception {
		String original = ""
				+ "	int test() {\n"
				+ "		boolean condition = true;\n"
				+ "		if(condition) {\n"
				+ "		} else {\n"
				+ "			return 0;\n"
				+ "		}\n"
				+ "		return 1;\n"
				+ "	}";
		assertNoChange(original);
	}

	@Test
	void visit_IfContainingIfContainingReturnFollowedByReturn_shouldNotTransform() throws Exception {
		String original = ""
				+ "	int test(boolean condition1, boolean condition2) {\n"
				+ "		if (condition1)\n"
				+ "			if (condition2) {\n"
				+ "				return 2;\n"
				+ "			}\n"
				+ "		return 0;\n"
				+ "	}";
		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			""
					+ "		void test(int pX_123456789_Y_123456789_Z_123456789) {\n"
					+ "			boolean condition = true;\n"
					+ "			int x;\n"
					+ "			if (condition) {\n"
					+ "				x = pX_123456789_Y_123456789_Z_123456789;\n"
					+ "			} else {\n"
					+ "				x = 0;\n"
					+ "			}\n"
					+ "		}",
			""
					+ "		void test(int pX_123456789_Y_123456789_Z_123456789) {\n"
					+ "			boolean condition = true;\n"
					+ "			int x;\n"
					+ "			if (condition) {\n"
					+ "				x = 1;\n"
					+ "			} else {\n"
					+ "				x = pX_123456789_Y_123456789_Z_123456789;\n"
					+ "			}\n"
					+ "		}",
			""
					+ "		void test(int pX_123456789_Y_123456789_Z_123456789) {\n"
					+ "			int x;\n"
					+ "			if (pX_123456789_Y_123456789_Z_123456789 < 0) {\n"
					+ "				x = 1;\n"
					+ "			} else {\n"
					+ "				x = 0;\n"
					+ "			}\n"
					+ "		}"
	})
	void visit_largeSimpleNames_shouldNotTransform(String original) throws Exception {
		assertNoChange(original);
	}

	@Test
	void visit_assignTernaryExpression_shouldNotTransform() throws Exception {
		String original = ""
				+ "	void test(boolean condition, boolean condition2) {\n"
				+ "		int x;\n"
				+ "		if(condition) {\n"
				+ "			x = condition2 ? 10 : 1;\n"
				+ "		} else {\n"
				+ "			x = 0;\n"
				+ "		}\n"
				+ "	}";
		assertNoChange(original);
	}
}
