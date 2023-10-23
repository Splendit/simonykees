package eu.jsparrow.core.visitor.impl.extradimensions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;

class ArrayDesignatorsOnVariableNamesASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new ArrayDesignatorsOnVariableNamesASTVisitor());
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_extraDimensionOnLocalVariableName_shouldTransform() throws Exception {

		String original = ""
				+ "	void extraDimensionOnLocalVariableName() {\n"
				+ "		int iArray[] = { 1, 2, 3 };\n"
				+ "	}";
		String expected = ""
				+ "	void extraDimensionOnLocalVariableName() {\n"
				+ "		int[] iArray = { 1, 2, 3 };\n"
				+ "	}";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"int iArray2D[][] = { { 1, 2 }, { 3, 4 } };",
			"int[] iArray2D[] = { { 1, 2 }, { 3, 4 } };",
	})
	void visit_Local2DimensionalArrayVariables_shouldTransform(String variableDeclaration) throws Exception {
		String original = ""
				+ "	void local2DimensionalArrayVariables() {\n"
				+ variableDeclaration + "\n"
				+ "	}";

		String expected = ""
				+ "	void local2DimensionalArrayVariables() {\n"
				+ "int[][] iArray2D={{1,2},{3,4}};\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_extraDimensionOnFieldName_shouldTransform() throws Exception {

		String original = "int iArray[] = {1,2,3};";
		String expected = "int[] iArray = {1,2,3};";

		assertChange(original, expected);
	}

	@Test
	void visit_extraDimensionsOnParameterName_shouldTransform() throws Exception {

		String original = "" +
				"	void extraDimensionsOnParameterName(int iArray[]) {\n" +
				"	}";

		String expected = "" +
				"	void extraDimensionsOnParameterName(int[] iArray) {\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	void visit_NoExtraDimensionOnLocalVariableName_shouldNotTransform() throws Exception {

		String original = "" +
				"	void noExtraDimensionsOnLocalVariableName() {\n" +
				"		int i = 1;\n" +
				"	}";
		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"		InnerClass @X [] x[];",
			"		InnerClass.@X InnerClass2 x[];",
			"		InnerClass.InnerClass2.@X InnerClass3 x[];",
			"		InnerClass.@X InnerClass2.@X InnerClass3 x[];",
			"		java.util.List<@X InnerClass> x[];",
	})
	void visit_TypesContainingAnnotations_shouldNotTransform(String variableDeclaration) throws Exception {

		String original = "" +
				"	void typeContainingAnnotations() {\n" +
				variableDeclaration + "\n" +
				"	}\n" +
				"\n" +
				"	@interface X {\n" +
				"	}\n" +
				"\n" +
				"	class InnerClass {\n" +
				"		class InnerClass2 {\n" +
				"			class InnerClass3 {\n" +
				"			}\n" +
				"		}\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	void visit_ExtraDimensionsWithAnnotations_shouldNotTransform() throws Exception {

		String original = "" +
				"	void extraDimensionsWithAnnotations() {\n" +
				"		int i2Dim @X [] @X [];\n" +
				"	}\n" +
				"\n" +
				"	@interface X {\n" +
				"	}";

		assertNoChange(original);
	}

	// @Test
	// void visit__shouldTransform() throws Exception {
	// String original = "";
	// String expected = "";
	// assertChange(original, expected);
	// }
}
