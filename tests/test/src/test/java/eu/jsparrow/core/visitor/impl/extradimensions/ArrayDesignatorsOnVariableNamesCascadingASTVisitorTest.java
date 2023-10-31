package eu.jsparrow.core.visitor.impl.extradimensions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;

class ArrayDesignatorsOnVariableNamesCascadingASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new ArrayDesignatorsOnVariableNamesASTVisitor());
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_cascadingLocalVariableDeclarations_shouldTransform() throws Exception {

		String original = "" +
				"	void cascadingLocalVariableDeclarations() {\n" +
				"\n" +
				"		Runnable r = new Runnable() {\n" +
				"\n" +
				"			@Override\n" +
				"			public void run() {\n" +
				"				int x1, x2[], x3[][];\n" +
				"			}\n" +
				"\n" +
				"		}, r2[] = { new Runnable() {\n" +
				"\n" +
				"			@Override\n" +
				"			public void run() {\n" +
				"				int x1, x2[], x3[][];\n" +
				"			}\n" +
				"\n" +
				"		}, () -> {\n" +
				"			int x1, x2[], x3[][];\n" +
				"		} };\n" +
				"	}";

		String expected = "" +
				"	void cascadingLocalVariableDeclarations() {\n" +
				"\n" +
				"		Runnable r = new Runnable() {\n" +
				"\n" +
				"			@Override\n" +
				"			public void run() {\n" +
				"				int x1;\n" +
				"				int[] x2;\n" +
				"				int[][] x3;\n" +
				"			}\n" +
				"\n" +
				"		};\n" +
				"		Runnable[] r2 = { new Runnable() {\n" +
				"\n" +
				"			@Override\n" +
				"			public void run() {\n" +
				"				int x1;\n" +
				"				int[] x2;\n" +
				"				int[][] x3;\n" +
				"			}\n" +
				"\n" +
				"		}, () -> {\n" +
				"			int x1;\n" +
				"			int[] x2;\n" +
				"			int[][] x3;\n" +
				"		} };\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	void visit_cascadingFieldDeclarations_shouldTransform() throws Exception {
		String original = ""
				+ "		Runnable r = new Runnable() {\n"
				+ "			int x1 = 0, x2[] = {}, x3[][] = { {}, {} };\n"
				+ "\n"
				+ "			@Override\n"
				+ "			public void run() {\n"
				+ "			}\n"
				+ "\n"
				+ "		}, r2[] = { new Runnable() {\n"
				+ "			int x1 = 0, x2[] = {}, x3[][] = { {}, {} };\n"
				+ "\n"
				+ "			@Override\n"
				+ "			public void run() {\n"
				+ "			}\n"
				+ "\n"
				+ "		}, new Runnable() {\n"
				+ "			int x1 = 0, x2[] = {}, x3[][] = { {}, {} };\n"
				+ "\n"
				+ "			@Override\n"
				+ "			public void run() {\n"
				+ "			}\n"
				+ "		} };";

		String expected = ""
				+ "		Runnable r = new Runnable() {\n"
				+ "			int x1 = 0;\n"
				+ "			int[] x2 = {};\n"
				+ "			int[][] x3 = { {}, {} };\n"
				+ "\n"
				+ "			@Override\n"
				+ "			public void run() {\n"
				+ "			}\n"
				+ "\n"
				+ "		};"
				+ "		Runnable[] r2 = { new Runnable() {\n"
				+ "			int x1 = 0;\n"
				+ "			int[] x2 = {};\n"
				+ "			int[][] x3 = { {}, {} };\n"
				+ "\n"
				+ "			@Override\n"
				+ "			public void run() {\n"
				+ "			}\n"
				+ "\n"
				+ "		}, new Runnable() {\n"
				+ "			int x1 = 0;\n"
				+ "			int[] x2 = {};\n"
				+ "			int[][] x3 = { {}, {} };\n"
				+ "\n"
				+ "			@Override\n"
				+ "			public void run() {\n"
				+ "			}\n"
				+ "		} };";

		assertChange(original, expected);
	}

	// @Test
	// void visit__shouldTransform() throws Exception {
	// String original = "";
	// String expected = "";
	// assertChange(original, expected);
	// }
}
