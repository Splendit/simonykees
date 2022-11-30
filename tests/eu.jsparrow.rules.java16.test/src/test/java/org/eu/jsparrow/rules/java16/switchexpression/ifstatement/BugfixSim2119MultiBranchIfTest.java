package org.eu.jsparrow.rules.java16.switchexpression.ifstatement;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.ifstatement.ReplaceMultiBranchIfBySwitchASTVisitor;

public class BugfixSim2119MultiBranchIfTest extends UsesJDTUnitFixture {

	private static final String STRING_CONSTANTS = ""
			+ "	static final String NEGATIVE = \"NEGATIVE\";\n"
			+ "	static final String ZERO = \"ZERO\";\n"
			+ "	static final String ONE = \"ONE\";\n"
			+ "	static final String TWO = \"TWO\";		\n"
			+ "	static final String GREATER_THAN_TWO = \"GREATER THAN TWO\";\n"
			+ "	static final String OTHER = \"OTHER\";		";

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
	void visit_NoAssignmentOfSwitchExpressionToSimpleName_shouldTransform() throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "		String result;\n"
				+ "\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			if (value == 0) {\n"
				+ "				result = ZERO;\n"
				+ "			} else if (value == 1) {\n"
				+ "				String result;\n"
				+ "				result = ONE;\n"
				+ "			} else if (value == 2) {\n"
				+ "				result = TWO;\n"
				+ "			} else {\n"
				+ "				result = OTHER;\n"
				+ "			}\n"
				+ "		}";

		String expected = STRING_CONSTANTS + "\n"
				+ "	String result;\n"
				+ "\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			switch (value) {\n"
				+ "			case 0 -> result = ZERO;\n"
				+ "			case 1 -> {\n"
				+ "				String result;\n"
				+ "				result = ONE;\n"
				+ "				 break;\n"
				+ "			}\n"
				+ "			case 2 -> result = TWO;\n"
				+ "			default -> result = OTHER;\n"
				+ "			}\n"
				+ "		}";

