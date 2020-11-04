package eu.jsparrow.core.visitor.impl.comparatormethods;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

public class UseComparatorMethodsASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new UseComparatorMethodsASTVisitor());
		defaultFixture.addImport(java.util.Comparator.class.getName());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_Comparator4InnerClass_shouldTransform() throws Exception {
		String original = "" +
				"class InnerClass {\n" +
				"	String getString(){\n" +
				"		return \"\";\n" +
				"	}\n" +
				"}\n" +
				"void test() {\n" +
				"	Comparator<InnerClass> comparator = (lhs, rhs) -> lhs.getString().compareTo(rhs.getString());\n" +
				"}";

		String expected = "" +
				"class InnerClass {\n" +
				"	String getString(){\n" +
				"		return \"\";\n" +
				"	}\n" +
				"}\n" +
				"void test() {\n" +
				"	Comparator<InnerClass> comparator=Comparator.comparing(fixturepackage.TestCU.InnerClass::getString);\n"
				+
				"}";

		assertChange(original, expected);
	}
	

	@Test
	public void visit_InitializingComparatorOfDequeOfInteger_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.Deque.class.getName());
		String original = "" +
				"void test() {\n" +
				"	Comparator<Deque<Integer>> comparator = (Deque<Integer> x1, Deque<Integer> x2) -> x1\n"
				+
				"			.getFirst().compareTo(x2.getFirst());\n" +
				"}";

		String expected = "" +
				"void test() {\n" +
				"	Comparator<Deque<Integer>> comparator=Comparator.comparingInt(Deque::getFirst);\n"
				+
				"}";

		assertChange(original, expected);
	}


	@Test
	public void visit_IntegerLambdaParameterInInitializer_shouldTransform() throws Exception {
		String original = "" +
				"	void test () {\n"
				+ "		Comparator<Integer> integerComparator = (Integer u1, Integer u2) -> u1.compareTo(u2);\n"
				+ "	}";
		String expected = "" +
				"	void test () {\n"
				+ "		Comparator<Integer> integerComparator = Comparator.naturalOrder();\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_IntegerLambdaParameterInAssignmentRHS_shouldTransform() throws Exception {
		String original = "" +
				"	void test () {\n"
				+ "		Comparator<Integer> integerComparator;\n"
				+ "		integerComparator = (Integer u1, Integer u2) -> u1.compareTo(u2);\n"
				+ "	}";
		String expected = "" +
				"	void test () {\n"
				+ "		Comparator<Integer> integerComparator;\n"
				+ "		integerComparator = Comparator.naturalOrder();\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_UseComparatorWithDequeOfIntegerAsArgument_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.Deque.class.getName());
		String original = "" +
				"void useComparator(Comparator<Deque<Integer>> comparator) {\n" +
				"}\n" +
				"void test() {\n" +
				"	useComparator(\n" +
				"			(Deque<Integer> lhs, Deque<Integer> rhs) -> lhs.getFirst().compareTo(rhs.getFirst()));\n"
				+
				"}";
		String expected = "" +
				"void useComparator(Comparator<Deque<Integer>> comparator) {\n" +
				"}\n" +
				"void test() {\n" +
				"	useComparator(\n" +
				"			Comparator.comparingInt(Deque::getFirst));\n" +
				"}";
		assertChange(original, expected);
	}

	@Test
	public void visit_UseComparatorOfJokerWithDequeOfIntegerAsArgument_shouldTransform() throws Exception {

		defaultFixture.addImport(java.util.Deque.class.getName());
		String original = "" +
				"		void useComparator(Comparator<?> comparator) {\n" +
				"		}\n" +
				"		void test() {\n" +
				"			useComparator(					\n" +
				"					(Deque<Integer> lhs, Deque<Integer> rhs) -> lhs.getFirst().compareTo(rhs.getFirst()));\n"
				+
				"		}";
		String expected = "" +
				"		void useComparator(Comparator<?> comparator) {\n" +
				"		}\n" +
				"		void test() {\n" +
				"			useComparator(					\n" +
				"					Comparator.comparingInt((Deque<Integer> lhs) -> lhs.getFirst()));\n"
				+
				"		}";

		assertChange(original, expected);
	}

	@Test
	public void visit_ComparatorOfJokerAsTypeArgument_shouldTransform() throws Exception {
		String original = "" +
				"	<T> void useT(T t) {\n" +
				"	}\n" +
				"	void test() {\n" +
				"		this.<Comparator<?>>useT((Integer t1, Integer t2) -> t1.compareTo(t2));\n" +
				"	}";

		String expected = "" +
				"	<T> void useT(T t) {\n" +
				"	}\n" +
				"	void test() {\n" +
				"		this.<Comparator<?>>useT(Comparator.<Integer>naturalOrder());\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_TypeCastToComparatorOfIntegerUsedAsComparatorOfJoker_shouldTransform() throws Exception {
		String original = "" +
				"	void useComparatorOfJoker(Comparator<?> comparator) {\n"
				+ "	}	\n"
				+ "	void test() {\n"
				+ "		useComparatorOfJoker((Comparator<Integer>) (t1, t2) -> t1.compareTo(t2));\n"
				+ "	}";

		String expected = "" +
				"	void useComparatorOfJoker(Comparator<?> comparator) {\n"
				+ "	}	\n"
				+ "	void test() {\n"
				+ "		useComparatorOfJoker((Comparator<Integer>) Comparator.<Integer>naturalOrder());\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_TypeCastToComparatorOfDequeUsedAsComparatorOfJoker_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.Deque.class.getName());
		String original = "" +
				"	void useComparatorOfJoker(Comparator<?> comparator) {\n"
				+ "	}\n"
				+ "	void test() {\n"
				+ "		useComparatorOfJoker((Comparator<Deque<Integer>>) (x1, x2) -> x1.getFirst()\n"
				+ "			.compareTo(x2.getFirst()));\n"
				+ "	}";

		String expected = "" +
				"	void useComparatorOfJoker(Comparator<?> comparator) {\n"
				+ "	}\n"
				+ "	void test() {\n"
				+ "		useComparatorOfJoker(//\n"
				+ "				(Comparator<Deque<Integer>>) Comparator.comparingInt((Deque<Integer> x1) -> x1.getFirst()));\n"
				+ "	}";

		assertChange(original, expected);

	}

	@Test
	public void visit_Comparator4IntegerUsingOnlyLHSLambdaParameter_shouldNotTransform() throws Exception {
		String original = "" +
				"void test() {\n" +
				"	Comparator<Integer> comparator = (lhs, rhs) -> lhs.compareTo(lhs);\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	public void visit_Comparator4IntegerUsingOnlyRHSLambdaParameter_shouldNotTransform() throws Exception {
		String original = "" +
				"void test() {\n" +
				"	Comparator<Integer> comparator = (lhs, rhs) -> rhs.compareTo(rhs);\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	public void visit_ComparatorForIntegerNotUsingLambdaParameters_shouldNotTransform() throws Exception {
		String original = "" +
				"void test(Integer x1, Integer x2) {\n" +
				"	Comparator<Integer> comparator = (lhs, rhs) -> x1.compareTo(x2);\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	public void visit_DequeComparatorUsingDifferentGetters_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.Deque.class.getName());
		String original = "" +
				"void test() {\n" +
				"	Comparator<Deque<Integer>> comparator = (lhs, rhs) -> lhs.getFirst().compareTo(rhs.getLast());\n"
				+
				"}";
		assertNoChange(original);
	}

	@Test
	public void visit_DequeComparatorUsingOnlyLHSLambdaParameter_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.Deque.class.getName());
		String original = "" +
				"void test() {\n" +
				"	Comparator<Deque<Integer>> comparator = (lhs, rhs) -> lhs.getFirst().compareTo(lhs.getFirst());\n"
				+
				"}";
		assertNoChange(original);
	}

	@Test
	public void visit_DequeComparatorUsingOnlyRHSLambdaParameter_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.Deque.class.getName());
		String original = "" +
				"void test() {\n" +
				"	Comparator<Deque<Integer>> comparator = (lhs, rhs) -> rhs.getFirst().compareTo(rhs.getFirst());\n"
				+
				"}";
		assertNoChange(original);
	}

	@Test
	public void visit_Comparator4ArrayListByFirstItem_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.ArrayList.class.getName());
		String original = "" +
				"void test() {\n" +
				"	Comparator<ArrayList<Integer>> comparator = (lhs, rhs) -> lhs.get(0).compareTo(rhs.get(0));\n"
				+
				"}";

		assertNoChange(original);
	}

	@Test
	public void visit_SimpleNameCompareToMethodInvocation_shouldNotTransform() throws Exception {
		String original = "" +
				"Integer useInteger(Integer integer) {\n" +
				"	return integer;\n" +
				"}\n" +
				"void test() {\n" +
				"	Comparator<Integer> comparator = (lhs, rhs) -> lhs.compareTo(useInteger(rhs));\n" +
				"}";

		assertNoChange(original);
	}

	@Test
	public void visit_MethodInvocationCompareToVariable_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.Deque.class.getName());
		String original = "" +
				"void test() {\n" +
				"	Integer integerVariable = 1;\n" +
				"	Comparator<Deque<Integer>> comparator = (lhs, rhs) -> lhs.getFirst().compareTo(integerVariable);\n"
				+
				"}";

		assertNoChange(original);
	}

	@Test
	public void visit_LHSMethodInvocationOnMethodInvocation_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.Deque.class.getName());
		String original = "" +
				"<T> T useObject(T object) {\n" +
				"	return object;\n" +
				"}\n" +
				"void test() {\n" +
				"	Comparator<Deque<Integer>> comparator = (lhs, rhs) -> useObject(lhs).getFirst().compareTo(rhs.getFirst());\n"
				+
				"}";

		assertNoChange(original);
	}

	@Test
	public void visit_RHSMethodInvocationOnMethodInvocation_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.Deque.class.getName());
		String original = "" +
				"<T> T useObject(T object) {\n" +
				"	return object;\n" +
				"}\n" +
				"void test() {\n" +
				"	Comparator<Deque<Integer>> comparator = (lhs, rhs) -> lhs.getFirst().compareTo(useObject(rhs).getFirst());\n"
				+
				"}";

		assertNoChange(original);
	}

	@Test
	public void visit_LHSMethodInvocationWithoutExpression_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.Deque.class.getName());
		String original = "" +
				"Integer getFirst(Deque<Integer> deque) {\n" +
				"	return deque.getFirst();\n" +
				"}\n" +
				"void test() {\n" +
				"	Comparator<Deque<Integer>> comparator = (lhs, rhs) -> getFirst(lhs).compareTo(rhs.getFirst());\n"
				+
				"}";

		assertNoChange(original);
	}

	@Test
	public void visit_RHSMethodInvocationWithoutExpression_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.Deque.class.getName());
		String original = "" +
				"Integer getFirst(Deque<Integer> deque) {\n" +
				"	return deque.getFirst();\n" +
				"}\n" +
				"void test() {\n" +
				"	Comparator<Deque<Integer>> comparator = (lhs, rhs) -> lhs.getFirst().compareTo(getFirst(rhs));\n"
				+
				"}";

		assertNoChange(original);
	}
}
