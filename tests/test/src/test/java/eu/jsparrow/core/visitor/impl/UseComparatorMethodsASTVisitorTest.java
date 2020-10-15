package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
	public void visit_Comparator4Integer_shouldTransform() throws Exception {
		String original = "" +
				"void test() {\n" +
				"	Comparator<Integer> comparator = (lhs, rhs) -> lhs.compareTo(rhs);\n" +
				"}";
		String expected = "" +
				"void test() {\n" +
				"	Comparator<Integer> comparator=Comparator.naturalOrder();\n" +
				"}";
		assertChange(original, expected);
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
				"	Comparator<InnerClass> comparator = (lhs, rhs) -> lhs.getString().compareTo(rhs.getString());\n"
				+
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
	public void visit_Comparator4LocalClassWithoutCompareTo_shouldNotTransform() throws Exception {
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
	public void visit_Comparator4LocalClassUsingCompareTo_shouldNotTransform() throws Exception {
		String original = "" +
				"	class Comparator4LocalClassUsingCompareTo {\n" +
				"		void test() {\n" +
				"			class LocalClass {\n" +
				"				int compareTo(LocalClass other) {\n" +
				"					return 0;\n" +
				"				}\n" +
				"			}\n" +
				"			Comparator<LocalClass> comparator = (lhs, rhs) -> lhs.compareTo(rhs);\n" +
				"		}\n" +
				"	}";
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
	public void visit_LambdaTypeBindingNotComparator_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.function.BiFunction.class.getName());
		String original = "" +
				"void test() {\n" +
				"	BiFunction<Integer, Integer, Integer> bifunction = (lhs, rhs) -> lhs.compareTo(rhs);\n"
				+
				"}";

		assertNoChange(original);
	}

	/**
	 * This test will fail as soon as the transformation with jokers is
	 * supported. TODO for Implementation of isComparable: find out whether
	 * typeBinding.isCapture() is true, and if this is the case, then get the
	 * bounds and determine whether comparable is within the bounds.
	 * 
	 * 
	 * @throws Exception
	 */
	@Test
	public void visit_ComparatorForComparableOfJoker_shouldButDoesNotTransform() throws Exception {
		String original = "" +
				"void test() {\n" +
				"	Comparator<Comparable<? super Comparable<?>>> comparableComparatorReversed = (u1, u2) -> u2.compareTo(u1);\n"
				+
				"}";

		assertNoChange(original);
	}

}
