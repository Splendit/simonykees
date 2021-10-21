package eu.jsparrow.core.visitor.lambda2methdref;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.core.visitor.impl.LambdaToMethodReferenceASTVisitor;

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
	
	@Test
	void visit_toleratingNullMethodDeclarationBindings_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.List.class.getName());
		String original = ""
				+ "	public void usingAnOverloadWithCompilationErrors(CompilationErrorInMethodDeclarationClass cls) {\n"
				+ "		cls.run(() -> { cls.foo();});\n"
				+ "	}\n"
				+ ""
				+ "class CompilationErrorInMethodDeclarationClass<T extends org.apache.IDoNotExist> {\n"
				+ "	\n"
				+ "	public void run(Runnable r) {}\n"
				+ "	\n"
				+ "	public void foo() {}\n"
				+ "	\n"
				+ "	public List<Lisst<org.apache.IDoNotExist>> foo(int i) {\n"
				+ "		return;\n"
				+ "	}\n"
				+ "}";
		String expected = ""
				+ "	public void usingAnOverloadWithCompilationErrors(CompilationErrorInMethodDeclarationClass cls) {\n"
				+ "		cls.run(cls::foo);\n"
				+ "	}\n"
				+ ""
				+ "class CompilationErrorInMethodDeclarationClass<T extends org.apache.IDoNotExist> {\n"
				+ "	\n"
				+ "	public void run(Runnable r) {}\n"
				+ "	\n"
				+ "	public void foo() {}\n"
				+ "	\n"
				+ "	public List<Lisst<org.apache.IDoNotExist>> foo(int i) {\n"
				+ "		return;\n"
				+ "	}\n"
				+ "}";
		assertChange(original, expected);
	}
	
	@Test
	void visit_arrayInstanceCreation_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.function.Predicate.class.getName());

		String original = "" +
				"public void visit_arrayInstanceCreation_shouldTransform() {\n" +
				"	Predicate[] arr = new Predicate[] {s -> \"\".equals(s)};" +
				"}";
		assertNoChange(original);
	}

}
