package eu.jsparrow.core.visitor.impl.comparatormethods;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

public class UseComparatorMethodsASTVisitorSimpleTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setVisitor(new UseComparatorMethodsASTVisitor());
		fixture.addImport(java.util.Comparator.class.getName());
	}

	@Test
	public void visit_ComparatorOfInteger_shouldTransform() throws Exception {
		String original = "Comparator<Integer> comparator = (lhs, rhs) -> lhs.compareTo(rhs);";
		String expected = "Comparator<Integer> comparator = Comparator.naturalOrder();";

		assertChange(original, expected);
	}

	@Test
	public void visit_ComparatorOfIntegerReversed_shouldTransform() throws Exception {
		String original = "Comparator<Integer> comparator = (lhs, rhs) -> rhs.compareTo(lhs);";
		String expected = "Comparator<Integer> comparator=Comparator.reverseOrder();";
		assertChange(original, expected);
	}

	@Test
	public void visit_ComparatorOfDequeOfInteger_shouldTransform() throws Exception {
		fixture.addImport(java.util.Deque.class.getName());
		String original = "Comparator<Deque<Integer>> comparator = (lhs, rhs) -> lhs.getFirst().compareTo(rhs.getFirst());";
		String expected = "Comparator<Deque<Integer>> comparator=Comparator.comparingInt(Deque::getFirst);";
		assertChange(original, expected);
	}

	@Test
	public void visit_ComparatorOfDequeOfLong_shouldTransform() throws Exception {
		fixture.addImport(java.util.Deque.class.getName());
		String original = "Comparator<Deque<Long>> comparator = (lhs, rhs) -> lhs.getFirst().compareTo(rhs.getFirst());";
		String expected = "Comparator<Deque<Long>> comparator=Comparator.comparingLong(Deque::getFirst);";
		assertChange(original, expected);
	}

	@Test
	public void visit_ComparatorOfDequeOfDouble_shouldTransform() throws Exception {
		fixture.addImport(java.util.Deque.class.getName());
		String original = "Comparator<Deque<Double>> comparator = (lhs, rhs) -> lhs.getFirst().compareTo(rhs.getFirst());";
		String expected = "Comparator<Deque<Double>> comparator=Comparator.comparingDouble(Deque::getFirst);";
		assertChange(original, expected);
	}

	@Test
	public void visit_ComparatorOfDequeOfString_shouldTransform() throws Exception {
		fixture.addImport(java.util.Deque.class.getName());
		String original = "Comparator<Deque<String>> comparator = (lhs, rhs) -> lhs.getFirst().compareTo(rhs.getFirst());";
		String expected = "Comparator<Deque<String>> comparator=Comparator.comparing(Deque::getFirst);";
		assertChange(original, expected);
	}

	@Test
	public void visit_InitializeComparatorRawType_shouldTransform() throws Exception {
		fixture.addImport(java.lang.Comparable.class.getName());
		String original = "Comparator comparator = (lhs, rhs) -> lhs.toString().compareTo(rhs.toString());";
		String expected = "Comparator comparator = Comparator.comparing(Object::toString);";
		assertChange(original, expected);
	}

	@Test
	public void visit_TypeCastToComparatorRawType_shouldTransform() throws Exception {
		fixture.addImport(java.lang.Comparable.class.getName());
		String original = "Comparator comparator =  (Comparator) (lhs, rhs) -> lhs.toString().compareTo(rhs.toString());";
		String expected = "Comparator comparator=(Comparator)Comparator.comparing((  Object lhs) -> lhs.toString());";
		assertChange(original, expected);
	}

	@Test
	public void visit_ComparatorOfDequeOfStringReversed_shouldTransform() throws Exception {
		fixture.addImport(java.util.Deque.class.getName());
		String original = "Comparator<Deque<String>> comparator = (lhs, rhs) -> rhs.getFirst().compareTo(lhs.getFirst());";
		String expected = "Comparator<Deque<String>> comparator=Comparator.comparing(Deque::getFirst).reversed();";
		assertChange(original, expected);
	}

	@Test
	public void visit_ComparatorForLocalClass_shouldTransform() throws Exception {
		String original = "" +
				"class LocalClass {\n" +
				"	String getString(){\n" +
				"		return \"\";\n" +
				"	}\n" +
				"}\n" +
				"Comparator<LocalClass> comparator = (lhs, rhs) -> lhs.getString().compareTo(rhs.getString());";
		String expected = "" +
				"class LocalClass {\n" +
				"	String getString(){\n" +
				"		return \"\";\n" +
				"	}\n" +
				"}\n" +
				"Comparator<LocalClass> comparator=Comparator.comparing(LocalClass::getString);\n";
		assertChange(original, expected);
	}

	@Test
	public void visit_ComparatorOfJokerInitializedWithComparatorOfInteger_shouldTransform() throws Exception {
		String original = "Comparator<?> comparator1 = (Integer u1, Integer u2) -> u1.compareTo(u2);";
		String expected = "Comparator<?> comparator1 = Comparator.<Integer>naturalOrder();";
		assertChange(original, expected);
	}

	@Test
	public void visit_ComparatorOfJokerInitializedWithDequeOfInteger_shouldTransform() throws Exception {
		fixture.addImport(java.util.Deque.class.getName());
		String original = "Comparator<?> comparator = (Deque<Integer> x1, Deque<Integer> x2) -> x1.getFirst().compareTo(x2.getFirst());";
		String expected = "Comparator<?> comparator = Comparator.comparingInt((Deque<Integer> x1) -> x1.getFirst());";
		assertChange(original, expected);
	}

	@Test
	public void visit_TypeCastOfParenthesizedLambda_shouldTransform() throws Exception {
		String original = "Comparator<Integer> comparator = (Comparator<Integer>)((((lhs, rhs) -> lhs.compareTo(rhs))));";
		String expected = "Comparator<Integer> comparator=(Comparator<Integer>)(((Comparator.<Integer>naturalOrder())));";
		assertChange(original, expected);
	}

	@Test
	public void visit_CompareToParameterIsCapture_shouldTransform() throws Exception {
		String original = "" +
				"Comparator<Comparable<? super Comparable<?>>> comparator = (u1, u2) -> u1.compareTo(u2);";

		String expected = "" +
				"Comparator<Comparable<? super Comparable<?>>> comparator = Comparator.naturalOrder();";

		assertChange(original, expected);
	}

	@Test
	public void visit_LambdaWithExplicitParameterTypeToMethodReference_shouldTransform() throws Exception {
		fixture.addImport(java.util.Deque.class.getName());
		String original = "Comparator<Deque<Integer>> comparator = (Deque<Integer> x1, Deque<Integer> x2) -> x1.getFirst().compareTo(x2.getFirst());";
		String expected = "Comparator<Deque<Integer>> comparator = Comparator.comparingInt(Deque::getFirst);";
		assertChange(original, expected);
	}

	@Test
	public void visit_ExplicitLambdaParameterTypeNoNeedForTypeArgument_shouldTransform() throws Exception {
		String original = "Comparator<Integer> integerComparator = (Integer u1, Integer u2) -> u1.compareTo(u2);";
		String expected = "Comparator<Integer> integerComparator = Comparator.naturalOrder();";
		assertChange(original, expected);
	}

	@Test
	public void visit_IntegerLambdaParameterInAssignmentRHS_shouldTransform() throws Exception {
		String original = "" +
				"Comparator<Integer> integerComparator;\n" +
				"integerComparator = (Integer u1, Integer u2) -> u1.compareTo(u2);";
		String expected = "" +
				"Comparator<Integer> integerComparator;\n" +
				"integerComparator = Comparator.naturalOrder();\n";
		assertChange(original, expected);
	}
}
