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
}
