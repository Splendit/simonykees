package eu.jsparrow.core.visitor.impl.comparatormethods;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

public class UseComparatorMethodsASTVisitorComplexCasesTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new UseComparatorMethodsASTVisitor());
		defaultFixture.addImport(java.util.Comparator.class.getName());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	/**
	 * Fails because of invalid transformation
	 */
	@Test
	public void visit_ComparatorForTExtendsSupplierOfInteger_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.function.Supplier.class.getName());
		String original = "" +
				"	class TestClassWithTypeParameterSupplyingComparable<T extends Supplier<Integer>> {\n"
				+ "		Comparator<T> comparatorOfT;\n"
				+ "		void test() {\n"
				+ "			comparatorOfT = (x1, x2) -> x1.get()\n"
				+ "				.compareTo(x2.get());\n"
				+ "		}\n"
				+ "	}";
		String expected = "" +
				"	class TestClassWithTypeParameterSupplyingComparable<T extends Supplier<Integer>> {\n"
				+ "		Comparator<T> comparatorOfT;\n"
				+ "		void test() {\n"
				+ "			comparatorOfT = Comparator.comparingInt(T::get);\n"
				+ "		}\n"
				+ "	}";
		assertChange(original, expected);
	}

	/**
	 * Fails because of invalid transformation
	 */
	@Test
	public void visit_ReversedComparatorForTExtendsSupplierOfInteger_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.function.Supplier.class.getName());
		String original = "" +
				"	class TestClassWithTypeParameterSupplyingComparable<T extends Supplier<Integer>> {\n"
				+ "		Comparator<T> comparatorOfT;\n"
				+ "		void test() {\n"
				+ "			comparatorOfT = (x1, x2) -> x2.get()\n"
				+ "				.compareTo(x1.get());\n"
				+ "		}\n"
				+ "	}\n"
				+ "";
		String expected = "" +
				"	class TestClassWithTypeParameterSupplyingComparable<T extends Supplier<Integer>> {\n"
				+ "		Comparator<T> comparatorOfT;\n"
				+ "		void test() {\n"
				+ "			comparatorOfT = Comparator.comparingInt((T x1) -> x1.get())\n"
				+ "				.reversed();\n"
				+ "		}\n"
				+ "	}";
		assertChange(original, expected);
	}

	/**
	 * Fails because of invalid transformation
	 */
	@Test
	public void visit_ComparatorForTExtendsSupplierOfIntegerExplicitLambdaParameterType_shouldTransform()
			throws Exception {
		defaultFixture.addImport(java.util.function.Supplier.class.getName());
		String original = "" +
				"	class TestClassWithTypeParameterSupplyingComparable<T extends Supplier<Integer>> {\n"
				+ "		Comparator<T> comparatorOfT;\n"
				+ "		void test() {\n"
				+ "			comparatorOfT = (T x1, T x2) -> x1.get()\n"
				+ "				.compareTo(x2.get());\n"
				+ "		}\n"
				+ "	}";
		String expected = "" +
				"	class TestClassWithTypeParameterSupplyingComparable<T extends Supplier<Integer>> {\n"
				+ "		Comparator<T> comparatorOfT;\n"
				+ "		void test() {\n"
				+ "			comparatorOfT = Comparator.comparingInt(T::get);\n"
				+ "		}\n"
				+ "	}";
		assertChange(original, expected);
	}

	/**
	 * Fails because of invalid transformation
	 */
	@Test
	public void visit_ReversedComparatorForTExtendsSupplierOfIntegerExplicitLambdaParameterType_shouldTransform()
			throws Exception {
		defaultFixture.addImport(java.util.function.Supplier.class.getName());
		String original = "" +
				"	class TestClassWithTypeParameterSupplyingComparable<T extends Supplier<Integer>> {\n"
				+ "		Comparator<T> comparatorOfT;\n"
				+ "		void test() {\n"
				+ "			comparatorOfT = (T x1, T x2) -> x2.get()\n"
				+ "				.compareTo(x1.get());\n"
				+ "		}\n"
				+ "	}";
		String expected = "" +
				"	class TestClassWithTypeParameterSupplyingComparable<T extends Supplier<Integer>> {\n"
				+ "		Comparator<T> comparatorOfT;\n"
				+ "		void test() {\n"
				+ "			comparatorOfT = Comparator.comparingInt((T x1) -> x1.get())\n"
				+ "				.reversed();\n"
				+ "		}\n"
				+ "	}\n"
				+ "";
		assertChange(original, expected);
	}

	@Test
	public void visit_SortArrayListOfTExtendsComparable_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.ArrayList.class.getName());
		String original = "" +
				"<T extends Comparable<T>> void testWithArrayListOfT() {\n"
				+ "		ArrayList<T> arrayList = new ArrayList<>();\n"
				+ "		arrayList.sort((T x1, T x2) -> x1.compareTo(x2));\n"
				+ "	}";
		String expected = "" +
				"	<T extends Comparable<T>> void testWithArrayListOfT() {\n"
				+ "		ArrayList<T> arrayList = new ArrayList<>();\n"
				+ "		arrayList.sort(Comparator.<T>naturalOrder());\n"
				+ "}";
		assertChange(original, expected);
	}

	@Test
	public void visit_SortArrayListOfTExtendsSupplierOfInteger_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.ArrayList.class.getName());
		defaultFixture.addImport(java.util.function.Supplier.class.getName());
		String original = "" +
				"	<T extends Supplier<Integer>> void testWithArrayListOfTSupplyingInteger() {\n"
				+ "		ArrayList<T> arrayList = new ArrayList<>();\n"
				+ "		arrayList.sort((T x1, T x2) -> x1.get().compareTo(x2.get()));\n"
				+ "	}\n"
				+ "";
		String expected = "" +
				"	<T extends Supplier<Integer>> void testWithArrayListOfTSupplyingInteger() {\n"
				+ "		ArrayList<T> arrayList = new ArrayList<>();\n"
				+ "		arrayList.sort(Comparator.comparingInt((T x1) -> x1.get()));\n"
				+ "	}\n"
				+ "";
		assertChange(original, expected);
	}

	@Test
	public void visit_SortArrayListOfInteger_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.ArrayList.class.getName());
		String original = "" +
				"void testWithArrayListOfInteger() {\n"
				+ "		ArrayList<Integer> arrayList = new ArrayList<>();\n"
				+ "		arrayList.sort((Integer x1, Integer x2) -> x1.compareTo(x2));\n"
				+ "}";
		String expected = "" +
				"void testWithArrayListOfInteger() {\n"
				+ "		ArrayList<Integer> arrayList = new ArrayList<>();\n"
				+ "		arrayList.sort(Comparator.<Integer>naturalOrder());\n"
				+ "}";
		assertChange(original, expected);
	}
}
