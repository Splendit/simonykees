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
	public void visit_LambdaExpressionComparatorOfInteger_shouldTransform() throws Exception {
		String original = "Comparator<Integer> comparator = (lhs, rhs) -> lhs.compareTo(rhs);";
		String expected = "Comparator<Integer> comparator = Comparator.naturalOrder();";

		assertChange(original, expected);
	}

	@Test
	public void visit_LambdaExpressionNotComparator_shouldNotTransform() throws Exception {
		fixture.addImport(java.util.function.BiFunction.class.getName());
		assertNoChange("BiFunction<Integer, Integer, Integer> bifunction = (lhs, rhs) -> lhs.compareTo(rhs);");
	}

	@Test
	public void visit_LambdaBodyNotMethodInvocation_shouldNotTransform() throws Exception {
		String original = "" +
				"Comparator<Integer> comparator = (lhs, rhs) -> {\n" +
				"	return lhs.compareTo(rhs);\n" +
				"};";

		assertNoChange(original);
	}

	@Test
	public void visit_LambdaHasNotCompareToMethod_shouldNotTransform() throws Exception {
		String original = "" +
				"class LocalClass {\n" +
				"	int extractInt(LocalClass other) {\n" +
				"		return 0;\n" +
				"	}\n" +
				"}\n" +
				"Comparator<LocalClass> comparator = (lhs, rhs) -> lhs.extractInt(rhs);";

		assertNoChange(original);
	}

	@Test
	public void visit_CompareToMethodWithoutExpression_shouldNotTransform() throws Exception {
		String original = "" +
				"class LocalComparable implements Comparable<LocalComparable> {\n" +
				"	Comparator<LocalComparable> comparator = (lhs, rhs) -> compareTo(rhs);\n" +
				"	@Override\n" +
				"	public int compareTo(LocalComparable o) {\n" +
				"		return 0;\n" +
				"	}\n" +
				"}";

		assertNoChange(original);
	}

	@Test
	public void visit_CompareToArgumentNotComparable_shouldNotTransform() throws Exception {
		String original = "" +
				"class ComparableSubclass implements Comparable<ComparableSubclass> {\n" +
				"	int compareTo(Object[] objects) {\n" +
				"		return 0;\n" +
				"	}\n" +
				"	public int compareTo(ComparableSubclass o) {\n" +
				"		return 0;\n" +
				"	}\n" +
				"}\n" +
				"Comparator<ComparableSubclass> comparator = (lhs, rhs) -> lhs.compareTo(new Object[] {});";

		assertNoChange(original);
	}

	@Test
	public void visit_CompareToWithoutParameter_shouldNotTransform() throws Exception {
		String original = "" +
				"class LocalComparableSubclass implements Comparable<LocalComparableSubclass> {\n" +
				"	int compareTo() {\n" +
				"		return 0;\n" +
				"	}\n" +
				"	public int compareTo(LocalComparableSubclass o) {\n" +
				"		return 0;\n" +
				"	}\n" +
				"}\n" +
				"Comparator<LocalComparableSubclass> comparator = (lhs, rhs) -> lhs.compareTo();";

		assertNoChange(original);
	}

	@Test
	public void visit_CompareToNotMethodOfComparable_shouldNotTransform() throws Exception {
		String original = "" +
				"	class NotComparable {\n" +
				"		int compareTo(NotComparable other) {\n" +
				"			return 0;\n" +
				"		}\n" +
				"	}\n" +
				"	Comparator<NotComparable> comparator = (lhs, rhs) -> lhs.compareTo(rhs);";
		assertNoChange(original);
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
	public void visit_LambdaParameterHasWildCardType_shouldNotTransform() throws Exception {
		String original = "" +
				"		class LocalClass {\n"
				+ "			<T extends Comparable<T>> void useComparatorOfTExtendingComparable(Comparator<T> comparator) {\n"
				+ "				// intended (java:S1186)\n"
				+ "			}\n"
				+ "		}\n"
				+ "		new LocalClass().useComparatorOfTExtendingComparable((t1, t2) -> t1.compareTo(t2));";
		assertNoChange(original);
	}
}
