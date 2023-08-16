package eu.jsparrow.core.visitor.impl.inline;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;

@SuppressWarnings({ "nls" })
public class InlineLocalVariablesASTVisitorNegtivesTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setDefaultVisitor(new InlineLocalVariablesASTVisitor());
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_NoInitializer_shouldNotTransform() throws Exception {
		String original = "" +
				"	void noInitializer() {\n" +
				"		int sum;\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	void visit_DeclarationAsForLoopCounter_shouldNotTransform() throws Exception {
		String original = "" +
				"	void declarationAsForLoopCounter() {\n" +
				"		for (int i = 0; i < 10; i++) {\n" +
				"		}\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	void visit_MultipleVariableDeclaration_shouldNotTransform() throws Exception {
		String original = "" +
				"	int multipleVariableDecaration(int a) {\n" +
				"		int square = a * a, cube = a * a * a;\n" +
				"		return square;\n" +
				"	}";
		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"int a, int b",
			"int a, Integer b",
			"Integer a, int b",
			"Integer a, Integer b",
	})
	void visit_BoxingInitializer_shouldNotTransform(String parameters) throws Exception {
		String initializer = "a + b";
		String original = "" +
				"	Integer boxingInitializer(" + parameters + ") {\n" +
				"		Integer result = " + initializer + ";\n" +
				"		return result;\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	void visit_UnboxingInitializer_shouldNotTransform() throws Exception {
		String original = "" +
				"	int unboxingInitializer(Integer a) {\n" +
				"		int result = a;\n" +
				"		return result;\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	void visit_UsedMoreThanOnce_shouldNotTransform() throws Exception {
		String original = "" +
				"	int usedMoreThanOnce() {\n" +
				"		int x = 1;\n" +
				"		int y = x;\n" +
				"		return x;\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	void visit_IncrementAsOnlyUse_shouldNotTransform() throws Exception {
		String original = "" +
				"	void incrementAsOnlyUse() {\n" +
				"		int x = 0;\n" +
				"		++x;\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	void visit_StatementBetweenDeclarationAndReturn_shouldNotTransform() throws Exception {
		String original = "" +
				"	int statementBetweenDeclarationAndReturn(int a) {\n" +
				"		int square = a * a;\n" +
				"		a += 1;\n" +
				"		return square;\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	void visit_StatementBetweenDeclarationAndThrow_shouldNotTransform() throws Exception {
		String original = "" +
				"	void statementBetweenDeclarationAndThrow(String message) throws Exception {\n" +
				"		Exception exception = new Exception(message);\n" +
				"		message = \"***\" + message + \"***\";\n" +
				"		throw exception;\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	void visit_ConditionalReturnAfterDeclaration_shouldNotTransform() throws Exception {
		String original = "" +
				"		int conditionalReturnAfterDeclaration(int a) {\n"
				+ "			int square = a * a;\n"
				+ "			// a changed in if condition\n"
				+ "			if ((a = a + 2) < 0)\n"
				+ "				return square;\n"
				+ "			\n"
				+ "			return 2;\n"
				+ "		}";
		assertNoChange(original);
	}

	@Test
	void visit_variableUsedInAssignment_shouldNotTransform() throws Exception {
		String original = "" +
				"	int x;\n" +
				"	void useInAssignment(int pValue) {\n" +
				"		int a = pValue + 1;\n" +
				"		int b = (x = a);\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	void visit_variableUsedInVariableDeclarationExpression_shouldNotTransform() throws Exception {
		String original = "" +
				"		int x;\n" +
				"		void useInAssignment(int pValue) {\n" +
				"			int a = pValue + 1;\n" +
				"			for(int i = a; true; ) {\n" +
				"				break;\n" +
				"			}\n" +
				"		}";
		assertNoChange(original);
	}

	@Test
	void visit_variableUsedInAnnotation_shouldNotTransform() throws Exception {
		String original = "" +
				"	@interface ExampleAnnotation {\n" +
				"		int value();\n" +
				"	}\n" +
				"\n" +
				"	void useInAnnotation() {\n" +
				"		final int x = 1;\n" +
				"			@ExampleAnnotation(value = x)\n" +
				"		class LocalClass {\n" +
				"\n" +
				"		}\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	void visit_VariableUsedMoreThanTwice_shouldNotTransform() throws Exception {
		String original = "" +
				"	int variableUsedMoreThanTwice() {\n" +
				"		int x = 1;\n" +
				"		int y = x;\n" +
				"		int z = x;\n" +
				"		return x;\n" +
				"	}";
		assertNoChange(original);
	}

	// @Test
	// void visit__shouldNotTransform() throws Exception {
	// String original = "" +
	// "";
	// assertNoChange(original);
	// }

}
