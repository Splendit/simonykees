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
	public void visit_Comparator4IntegerReversed_shouldTransform() throws Exception {
		String original = "" +
				"void test() {\n" +
				"	Comparator<Integer> comparator = (lhs, rhs) -> rhs.compareTo(lhs);\n" +
				"}";
		String expected = "" +
				"void test() {\n" +
				"	Comparator<Integer> comparator=Comparator.reverseOrder();\n" +
				"}";
		assertChange(original, expected);
	}

	@Test
	public void visit_Comparator4ArrayDequeOfInteger_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.ArrayDeque.class.getName());
		String original = "" +
				"void test() {\n" +
				"	Comparator<ArrayDeque<Integer>> comparator = (lhs, rhs) -> lhs.getFirst().compareTo(rhs.getFirst());\n"
				+
				"}";

		String expected = "" +
				"void test() {\n" +
				"	Comparator<ArrayDeque<Integer>> comparator=Comparator.comparingInt(ArrayDeque::getFirst);\n"
				+
				"}";

		assertChange(original, expected);
	}

	@Test
	public void visit_Comparator4ArrayDequeOfLong_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.ArrayDeque.class.getName());
		String original = "" +
				"void test() {\n" +
				"	Comparator<ArrayDeque<Long>> comparator = (lhs, rhs) -> lhs.getFirst().compareTo(rhs.getFirst());\n"
				+
				"}";

		String expected = "" +
				"void test() {\n" +
				"	Comparator<ArrayDeque<Long>> comparator=Comparator.comparingLong(ArrayDeque::getFirst);\n"
				+
				"}";

		assertChange(original, expected);
	}

	@Test
	public void visit_Comparator4ArrayDequeOfDouble_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.ArrayDeque.class.getName());
		String original = "" +
				"void test() {\n" +
				"	Comparator<ArrayDeque<Double>> comparator = (lhs, rhs) -> lhs.getFirst().compareTo(rhs.getFirst());\n"
				+
				"}";

		String expected = "" +
				"void test() {\n" +
				"	Comparator<ArrayDeque<Double>> comparator=Comparator.comparingDouble(ArrayDeque::getFirst);\n"
				+
				"}";

		assertChange(original, expected);
	}

	@Test
	public void visit_Comparator4ArrayDequeOfString_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.ArrayDeque.class.getName());
		String original = "" +
				"void test() {\n" +
				"	Comparator<ArrayDeque<String>> comparator = (lhs, rhs) -> lhs.getFirst().compareTo(rhs.getFirst());\n"
				+
				"}";

		String expected = "" +
				"void test() {\n" +
				"	Comparator<ArrayDeque<String>> comparator=Comparator.comparing(ArrayDeque::getFirst);\n"
				+
				"}";

		assertChange(original, expected);
	}

	@Test
	public void visit_Comparator4ArrayDequeOfStringReversed_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.ArrayDeque.class.getName());
		String original = "" +
				"void test() {\n" +
				"	Comparator<ArrayDeque<String>> comparator = (lhs, rhs) -> rhs.getFirst().compareTo(lhs.getFirst());\n"
				+
				"}";

		String expected = "" +
				"void test() {\n" +
				"	Comparator<ArrayDeque<String>> comparator=Comparator.comparing(ArrayDeque::getFirst).reversed();\n"
				+
				"}";

		assertChange(original, expected);
	}

	@Test
	public void visit_Comparator4LocalClass_shouldTransform() throws Exception {
		String original = "" +
				"void test() {\n" +
				"	class LocalClass {\n" +
				"		String getString(){\n" +
				"			return \"\";\n" +
				"		}\n" +
				"	}\n" +
				"	Comparator<LocalClass> comparator = (lhs, rhs) -> lhs.getString().compareTo(rhs.getString());\n" +
				"}";

		String expected = "" +
				"void test() {\n" +
				"	class LocalClass {\n" +
				"		String getString(){\n" +
				"			return \"\";\n" +
				"		}\n" +
				"	}\n" +
				"	Comparator<LocalClass> comparator=Comparator.comparing(LocalClass::getString);\n" +
				"}";

		assertChange(original, expected);
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
	public void visit_ComparatorForComparableOfJoker_shouldTransform() throws Exception {
		String original = "" +
				"void test() {\n" +
				"	Comparator<Comparable<? super Comparable<?>>> comparable = (u1, u2) -> u1.compareTo(u2);\n"
				+
				"}";

		String expected = "" +
				"void test() {\n" +
				"	Comparator<Comparable<? super Comparable<?>>> comparable=Comparator.naturalOrder();\n"
				+
				"}";

		assertChange(original, expected);
	}

	@Test
	public void visit_InitializingComparatorOfJoker_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.ArrayDeque.class.getName());
		String original = "" +
				"void test() {\n" +
				"	Comparator<?> comparator = (ArrayDeque<Integer> x1, ArrayDeque<Integer> x2) -> x1\n"
				+
				"			.getFirst().compareTo(x2.getFirst());\n" +
				"}";

		String expected = "" +
				"void test() {\n" +
				"	Comparator<?> comparator=Comparator.comparingInt((ArrayDeque<Integer> x1) -> x1.getFirst());\n"
				+
				"}";

		assertChange(original, expected);
	}

	@Test
	public void visit_InitializingComparatorOfDequeOfInteger_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.ArrayDeque.class.getName());
		String original = "" +
				"void test() {\n" +
				"	Comparator<ArrayDeque<Integer>> comparator = (ArrayDeque<Integer> x1, ArrayDeque<Integer> x2) -> x1\n"
				+
				"			.getFirst().compareTo(x2.getFirst());\n" +
				"}";

		String expected = "" +
				"void test() {\n" +
				"	Comparator<ArrayDeque<Integer>> comparator=Comparator.comparingInt(ArrayDeque::getFirst);\n"
				+
				"}";

		assertChange(original, expected);
	}

	@Test
	public void visit_LambdaParameterExplicitlyInteger_shouldTransform() throws Exception {
		String original = "" +
				"	void test() {\n"
				+ "		Comparator<?> comparator1 = (Integer u1, Integer u2) -> u1.compareTo(u2);\n"
				+ "	}";
		String expected = "" +
				"	void test() {\n"
				+ "		Comparator<?> comparator1=Comparator.<Integer>naturalOrder();\n"
				+ "	}";

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
		defaultFixture.addImport(java.util.ArrayDeque.class.getName());
		String original = "" +
				"void useComparator(Comparator<ArrayDeque<Integer>> comparator) {\n" +
				"}\n" +
				"void test() {\n" +
				"	useComparator(\n" +
				"			(ArrayDeque<Integer> lhs, ArrayDeque<Integer> rhs) -> lhs.getFirst().compareTo(rhs.getFirst()));\n"
				+
				"}";
		String expected = "" +
				"void useComparator(Comparator<ArrayDeque<Integer>> comparator) {\n" +
				"}\n" +
				"void test() {\n" +
				"	useComparator(\n" +
				"			Comparator.comparingInt(ArrayDeque::getFirst));\n" +
				"}";
		assertChange(original, expected);
	}

	@Test
	public void visit_UseComparatorOfJokerWithDequeOfIntegerAsArgument_shouldTransform() throws Exception {

		defaultFixture.addImport(java.util.ArrayDeque.class.getName());
		String original = "" +
				"		void useComparator(Comparator<?> comparator) {\n" +
				"		}\n" +
				"		void test() {\n" +
				"			useComparator(					\n" +
				"					(ArrayDeque<Integer> lhs, ArrayDeque<Integer> rhs) -> lhs.getFirst().compareTo(rhs.getFirst()));\n"
				+
				"		}";
		String expected = "" +
				"		void useComparator(Comparator<?> comparator) {\n" +
				"		}\n" +
				"		void test() {\n" +
				"			useComparator(					\n" +
				"					Comparator.comparingInt((ArrayDeque<Integer> lhs) -> lhs.getFirst()));\n"
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
		defaultFixture.addImport(java.util.ArrayDeque.class.getName());
		String original = "" +
				"	void useComparatorOfJoker(Comparator<?> comparator) {\n"
				+ "	}\n"
				+ "	void test() {\n"
				+ "		useComparatorOfJoker((Comparator<ArrayDeque<Integer>>) (x1, x2) -> x1.getFirst()\n"
				+ "			.compareTo(x2.getFirst()));\n"
				+ "	}";

		String expected = "" +
				"	void useComparatorOfJoker(Comparator<?> comparator) {\n"
				+ "	}\n"
				+ "	void test() {\n"
				+ "		useComparatorOfJoker(//\n"
				+ "				(Comparator<ArrayDeque<Integer>>) Comparator.comparingInt((ArrayDeque<Integer> x1) -> x1.getFirst()));\n"
				+ "	}";

		assertChange(original, expected);

	}

	@Test
	public void visit_OverloadedCompareToReceivingNotComparable_shouldNotTransform() throws Exception {
		String original = "" +
				"void test() {\n" +
				"	class LocalClass {}\n" +
				"	class LocalComparableSubclass implements Comparable<LocalComparableSubclass> {\n" +
				"		int compareTo(LocalClass o) {\n" +
				"			return 0;\n" +
				"		}\n" +
				"		public int compareTo(LocalComparableSubclass o) {\n" +
				"			return 0;\n" +
				"		}\n" +
				"	}\n" +
				"	Comparator<LocalComparableSubclass> comparator = (lhs, rhs) -> lhs.compareTo(new LocalClass());\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	public void visit_OverloadedCompareToReceivingTwoParameters_shouldNotTransform() throws Exception {
		String original = "" +
				"void test2() {\n" +
				"	class LocalComparableSubclass implements Comparable<LocalComparableSubclass> {\n" +
				"		int compareTo(LocalComparableSubclass o1, LocalComparableSubclass o2) {\n" +
				"			return 0;\n" +
				"		}\n" +
				"		public int compareTo(LocalComparableSubclass o) {\n" +
				"			return 0;\n" +
				"		}\n" +
				"	}\n" +
				"	Comparator<LocalComparableSubclass> comparator = (lhs, rhs) -> lhs.compareTo(lhs, rhs);\n" +
				"}";
		assertNoChange(original);
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
	public void visit_ComparatorNotUsingCompareToMethod_shouldNotTransform() throws Exception {
		String original = "" +
				"void test() {\n" +
				"	class LocalClass {\n" +
				"		int extractInt(LocalClass other) {\n" +
				"			return 0;\n" +
				"		}\n" +
				"	}\n" +
				"	Comparator<LocalClass> comparator = (lhs, rhs) -> lhs.extractInt(rhs);\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	public void visit_CompareToNotMethodOfComparable_shouldNotTransform() throws Exception {
		String original = "" +
				"void test() {\n" +
				"	class LocalClass {\n" +
				"		int compareTo(LocalClass other) {\n" +
				"			return 0;\n" +
				"		}\n" +
				"	}\n" +
				"	Comparator<LocalClass> comparator = (lhs, rhs) -> lhs.compareTo(rhs);\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	public void visit_ArrayDequeComparatorUsingDifferentGetters_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.ArrayDeque.class.getName());
		String original = "" +
				"void test() {\n" +
				"	Comparator<ArrayDeque<Integer>> comparator = (lhs, rhs) -> lhs.getFirst().compareTo(rhs.getLast());\n"
				+
				"}";
		assertNoChange(original);
	}

	@Test
	public void visit_DequeComparatorUsingOnlyLHSLambdaParameter_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.ArrayDeque.class.getName());
		String original = "" +
				"void test() {\n" +
				"	Comparator<ArrayDeque<Integer>> comparator = (lhs, rhs) -> lhs.getFirst().compareTo(lhs.getFirst());\n"
				+
				"}";
		assertNoChange(original);
	}

	@Test
	public void visit_DequeComparatorUsingOnlyRHSLambdaParameter_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.ArrayDeque.class.getName());
		String original = "" +
				"void test() {\n" +
				"	Comparator<ArrayDeque<Integer>> comparator = (lhs, rhs) -> rhs.getFirst().compareTo(rhs.getFirst());\n"
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
		defaultFixture.addImport(java.util.ArrayDeque.class.getName());
		String original = "" +
				"void test() {\n" +
				"	Integer integerVariable = 1;\n" +
				"	Comparator<ArrayDeque<Integer>> comparator = (lhs, rhs) -> lhs.getFirst().compareTo(integerVariable);\n"
				+
				"}";

		assertNoChange(original);
	}

	@Test
	public void visit_LHSMethodInvocationOnMethodInvocation_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.ArrayDeque.class.getName());
		String original = "" +
				"<T> T useObject(T object) {\n" +
				"	return object;\n" +
				"}\n" +
				"void test() {\n" +
				"	Comparator<ArrayDeque<Integer>> comparator = (lhs, rhs) -> useObject(lhs).getFirst().compareTo(rhs.getFirst());\n"
				+
				"}";

		assertNoChange(original);
	}

	@Test
	public void visit_RHSMethodInvocationOnMethodInvocation_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.ArrayDeque.class.getName());
		String original = "" +
				"<T> T useObject(T object) {\n" +
				"	return object;\n" +
				"}\n" +
				"void test() {\n" +
				"	Comparator<ArrayDeque<Integer>> comparator = (lhs, rhs) -> lhs.getFirst().compareTo(useObject(rhs).getFirst());\n"
				+
				"}";

		assertNoChange(original);
	}

	@Test
	public void visit_LHSMethodInvocationWithoutExpression_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.ArrayDeque.class.getName());
		String original = "" +
				"Integer getFirst(ArrayDeque<Integer> deque) {\n" +
				"	return deque.getFirst();\n" +
				"}\n" +
				"void test() {\n" +
				"	Comparator<ArrayDeque<Integer>> comparator = (lhs, rhs) -> getFirst(lhs).compareTo(rhs.getFirst());\n"
				+
				"}";

		assertNoChange(original);
	}

	@Test
	public void visit_RHSMethodInvocationWithoutExpression_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.ArrayDeque.class.getName());
		String original = "" +
				"Integer getFirst(ArrayDeque<Integer> deque) {\n" +
				"	return deque.getFirst();\n" +
				"}\n" +
				"void test() {\n" +
				"	Comparator<ArrayDeque<Integer>> comparator = (lhs, rhs) -> lhs.getFirst().compareTo(getFirst(rhs));\n"
				+
				"}";

		assertNoChange(original);
	}

	@Test
	public void visit_LambdaBodyNotMethodInvocation_shouldNotTransform() throws Exception {
		String original = "" +
				"void test() {\n" +
				"	Comparator<Integer> comparator = (lhs, rhs) -> {\n" +
				"		return lhs.compareTo(rhs);\n" +
				"	};\n" +
				"}";

		assertNoChange(original);
	}

	@Test
	public void visit_CompareToMethodWithoutExpression_shouldNotTransform() throws Exception {
		String original = "" +
				"void test() {\n" +
				"	class LocalComparable implements Comparable<LocalComparable> {\n" +
				"		Comparator<LocalComparable> comparator = (lhs, rhs) -> compareTo(rhs);\n" +
				"		@Override\n" +
				"		public int compareTo(LocalComparable o) {\n" +
				"			return 0;\n" +
				"		}\n" +
				"	}\n" +
				"}";

		assertNoChange(original);
	}

}
