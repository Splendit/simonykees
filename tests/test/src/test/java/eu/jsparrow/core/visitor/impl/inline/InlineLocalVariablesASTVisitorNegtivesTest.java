package eu.jsparrow.core.visitor.impl.inline;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
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

	/**
	 * Not transformed because {@link VariableDeclarationStatement} has more
	 * than one {@link VariableDeclarationFragment}.
	 * 
	 */
	@Test
	void visit_MultipleVariableDeclaration_shouldNotTransform() throws Exception {
		String original = "" +
				"	int multipleVariableDecaration(int a) {\n" +
				"		int square = a * a, cube = a * a * a;\n" +
				"		return square;\n" +
				"	}";
		assertNoChange(original);
	}

	/**
	 * Not transformed because boxing behavior would change by transformation.
	 */
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

	/**
	 * Not transformed because boxing behavior would change by transformation.
	 */
	@Test
	void visit_UnboxingInitializer_shouldNotTransform() throws Exception {
		String original = "" +
				"	int unboxingInitializer(Integer a) {\n" +
				"		int result = a;\n" +
				"		return result;\n" +
				"	}";
		assertNoChange(original);
	}

	/**
	 * Not transformed because the variable declaration preceding the
	 * {@link ReturnStatement} declares a variable which has not the same name
	 * as the name found in the {@link ReturnStatement}.
	 */
	@Test
	void visit_PreviousVariableDeclarationWithWrongName_shouldNotTransform() throws Exception {
		String original = "" +
				"	int usedMoreThanOnce() {\n" +
				"		int x = 1;\n" +
				"		int y = x;\n" +
				"		return x;\n" +
				"	}";
		assertNoChange(original);
	}

	/**
	 * Not transformed because the statement preceding the
	 * {@link ReturnStatement} is not a {@link VariableDeclarationStatement}.
	 */
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

	/**
	 * Not transformed because the statement preceding the
	 * {@link ReturnStatement} is not a {@link VariableDeclarationStatement}.
	 */
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

	/**
	 * Not transformed because the exception is initialized with null.
	 */
	@Test
	void visit_ExceptionInitializedWithNull_shouldNotTransform() throws Exception {
		String original = "" +
				"	void statementBetweenDeclarationAndThrow(String message) throws Exception {\n" +
				"		Exception exception = null;\n" +
				"		throw exception;\n" +
				"	}";
		assertNoChange(original);
	}

	/**
	 * Not transformed because the expression of the return statement is not a
	 * {@link SimpleName} but a {@link NumberLiteral}.
	 * 
	 */
	@Test
	void visit_ReturnStatementWithNumericExpression_shouldNotTransform() throws Exception {
		String original = "" +
				"		int returnNumberLiteral() {\n"
				+ "			return 2;\n"
				+ "		}";
		assertNoChange(original);
	}

	@Test
	void visit_ReturnStatementWithoutExpression_shouldNotTransform() throws Exception {
		String original = "" +
				"		void returnWithoutExpression() {\n" +
				"			return;\n" +
				"		}";
		assertNoChange(original);
	}

	/**
	 * The first {@link ReturnStatement} is not transformed because it is not
	 * child of a {@link Block}, the second {@link ReturnStatement} is not
	 * transformed because it has a literal as expression instead of a simple
	 * name.
	 * 
	 */
	@Test
	void visit_ConditionalReturnAfterDeclaration_shouldNotTransform() throws Exception {
		String original = "" +
				"		int conditionalReturnAfterDeclaration(int a) {\n"
				+ "			int square = a * a;\n"
				+ "			if ((a = a + 2) < 0)\n"
				+ "				return square;\n"
				+ "			\n"
				+ "			return 2;\n"
				+ "		}";
		assertNoChange(original);
	}

	/**
	 * Maybe redundant
	 */
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

	/**
	 * Not transformed because of annotation.
	 */
	@Test
	void visit_VariableDeclarationWithAnnotation_shouldNotTransform() throws Exception {
		String original = "" +
				"int localVariableWithAnnotation() {\n" +
				"	@Deprecated\n" +
				"	int x = 1;\n" +
				"	return x;\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	void visit_RawTypeInitializedWithParameterizedType_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.Map.class.getName());
		String original = "" +
				"	Map<Integer, Integer> cornerCaseWithRawType(Map<String, String> mapStringToString) {\n" +
				"		Map map = mapStringToString;\n" +
				"		return map;\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	void visit_DifferentTypeArguments_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.Map.class.getName());
		String original = "" +
				"	Map<?, ?> cornerCaseWithRawType6(Map<String, String> mapStringToString) {\n" +
				"		Map<?, ?> map = mapStringToString;\n" +
				"		return map;\n" +
				"	}";
		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"	Object getObjectReturningLambda() {\n" +
					"		Runnable r = () -> {\n" +
					"		};\n" +
					"		return r;\n" +
					"	}",
			"" +
					"	void exampleWithLambdaReturningLambda() {\n" +
					"		java.util.function.Supplier<Object> supplier = () -> {\n" +
					"			Runnable r = () -> {\n" +
					"			};\n" +
					"			return r;\n" +
					"		};\n" +
					"	}",
			"" +
					"	void exampleWithLambdaReturningLambda() {\n" +
					"		java.util.function.Function<Object, Object> function = o -> {\n" +
					"			Runnable r = () -> {\n" +
					"			};\n" +
					"			return r;\n" +
					"		};\n" +
					"	}",
	})
	void visit_LambdaExpressionAsInitializer_shouldNotTransform(String original) throws Exception {
		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"	Object getObject() {\n" +
					"		Runnable r = this::exampleMethod;\n" +
					"		return r;\n" +
					"	}",
			"" +
					"	void exampleWithLambdaReturningLambda() {\n" +
					"		java.util.function.Supplier<Object> supplier = () -> {\n" +
					"			Runnable r = this::exampleMethod;\n" +
					"			return r;\n" +
					"		};\n" +
					"	}",
			"" +
					"	void exampleWithLambdaReturningLambda() {\n" +
					"		java.util.function.Function<Object, Object> function = o -> {\n" +
					"			Runnable r = this::exampleMethod;\n" +
					"			return r;\n" +
					"		};\n" +
					"	}",
	})
	void visit_MethodReferenceAsInitializer_shouldNotTransform(String method) throws Exception {
		String original = method +
				"\n" +
				"	void exampleMethod() {\n" +
				"	}";
		assertNoChange(original);
	}
}
