package org.eu.jsparrow.rules.java16.switchexpression;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.UseSwitchExpressionASTVisitor;

public class BugfixSim2119SwitchTest extends UsesJDTUnitFixture {

	private static final String STRING_CONSTANTS = ""
			+ "	static final String NEGATIVE = \"NEGATIVE\";\n"
			+ "	static final String ZERO = \"ZERO\";\n"
			+ "	static final String ONE = \"ONE\";\n"
			+ "	static final String TWO = \"TWO\";		\n"
			+ "	static final String GREATER_THAN_TWO = \"GREATER THAN TWO\";\n"
			+ "	static final String OTHER = \"OTHER\";		";

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
	void visit_NoAssignmentOfSwitchExpressionToSimpleName_shouldTransform() throws Exception {
		String original = STRING_CONSTANTS + "\n"
				+ "		String result;\n"
				+ "\n"
				+ "		void assignmentsWithinSwitch(int value) {\n"
				+ "			switch (value) {\n"
				+ "			case 0:\n"
				+ "				result = ZERO;\n"
				+ "				break;\n"
				+ "			case 1:\n"
				+ "				result = ONE;\n"
				+ "				break;\n"
				+ "\n"
				+ "			case 2:\n"
				+ "				result = TWO;\n"
				+ "				break;\n"
				+ "\n"
				+ "			default:\n"
				+ "				String result;\n"
				+ "				result = OTHER;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "		}";

		String expected = STRING_CONSTANTS + "\n"
				+ "	String result;\n"
				+ "\n"
				+ "		void assignmentsWithinSwitch(int value) {\n"
				+ "			switch (value) {\n"
				+ "			case 0 -> result = ZERO;\n"
				+ "			case 1 -> result = ONE;\n"
				+ "			case 2 -> result = TWO;\n"
				+ "			default -> {\n"
				+ "				String result;\n"
				+ "				result = OTHER;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			}\n"
				+ "		}";

		assertChange(original, expected);
	}

	@Test
	void visit_NoAssignmentOfSwitchExpressionToQualifiedName_shouldTransform() throws Exception {

		String original = STRING_CONSTANTS + "\n"
				+ "		StringWrapper resultWrapper;\n"
				+ "\n"
				+ "		void assignmentsWithinSwitch(int value) {\n"
				+ "			switch (value) {\n"
				+ "			case 0:\n"
				+ "				resultWrapper.content = ZERO;\n"
				+ "				break;\n"
				+ "			case 1:\n"
				+ "				resultWrapper.content = ONE;\n"
				+ "				break;\n"
				+ "\n"
				+ "			case 2:\n"
				+ "				resultWrapper.content = TWO;\n"
				+ "				break;\n"
				+ "\n"
				+ "			default:\n"
				+ "				StringWrapper resultWrapper = new StringWrapper();\n"
				+ "				resultWrapper.content = OTHER;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "		}\n"
				+ "\n"
				+ "		static class StringWrapper {\n"
				+ "			String content;\n"
				+ "		}";

		String expected = STRING_CONSTANTS + "\n"
				+ "		StringWrapper resultWrapper;\n"
				+ "\n"
				+ "		void assignmentsWithinSwitch(int value) {\n"
				+ "			switch (value) {\n"
				+ "			case 0 -> resultWrapper.content = ZERO;\n"
				+ "			case 1 -> resultWrapper.content = ONE;\n"
				+ "			case 2 -> resultWrapper.content = TWO;\n"
				+ "			default -> {\n"
				+ "				StringWrapper resultWrapper = new StringWrapper();\n"
				+ "				resultWrapper.content = OTHER;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			}\n"
				+ "		}\n"
				+ "		\n"
				+ "		static class StringWrapper {\n"
				+ "			String content;\n"
				+ "		}";

		assertChange(original, expected);
	}

	@Test
	void visit_NoAssignmentOfSwitchExpressionToFieldAccess_shouldTransform() throws Exception {

		String original = STRING_CONSTANTS + "\n"
				+ "		StringWrapper resultWrapper;\n"
				+ "\n"
				+ "		void assignmentsWithinSwitch(int value) {\n"
				+ "			switch (value) {\n"
				+ "			case 0:\n"
				+ "				getStringWrapper().content = ZERO;\n"
				+ "				break;\n"
				+ "			case 1:\n"
				+ "				getStringWrapper().content = ONE;\n"
				+ "				break;\n"
				+ "\n"
				+ "			case 2:\n"
				+ "				getStringWrapper().content = TWO;\n"
				+ "				break;\n"
				+ "\n"
				+ "			default:\n"
				+ "				getStringWrapper().content = OTHER;\n"
				+ "				break;\n"
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
				+ "		void assignmentsWithinSwitch(int value) {\n"
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
				+ "	static class SampleClassWithSwitch extends SuperClass {\n"
				+ "		void assignmentsWithinSwitch(int value) {\n"
				+ "			switch (value) {\n"
				+ "			case 0:\n"
				+ "				super.result = ZERO;\n"
				+ "				break;\n"
				+ "			case 1:\n"
				+ "				super.result = ONE;\n"
				+ "				break;\n"
				+ "\n"
				+ "			case 2:\n"
				+ "				super.result = TWO;\n"
				+ "				break;\n"
				+ "\n"
				+ "			default:\n"
				+ "				String result;\n"
				+ "				super.result = OTHER;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}\n"
				+ "	\n"
				+ "	static class SuperClass {\n"
				+ "		String result;\n"
				+ "	}";

		String expected = STRING_CONSTANTS + "\n"
				+ "	static class SampleClassWithSwitch extends SuperClass{\n"
				+ "		void assignmentsWithinSwitch(int value) {\n"
				+ "			super.result = switch (value) {\n"
				+ "			case 0 -> ZERO;\n"
				+ "			case 1 -> ONE;\n"
				+ "			case 2 -> TWO;\n"
				+ "			default -> {\n"
				+ "				String result;\n"
				+ "				yield OTHER;\n"
				+ "			}\n"
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
				+ "		void assignmentsWithinSwitch(int value) {\n"
				+ "			switch (value) {\n"
				+ "			case 0:\n"
				+ "				this.result = ZERO;\n"
				+ "				break;\n"
				+ "			case 1:\n"
				+ "				this.result = ONE;\n"
				+ "				break;\n"
				+ "\n"
				+ "			case 2:\n"
				+ "				this.result = TWO;\n"
				+ "				break;\n"
				+ "\n"
				+ "			default:\n"
				+ "				String result;\n"
				+ "				this.result = OTHER;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "		}";

		String expected = STRING_CONSTANTS + "\n"
				+ "		String result;\n"
				+ "\n"
				+ "		void assignmentsWithinSwitch(int value) {\n"
				+ "			this.result = switch (value) {\n"
				+ "			case 0 -> ZERO;\n"
				+ "			case 1 -> ONE;\n"
				+ "			case 2 -> TWO;\n"
				+ "			default -> {\n"
				+ "				String result;\n"
				+ "				yield OTHER;\n"
				+ "			}\n"
				+ "			};\n"
				+ "		}";

		assertChange(original, expected);
	}
}