		assertChange(original, expected);
	}

	@Test
	void visit_NoAssignmentOfSwitchExpressionToQualifiedName_shouldTransform() throws Exception {

		String original = STRING_CONSTANTS + "\n"
				+ "		StringWrapper resultWrapper;\n"
				+ "\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			if (value == 0) {\n"
				+ "				resultWrapper.content = ZERO;\n"
				+ "			} else if (value == 1) {\n"
				+ "				StringWrapper resultWrapper = new StringWrapper();\n"
				+ "				resultWrapper.content = ONE;\n"
				+ "			} else if (value == 2) {\n"
				+ "				resultWrapper.content = TWO;\n"
				+ "			} else {\n"
				+ "				resultWrapper.content = OTHER;\n"
				+ "			}\n"
				+ "		}\n"
				+ "\n"
				+ "		static class StringWrapper {\n"
				+ "			String content;\n"
				+ "		}";

		String expected = STRING_CONSTANTS + "\n"
				+ "		StringWrapper resultWrapper;\n"
				+ "\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			switch (value) {\n"
				+ "			case 0 -> resultWrapper.content = ZERO;\n"
				+ "			case 1 -> {\n"
				+ "				StringWrapper resultWrapper = new StringWrapper();\n"
				+ "				resultWrapper.content = ONE;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			case 2 -> resultWrapper.content = TWO;\n"
				+ "			default -> resultWrapper.content = OTHER;\n"
				+ "			}\n"
				+ "		}\n"
				+ "		\n"
				+ "		static class StringWrapper {\n"
				+ "			String content;\n"
				+ "		}";

		assertChange(original, expected);
	}

	@Test
	void visit_ExpectAssignmentOfSwitchExpressionToQualifiedName_shouldTransform() throws Exception {

		String original = STRING_CONSTANTS + "\n"
				+ "	void assignmentsWithinIf(int value) {\n"
				+ "		StringWrapper resultWrapper = new StringWrapper();\n"
				+ "		if (value == 0) {\n"
				+ "			resultWrapper.content = ZERO;\n"
				+ "		} else if (value == 1) {\n"
				+ "			resultWrapper.content = ONE;\n"
				+ "		} else if (value == 2) {\n"
				+ "			resultWrapper.content = TWO;\n"
				+ "		} else {\n"
				+ "			resultWrapper.content = OTHER;\n"
				+ "		}\n"
				+ "	}";

		String expected = STRING_CONSTANTS + "\n"
				+ "	void assignmentsWithinIf(int value) {\n"
				+ "		StringWrapper resultWrapper = new StringWrapper();\n"
				+ "		resultWrapper.content = switch (value) {\n"
				+ "		case 0 -> ZERO;\n"
				+ "		case 1 -> ONE;\n"
				+ "		case 2 -> TWO;\n"
				+ "		default -> OTHER;\n"
				+ "		};\n"
				+ "	}";
		assertChange(original, expected);
	}

	@Test
	void visit_NoAssignmentOfSwitchExpressionToFieldAccess_shouldTransform() throws Exception {

		String original = STRING_CONSTANTS + "\n"
				+ "		StringWrapper resultWrapper;\n"
				+ "\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			if (value == 0) {\n"
				+ "				getStringWrapper().content = ZERO;\n"
				+ "			} else if (value == 1) {\n"
				+ "				getStringWrapper().content = ONE;\n"
				+ "			} else if (value == 2) {\n"
				+ "				getStringWrapper().content = TWO;\n"
				+ "			} else {\n"
				+ "				getStringWrapper().content = OTHER;\n"
				+ "			}\n"
				+ "		}\n"
				+ "\n"
				+ "		StringWrapper getStringWrapper() {\n"
				+ "			return new StringWrapper();\n"
				+ "		}\n"
				+ "\n"
				+ "		static class StringWrapper {\n"
				+ "			String content;\n"
				+ "		}";

		String expected = STRING_CONSTANTS + "\n"
				+ "		StringWrapper resultWrapper;\n"
				+ "\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			switch (value) {\n"
				+ "			case 0 -> getStringWrapper().content = ZERO;\n"
				+ "			case 1 -> getStringWrapper().content = ONE;\n"
				+ "			case 2 -> getStringWrapper().content = TWO;\n"
				+ "			default -> getStringWrapper().content = OTHER;\n"
				+ "			}\n"
				+ "		}\n"
				+ "\n"
				+ "		StringWrapper getStringWrapper() {\n"
				+ "			return new StringWrapper();\n"
				+ "		}\n"
				+ "\n"
				+ "		static class StringWrapper {\n"
				+ "			String content;\n"
				+ "		}";

		assertChange(original, expected);
	}

	@Test
	void visit_AssignmentOfSwitchExpressionToSuperFieldAccess_shouldTransform() throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "	static class SampleClass extends SuperClass{\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			if (value == 0) {\n"
				+ "				super.result = ZERO;\n"
				+ "			} else if (value == 1) {				\n"
				+ "				super.result = ONE;\n"
				+ "			} else if (value == 2) {\n"
				+ "				super.result = TWO;\n"
				+ "			} else {\n"
				+ "				super.result = OTHER;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}\n"
				+ "	\n"
				+ "	static class SuperClass {\n"
				+ "		String result;\n"
				+ "	}";

		String expected = STRING_CONSTANTS + "\n"
				+ "	static class SampleClass extends SuperClass{\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			super.result = switch (value) {\n"
				+ "			case 0 -> ZERO;\n"
				+ "			case 1 -> ONE;\n"
				+ "			case 2 -> TWO;\n"
				+ "			default -> OTHER;\n"
				+ "			};\n"
				+ "		}\n"
				+ "	}\n"
				+ "	\n"
				+ "	static class SuperClass {\n"
				+ "		String result;\n"
				+ "	}";

		assertChange(original, expected);

	}

	@Test
	void visit_AssignmentOfSwitchExpressionToThisFieldAccess_shouldTransform() throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "		String result;\n"
				+ "\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			if (value == 0) {\n"
				+ "				this.result = ZERO;\n"
				+ "			} else if (value == 1) {				\n"
				+ "				this.result = ONE;\n"
				+ "			} else if (value == 2) {\n"
				+ "				this.result = TWO;\n"
				+ "			} else {\n"
				+ "				this.result = OTHER;\n"
				+ "			}\n"
				+ "		}";

		String expected = STRING_CONSTANTS + "\n"
				+ "		String result;\n"
				+ "\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			this.result = switch (value) {\n"
				+ "			case 0 -> ZERO;\n"
				+ "			case 1 -> ONE;\n"
				+ "			case 2 -> TWO;\n"
				+ "			default -> OTHER;\n"
				+ "			};\n"
				+ "		}";

		assertChange(original, expected);
	}

	@Test
	void visit_NoAssignmentOfSwitchExpressionToArrayAccessByLiteral_shouldTransform() throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			String[] strings = new String[3];\n"
				+ "			if (value == 0) {\n"
				+ "				strings[0] = ZERO;\n"
				+ "			} else if (value == 1) {\n"
				+ "				strings[0] = ONE;\n"
				+ "			} else if (value == 2) {\n"
				+ "				strings = new String[3];\n"
				+ "				strings[0] = TWO;\n"
				+ "			} else {\n"
				+ "				strings[0] = OTHER;\n"
				+ "			}\n"
				+ "		}";

		String expected = STRING_CONSTANTS + "\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			String[] strings = new String[3];\n"
				+ "			switch (value) {\n"
				+ "			case 0 -> strings[0] = ZERO;\n"
				+ "			case 1 -> strings[0] = ONE;\n"
				+ "			case 2 -> {\n"
				+ "				strings = new String[3];\n"
				+ "				strings[0] = TWO;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			default -> strings[0] = OTHER;\n"
				+ "			}\n"
				+ "		}";

		assertChange(original, expected);
	}

	@Test
	void visit_NoAssignmentOfSwitchExpressionToArrayAccessByIndexGetter_shouldTransform() throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "			void assignmentsWithinIf(int value) {\n"
				+ "				String[] strings = new String[3];\n"
				+ "				if (value == 0) {\n"
				+ "					strings[getIndex()] = ZERO;\n"
				+ "				} else if (value == 1) {\n"
				+ "					strings[getIndex()] = ONE;\n"
				+ "				} else if (value == 2) {\n"
				+ "					strings[getIndex()] = TWO;\n"
				+ "				} else {\n"
				+ "					strings[getIndex()] = OTHER;\n"
				+ "				}\n"
				+ "			}";

		String expected = STRING_CONSTANTS + "\n"
				+ "			void assignmentsWithinIf(int value) {\n"
				+ "				String[] strings = new String[3];\n"
				+ "				switch (value) {\n"
				+ "				case 0 -> strings[getIndex()] = ZERO;\n"
				+ "				case 1 -> strings[getIndex()] = ONE;\n"
				+ "				case 2 -> strings[getIndex()] = TWO;\n"
				+ "				default -> strings[getIndex()] = OTHER;\n"
				+ "				}\n"
				+ "			}";

		assertChange(original, expected);
	}

	@Test
	void visit_NoAssignmentOfSwitchExpressionToElementZeroOfGetArray_shouldTransform() throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "	void assignmentsWithinIf(int value) {\n"
				+ "		if (value == 0) {\n"
				+ "			getStringArray()[0] = ZERO;\n"
				+ "		} else if (value == 1) {\n"
				+ "			getStringArray()[0] = ONE;\n"
				+ "		} else if (value == 2) {\n"
				+ "			getStringArray()[0] = TWO;\n"
				+ "		} else {\n"
				+ "			getStringArray()[0] = OTHER;\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	private String[] getStringArray() {\n"
				+ "		return new String[3];\n"
				+ "	}";

		String expected = STRING_CONSTANTS + "\n"
				+ "	void assignmentsWithinIf(int value) {\n"
				+ "		switch (value) {\n"
				+ "		case 0 -> getStringArray()[0] = ZERO;\n"
				+ "		case 1 -> getStringArray()[0] = ONE;\n"
				+ "		case 2 -> getStringArray()[0] = TWO;\n"
				+ "		default -> getStringArray()[0] = OTHER;\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	private String[] getStringArray() {\n"
				+ "		return new String[3];\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_ExpectAssignmentOfSwitchExpressionToArrayAccessByLiteral_shouldTransform() throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			String[] strings = new String[3];\n"
				+ "			if (value == 0) {\n"
				+ "				strings[0] = ZERO;\n"
				+ "			} else if (value == 1) {\n"
				+ "				strings[0] = ONE;\n"
				+ "			} else if (value == 2) {\n"
				+ "				strings[0] = TWO;\n"
				+ "			} else {\n"
				+ "				strings[0] = OTHER;\n"
				+ "			}\n"
				+ "		}";

		String expected = STRING_CONSTANTS + "\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			String[] strings = new String[3];\n"
				+ "			strings[0] = switch (value) {\n"
				+ "			case 0 -> ZERO;\n"
				+ "			case 1 -> ONE;\n"
				+ "			case 2 -> TWO;\n"
				+ "			default -> OTHER;\n"
				+ "			};\n"
				+ "		}";

		assertChange(original, expected);

	}

	@Test
	void visit_ExpectAssignmentOfSwitchExpressionToArrayAccessByVariable_shouldTransform() throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			String[] strings = new String[3];\n"
				+ "			int i = 0;\n"
				+ "			if (value == 0) {\n"
				+ "				strings[i] = ZERO;\n"
				+ "			} else if (value == 1) {\n"
				+ "				strings[i] = ONE;\n"
				+ "			} else if (value == 2) {\n"
				+ "				strings[i] = TWO;\n"
				+ "			} else {\n"
				+ "				strings[i] = OTHER;\n"
				+ "			}\n"
				+ "		}";

		String expected = STRING_CONSTANTS + "\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			String[] strings = new String[3];\n"
				+ "			int i = 0;\n"
				+ "			strings[i] = switch (value) {\n"
				+ "			case 0 -> ZERO;\n"
				+ "			case 1 -> ONE;\n"
				+ "			case 2 -> TWO;\n"
				+ "			default -> OTHER;\n"
				+ "			};\n"
				+ "		}";

		assertChange(original, expected);

	}

	@Test
	void visit_ExpectAssignmentOfSwitchExpressionToThisArrayAccess_shouldTransform() throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "		String[] strings = new String[3];\n"
				+ "		void assignmentsWithinIf(int value) {			\n"
				+ "			if (value == 0) {\n"
				+ "				this.strings[0] = ZERO;\n"
				+ "			} else if (value == 1) {\n"
				+ "				this.strings[0] = ONE;\n"
				+ "			} else if (value == 2) {\n"
				+ "				this.strings[0] = TWO;\n"
				+ "			} else {\n"
				+ "				this.strings[0] = OTHER;\n"
				+ "			}\n"
				+ "		}";

		String expected = STRING_CONSTANTS + "\n"
				+ "		String[] strings = new String[3];\n"
				+ "		void assignmentsWithinIf(int value) {			\n"
				+ "			this.strings[0] = switch (value) {\n"
				+ "			case 0 -> ZERO;\n"
				+ "			case 1 -> ONE;\n"
				+ "			case 2 -> TWO;\n"
				+ "			default -> OTHER;\n"
				+ "			};\n"
				+ "		}";

		assertChange(original, expected);

	}

	@Test
	void visit_ExpectAssignmentOfSwitchExpressionToSuperArrayAccess_shouldTransform() throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "	static class SampleSuperArrayAccessWithinIf extends SuperClass {\n"
				+ "\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			if (value == 0) {\n"
				+ "				super.strings[0] = ZERO;\n"
				+ "			} else if (value == 1) {\n"
				+ "				super.strings[0] = ONE;\n"
				+ "			} else if (value == 2) {\n"
				+ "				super.strings[0] = TWO;\n"
				+ "			} else {\n"
				+ "				super.strings[0] = OTHER;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}\n"
				+ "	\n"
				+ "	static class SuperClass {\n"
				+ "		String[] strings = new String[3];\n"
				+ "	}";

		String expected = STRING_CONSTANTS + "\n"
				+ "	static class SampleSuperArrayAccessWithinIf extends SuperClass {\n"
				+ "\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			super.strings[0] = switch (value) {\n"
				+ "			case 0 -> ZERO;\n"
				+ "			case 1 -> ONE;\n"
				+ "			case 2 -> TWO;\n"
				+ "			default -> OTHER;\n"
				+ "			};\n"
				+ "		}\n"
				+ "	}\n"
				+ "	\n"
				+ "	static class SuperClass {\n"
				+ "		String[] strings = new String[3];\n"
				+ "	}\n"
				+ "";

		assertChange(original, expected);

	}

	@Test
	void visit_NoAssignmentOfSwitchExpressionToDifferentVariables_shouldTransform() throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			String s1;\n"
				+ "			String s2;\n"
				+ "			String s3;\n"
				+ "			String s4;\n"
				+ "			if (value == 0) {\n"
				+ "				System.out.println(\"s1 = ZERO;\");\n"
				+ "				s1 = ZERO;\n"
				+ "			} else if (value == 1) {\n"
				+ "				System.out.println(\"s2 = ONE;\");\n"
				+ "				s2 = ONE;\n"
				+ "			} else if (value == 2) {\n"
				+ "				System.out.println(\"s3 = TWO;\");\n"
				+ "				s3 = TWO;\n"
				+ "			} else {\n"
				+ "				System.out.println(\"s4 = OTHER;\");\n"
				+ "				s4 = OTHER;\n"
				+ "			}\n"
				+ "		}";

		String expected = STRING_CONSTANTS + "\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			String s1;\n"
				+ "			String s2;\n"
				+ "			String s3;\n"
				+ "			String s4;\n"
				+ "			switch (value) {\n"
				+ "			case 0 -> {\n"
				+ "				System.out.println(\"s1 = ZERO;\");\n"
				+ "				s1 = ZERO;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			case 1 -> {\n"
				+ "				System.out.println(\"s2 = ONE;\");\n"
				+ "				s2 = ONE;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			case 2 -> {\n"
				+ "				System.out.println(\"s3 = TWO;\");\n"
				+ "				s3 = TWO;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			default -> {\n"
				+ "				System.out.println(\"s4 = OTHER;\");\n"
				+ "				s4 = OTHER;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			}\n"
				+ "		}";

		assertChange(original, expected);
	}

	@Test
	void visit_NoAssignmentOfSwitchExpressionToDifferentKindsOfExpression_shouldTransform() throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "		String result;\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			String result;\n"
				+ "			if (value == 0) {\n"
				+ "				System.out.println(ZERO);\n"
				+ "				result = ZERO;\n"
				+ "			} else if (value == 1) {\n"
				+ "				System.out.println(ONE);\n"
				+ "				this.result = ONE;\n"
				+ "			} else if (value == 2) {\n"
				+ "				System.out.println(TWO);\n"
				+ "				result = TWO;\n"
				+ "			} else {\n"
				+ "				System.out.println(OTHER);\n"
				+ "				result = OTHER;\n"
				+ "			}\n"
				+ "		}";

		String expected = STRING_CONSTANTS + "\n"
				+ "		String result;\n"
				+ "		void assignmentsWithinIf(int value) {\n"
				+ "			String result;\n"
				+ "			switch (value) {\n"
				+ "			case 0 -> {\n"
				+ "				System.out.println(ZERO);\n"
				+ "				result = ZERO;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			case 1 -> {\n"
				+ "				System.out.println(ONE);\n"
				+ "				this.result = ONE;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			case 2 -> {\n"
				+ "				System.out.println(TWO);\n"
				+ "				result = TWO;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			default -> {\n"
				+ "				System.out.println(OTHER);\n"
				+ "				result = OTHER;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			}\n"
				+ "		}";

		assertChange(original, expected);

	}

}
