package eu.jsparrow.core.visitor.lambda2methdref;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.LambdaToMethodReferenceASTVisitor;
import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

public class LambdaToMethodReferenceASTVisitorTest extends UsesJDTUnitFixture {

	private static final String CLASS_AMBIGUOUS_METHODS_DECLARATION = ""
			+ "	static class AmbiguousMethods {\n"
			+ "\n"
			+ "		public String testAmbiguity() {\n"
			+ "			return \"nonStaticMethod\";\n"
			+ "		}\n"
			+ "\n"
			+ "		public String testAmbiguity(int i) {\n"
			+ "			return \"nonStaticMethod\";\n"
			+ "		}\n"
			+ "\n"
			+ "		public String testAmbiguity(String s, int i) {\n"
			+ "			return \"nonStaticMethod\";\n"
			+ "		}\n"
			+ "\n"
			+ "		public static String testAmbiguity(AmbiguousMethods i) {\n"
			+ "			return String.valueOf(i);\n"
			+ "		}\n"
			+ "	}";

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new LambdaToMethodReferenceASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_AmbiguousIntegerToString_shouldNotTransform() throws Exception {

		defaultFixture.addImport(java.util.function.Function.class.getName());

		String original = ""
				+ "	Function<Integer, String> toString = (Integer i) -> i.toString();\n"
				+ "\n"
				+ CLASS_AMBIGUOUS_METHODS_DECLARATION;

		assertNoChange(original);
	}

	@Test
	void visit_AmbiguousStaticIntegerToString_shouldNotTransform() throws Exception {

		defaultFixture.addImport(java.util.function.Function.class.getName());

		String original = ""
				+ "	Function<Integer, String> toStringStatic = (Integer i) -> Integer.toString(i);\n"
				+ "\n"
				+ CLASS_AMBIGUOUS_METHODS_DECLARATION;

		assertNoChange(original);
	}

	@Test
	void visit_AmbiguousTestAmbiguity_shouldNotTransform() throws Exception {

		defaultFixture.addImport(java.util.function.Function.class.getName());

		String original = ""
				+ "	Function<AmbiguousMethods, String> testingAmb2 = (AmbiguousMethods i) -> i.testAmbiguity();\n"
				+ "\n"
				+ CLASS_AMBIGUOUS_METHODS_DECLARATION;

		assertNoChange(original);
	}

	@Test
	void visit_AmbiguousStaticTestAmbiguity_shouldNotTransform() throws Exception {

		defaultFixture.addImport(java.util.function.Function.class.getName());

		String original = ""
				+ "	Function<AmbiguousMethods, String> testingAmb = (AmbiguousMethods i) -> AmbiguousMethods.testAmbiguity(i);\n"
				+ "\n"
				+ CLASS_AMBIGUOUS_METHODS_DECLARATION;

		assertNoChange(original);
	}
	
	@Test
	void visit_FunctionOfDequeOfIntegerAsAssignmentLHS_shouldTransform() throws Exception {

		defaultFixture.addImport(java.util.Deque.class.getName());
		defaultFixture.addImport(java.util.function.Function.class.getName());

		String original = "" +
				"public void visit_FunctionOfDequeOfIntegerAsAssignmentLHS_shouldTransform() {\n" +
				"	Function<Deque<Integer>, Integer> dequeToInteger;\n" +
				"	dequeToInteger = (Deque<Integer> x1) -> x1.getFirst();\n" +
				"}";

		String expected = "" +
				"public void visit_FunctionOfDequeOfIntegerAsAssignmentLHS_shouldTransform() {\n" +
				"	Function<Deque<Integer>, Integer> dequeToInteger;\n" +
				"	dequeToInteger = Deque<Integer>::getFirst;\n" +
				"}";

		assertChange(original, expected);
	}
	
	@Test
	void visit_FunctionOfDequeOfIntegerInitialization_shouldTransform() throws Exception {

		defaultFixture.addImport(java.util.Deque.class.getName());
		defaultFixture.addImport(java.util.function.Function.class.getName());

		String original = "Function<Deque<Integer>, Integer> dequeToInteger = (Deque<Integer> x1) -> x1.getFirst();";
		String expected = "Function<Deque<Integer>, Integer> dequeToInteger = Deque<Integer>::getFirst;";

		assertChange(original, expected);
	}

	@Test
	void visit_FunctionOfJokerUsingDequeOfIntegerInitialization_shouldNotTransform() throws Exception {

		defaultFixture.addImport(java.util.Deque.class.getName());
		defaultFixture.addImport(java.util.function.Function.class.getName());

		String original = "Function<?, Integer> dequeToInteger = (Deque<Integer> x1) -> x1.getFirst();";

		assertNoChange(original);
	}

	@Test
	void visit_FunctionOfJokerUsingDequeOfIntegerAsAssignmentLHS_shouldNotTransform() throws Exception {

		defaultFixture.addImport(java.util.Deque.class.getName());
		defaultFixture.addImport(java.util.function.Function.class.getName());

		String original = "" +
				"public void visit_FunctionOfJokerUsingDequeOfIntegerAsAssignmentLHS_shouldNotTransform() {\n" +
				"	Function<?, Integer> dequeToInteger;\n" +
				"	dequeToInteger = (Deque<Integer> x1) -> x1.getFirst();\n" +
				"}";
		assertNoChange(original);
	}

}
