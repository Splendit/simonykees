package eu.jsparrow.core.visitor.unused;

import static eu.jsparrow.core.rule.impl.unused.Constants.REMOVE_INITIALIZERS_SIDE_EFFECTS;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

class RemoveUnusedLocalVariabesASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put(REMOVE_INITIALIZERS_SIDE_EFFECTS, false);
		setVisitor(new RemoveUnusedLocalVariabesASTVisitor(options));
	}

	public static Stream<Arguments> notAllFragmentsUsed() throws Exception {
		return Stream.of(
				Arguments.of("x", "int x = 0;"),
				Arguments.of("y", "int y = 0;"),
				Arguments.of("z", "int z = 0;"),
				Arguments.of("x+y", "int x=0, y=0;"),
				Arguments.of("x+z", "int x=0, z=0;"),
				Arguments.of("y+z", "int y=0, z=0;"));
	}

	@ParameterizedTest
	@MethodSource("notAllFragmentsUsed")
	void visit_NotAllFragmentsUsed_shouldTransform(String expressionUsingVariables, String expectedDeclarationStatement)
			throws Exception {

		String original = String.format(
				"" +
						"		int x = 0, y = 0, z = 0;\n" +
						"		System.out.println(%s);",
				expressionUsingVariables);

		String expected = String.format(
				"" +
						"		%s\n" +
						"		System.out.println(%s);",
				expectedDeclarationStatement, expressionUsingVariables);

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"int x;",
			"int x, y;",
			"int x, y, z;",
	})
	void visit_AllFragmentsUnused_shouldTransform(String variableDeclarationStatement) throws Exception {

		String original = "{" + variableDeclarationStatement + "}";
		String expected = "{}";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"		int x = 0;\n" +
					"		++x;",
			"" +
					"		int x = 0;\n" +
					"		--x;"
	})
	void visit_VariableInPrefixExpressionstatement_shouldTransform(String codeToRemove) throws Exception {

		String original = "{" + codeToRemove + "}";
		String expected = "{}";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"		int x = 0;\n" +
					"		x++;",
			"" +
					"		int x = 0;\n" +
					"		x--;"
	})
	void visit_VariableInPostfixExpressionstatement_shouldTransform(String codeToRemove) throws Exception {

		String original = "{" + codeToRemove + "}";
		String expected = "{}";

		assertChange(original, expected);
	}

	@Test
	void visit_UnusedLocalVariableWithReAssignment_shouldTransform() throws Exception {
		String unusedVariableWithReassignment = "\n" +
				"		int x;\n" +
				"		x = 1;\n";

		String original = "{" + unusedVariableWithReassignment + "}";
		String expected = "{}";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"		x: while (true)\n" +
					"			break x;",
			"" +
					"		while (true) {\n" +
					"			x: while (true)\n" +
					"				continue x;\n" +
					"		}"
	})
	void visit_UnusedVariableXAndLabelX_shouldTransform(String statementWithLabelX) throws Exception {
		String original = "" +
				"		int x;\n" +
				"		x = 1;\n" +
				statementWithLabelX;

		String expected = statementWithLabelX;

		assertChange(original, expected);
	}

	@Test
	void visit_UnusedLocalVariabeXAndFieldX_shouldTransform() throws Exception {
		String original = "" +
				"		int x;\n" +
				"		class LocalClass {\n" +
				"			int x;\n" +
				"		}";

		String expected = "" +
				"		class LocalClass {\n" +
				"			int x;\n" +
				"		}";

		assertChange(original, expected);
	}

	@Test
	void visit_UnusedLocalVariabeXAndMethodX_shouldTransform() throws Exception {
		String original = "" +
				"		int x;\n" +
				"		class LocalClass {\n" +
				"			int x() {\n" +
				"				return 0;\n" +
				"			}\n" +
				"		}";

		String expected = "" +
				"		class LocalClass {\n" +
				"			int x() {\n" +
				"				return 0;\n" +
				"			}\n" +
				"		}";

		assertChange(original, expected);
	}

	@Test
	void visit_UnusedAndUsedLocalVariabeX_shouldTransform() throws Exception {
		String original = "" +
				"		int x;\n" +
				"		class LocalClass {\n" +
				"			int getInt() {\n" +
				"				int x = 0;\n" +
				"				return x;\n" +
				"			}\n" +
				"		}";

		String expected = "" +
				"		class LocalClass {\n" +
				"			int getInt() {\n" +
				"				int x = 0;\n" +
				"				return x;\n" +
				"			}\n" +
				"		}";

		assertChange(original, expected);
	}

	@Test
	void visit_ReAssignmentNotInBlock_shouldNotTransform() throws Exception {
		String original = "" +
				"		int x;\n" +
				"		if (true)\n" +
				"			x = 1;";

		assertNoChange(original);
	}

	@Test
	void visit_ReassignmentWithPossibleSideEffects_shouldNotTransform() throws Exception {
		String original = "" +
				"		class LocalClass {\n" +
				"			void reassignmentWithPossibleSideEffects() {\n" +
				"				int x;\n" +
				"				x = getValue();\n" +
				"			}\n" +
				"			int getValue() {\n" +
				"				return 0;\n" +
				"			}\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	void visit_RemoveInitializersSideEffectsOption_shouldTransform() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put(REMOVE_INITIALIZERS_SIDE_EFFECTS, true);
		setVisitor(new RemoveUnusedLocalVariabesASTVisitor(options));
		String original = "" +
				"		class LocalClass {\n" +
				"			void reassignmentWithPossibleSideEffects() {\n" +
				"				int x;\n" +
				"				x = getValue();\n" +
				"			}\n" +
				"			int getValue() {\n" +
				"				return 0;\n" +
				"			}\n" +
				"		}";

		String expected = "" +
				"		class LocalClass {\n" +
				"			void reassignmentWithPossibleSideEffects() {\n" +
				"			}\n" +
				"			int getValue() {\n" +
				"				return 0;\n" +
				"			}\n" +
				"		}";

		assertChange(original, expected);
	}

	/**
	 * Covers the case where a simple name is found which is not a label and has
	 * no valid binding. Note that the code in this test is invalid cannot be
	 * compiled!
	 */
	@Test
	void visit_UnresolvedBinding_shouldNotTransform() throws Exception {
		String original = "" +
				"		int x;\n" +
				"		x();";

		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"		Runnable r = () -> {\n" +
					"		};",
			"" +
					"		Runnable r = () -> {\n" +
					"			;\n" +
					"		};",
			"" +
					"		Runnable r = () -> {\n" +
					"			new Object().hashCode();\n" +
					"		};",
			"" +
					"		Runnable r = () -> {\n" +
					"			assert Integer.MAX_VALUE > 0;\n" +
					"		};",
			"" +
					"		Runnable r = () -> {\n" +
					"			int x = 0;\n" +
					"		};",
			"" +
					"		Supplier<String> supplier = () -> {\n" +
					"			return \"HelloWorld\";\n" +
					"		};",
			"" +
					"		Supplier<String> supplier = () -> \"HelloWorld\";",

	})
	void visit_SimpleLambdaExpression_shouldTransform(String declarationInitializedWithLambda) throws Exception {
		fixture.addImport(java.util.function.Supplier.class.getName());
		String original = "{" +
				declarationInitializedWithLambda +
				"}";
		String expected = "{}";
		assertChange(original, expected);

	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"		Supplier<String> supplier = () -> {\n" +
					"			String helloWorld =  \"HelloWorld\";\n" +
					"			return helloWorld;\n" +
					"		};",
			"" +
					"		Supplier<String> supplier = () -> {\n" +
					"			if(true) {\n" +
					"				return \"true\";\n" +
					"			}\n" +
					"			else {\n" +
					"				return \"false\";\n" +
					"			}\n" +
					"		};"
	})
	void visit_ComplexLambdaExpression_shouldNotTransform(String declarationInitializedWithLambda) throws Exception {
		fixture.addImport(java.util.function.Supplier.class.getName());
		assertNoChange(declarationInitializedWithLambda);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"		int[] array = new int[10];\n" +
					"		array[0] = 1;",
			"" +
					"		int[][] array = new int[10][10];\n" +
					"		array[0][0] = 1;",
			"" +
					"		int[] array = new int[10];\n" +
					"		++array[0];",
			"" +
					"		int[] array = new int[10];\n" +
					"		array[0]++;",
			"" +
					"		int[] array = new int[10];\n" +
					"		--array[0];",
			"" +
					"		int[] array = new int[10];\n" +
					"		array[0]--;",
	})
	void visit_OperationsOnArrayElements_shouldTransform(String declarationInitializedWithLambda) throws Exception {

		String original = "{" +
				declarationInitializedWithLambda +
				"}";
		String expected = "{}";
		assertChange(original, expected);
	}
}