package org.eu.jsparrow.rules.java16.javarecords;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.rules.java16.javarecords.UseJavaRecordsASTVisitor;

@SuppressWarnings("nls")
public class UseJavaRecordsClassAnalysisASTVisitorTest extends AbstractUseJavaRecordsTest {

	private static final String BODY_DECLARATIONS = ""
			+ "			private final int x;\n"
			+ "			private final int y;\n"
			+ "\n"
			+ "			Point(int x, int y) {\n"
			+ "				this.x = x;\n"
			+ "				this.y = y;\n"
			+ "			}\n"
			+ "\n"
			+ "			public int x() {\n"
			+ "				return x;\n"
			+ "			}\n"
			+ "\n"
			+ "			public int y() {\n"
			+ "				return y;\n"
			+ "			}\n";

	@BeforeEach
	public void setUp() {
		setDefaultVisitor(new UseJavaRecordsASTVisitor());
		fixtureProject.setJavaVersion(JavaCore.VERSION_16);
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_FinalLocalClassPoint_shouldTransform() throws Exception {
		String original = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		final class Point {\n"
				+ BODY_DECLARATIONS
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	public void methodWithLocalClassPoint() {\n" +
				"		record Point(int x, int y) {\n" +
				"		}\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_LocalClassPoint_shouldTransform() throws Exception {
		String original = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	public void methodWithLocalClassPoint() {\n" +
				"		record Point(int x, int y) {\n" +
				"		}\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_PrivateStaticFinalNestedClassPoint_shouldTransform() throws Exception {
		String original = "" +
				"	private static final class Point {\n"
				+ BODY_DECLARATIONS
				+ "	}";

		String expected = "" +
				"	record Point(int x, int y) {\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_PrivateStaticNestedClassPoint_shouldTransform() throws Exception {
		String original = "" +
				"	private static class Point {\n"
				+ BODY_DECLARATIONS
				+ "	}";

		String expected = "" +
				"	record Point(int x, int y) {\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_StaticFinalNestedClassPoint_shouldTransform() throws Exception {
		String original = "" +
				"	static final class Point {\n"
				+ BODY_DECLARATIONS
				+ "	}";

		String expected = "" +
				"	record Point(int x, int y) {\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_InterfacePoint_shouldNotransform() throws Exception {
		String original = "" +
				"	interface IPoint {\n"
				+ "		int x();\n"
				+ "		int y();\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_AbstractStaticNestedClassPoint_shouldNotTransform() throws Exception {
		String original = "" +
				"	static abstract class Point {\n"
				+ BODY_DECLARATIONS
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_PointExtendsSuperClass_shouldNotTransform() throws Exception {
		String original = "" +
				"	private static final class Point extends SuperClass {\n"
				+ BODY_DECLARATIONS
				+ "	}\n"
				+ "	\n"
				+ "	private static class SuperClass {\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_PrivateStaticNestedClassNotEffectivelyFinal_shouldNotTransform() throws Exception {
		String original = "" +
				"	private static class Point {\n"
				+ BODY_DECLARATIONS
				+ "	}\n"
				+ "\n"
				+ "	private static class Point00 extends Point {\n"
				+ "		Point00(int x, int y) {\n"
				+ "			super(0, 0);\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_LocalClassNotEffectivelyFinal_shouldNotTransform() throws Exception {
		String original = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "		}\n"
				+ "		\n"
				+ "		class Point00 extends Point {\n"
				+ "			Point00(int x, int y) {\n"
				+ "				super(0, 0);\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_PrivateFinalNonStaticNestedClassPoint_shouldNotTransform() throws Exception {
		String original = "" +
				"	private final class Point {\n"
				+ BODY_DECLARATIONS
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_StaticNotPrivateNotFinalNestedClassPoint_shouldNotTransform() throws Exception {
		String original = "" +
				"	static class Point {\n"
				+ BODY_DECLARATIONS
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_StaticClassInLocalClass_shouldTransform() throws Exception {
		String original = "" +
				"	void methodWithLocalClass() {\n"
				+ "		class LocalClassSurroundingStaticClass {\n"
				+ "			static class Point {\n"
				+ BODY_DECLARATIONS
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	void methodWithLocalClass() {\n"
				+ "		class LocalClassSurroundingStaticClass {\n"
				+ "			record Point (int x, int y) {\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_StaticFinalClassInLocalClass_shouldTransform() throws Exception {
		String original = "" +
				"	void methodWithLocalClass() {\n"
				+ "		class LocalClassSurroundingStaticClass {\n"
				+ "			static final class Point {\n"
				+ BODY_DECLARATIONS
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	void methodWithLocalClass() {\n"
				+ "		class LocalClassSurroundingStaticClass {\n"
				+ "			record Point (int x, int y) {\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_StaticClassInAnonymousClass_shouldTransform() throws Exception {
		String original = "" +
				"	Runnable runnable = new Runnable() {\n"
				+ "\n"
				+ "		static class Point {\n"
				+ BODY_DECLARATIONS
				+ "		}\n"
				+ "\n"
				+ "		@Override\n"
				+ "		public void run() {\n"
				+ "		}\n"
				+ "	};";

		String expected = "" +
				"	Runnable runnable = new Runnable() {\n"
				+ "\n"
				+ "		record Point  (int x, int y){\n"
				+ "		}\n"
				+ "\n"
				+ "		@Override\n"
				+ "		public void run() {\n"
				+ "		}\n"
				+ "	};";

		assertChange(original, expected);
	}

	@Test
	public void visit_StaticFinalClassInAnonymousClass_shouldTransform() throws Exception {
		String original = "" +
				"	Runnable runnable = new Runnable() {\n"
				+ "\n"
				+ "		static final class Point {\n"
				+ BODY_DECLARATIONS
				+ "		}\n"
				+ "\n"
				+ "		@Override\n"
				+ "		public void run() {\n"
				+ "		}\n"
				+ "	};";

		String expected = "" +
				"	Runnable runnable = new Runnable() {\n"
				+ "\n"
				+ "		record Point  (int x, int y){\n"
				+ "		}\n"
				+ "\n"
				+ "		@Override\n"
				+ "		public void run() {\n"
				+ "		}\n"
				+ "	};";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"	public void methodWithLocalClass () {\n" +
					"		int localVariableFromSurroundingMethod = 1;\n" +
					"		class Point {\n" +
					"%s" +
					"			public int getLocalVariableFromSurroundingMethod() {\n" +
					"				return localVariableFromSurroundingMethod;\n" +
					"			}\n" +
					"		}\n" +
					"	}",
			"" +
					"	int instanceFieldOfSurroundingClass = 1;\n" +
					"\n" +
					"	public void methodWithLocalClass () {\n" +
					"		class Point {\n" +
					"%s" +
					"			public int getInstanceFieldOfSurroundingClass() {\n" +
					"				return instanceFieldOfSurroundingClass;\n" +
					"			}\n" +
					"		}\n" +
					"	}",
			"" +
					"	class ExampleClass {\n"
					+ "		int x = 1;\n"
					+ "	}\n"
					+ "	ExampleClass exampleClass = new ExampleClass();\n"
					+ "\n"
					+ "	public void methodWithLocalClassPoint() {\n"
					+ "		class Point {\n"
					+ "%s"
					+ "			int getFieldOfInstanceFieldOfExampleClass() {\n"
					+ "				return exampleClass.x;\n"
					+ "			}\n"
					+ "		}\n"
					+ "	}",
			"" +
					"	class SurroundingClass {\n"
					+ "		public void methodWithLocalClassPoint() {\n"
					+ "			class Point {\n"
					+ "%s"
					+ "				int getSuperHashCodeOfSurroundingClass() {\n"
					+ "					return SurroundingClass.super.hashCode();\n"
					+ "				}\n"
					+ "			}\n"
					+ "		}\n"
					+ "	}",
			"" +
					"	class SurroundingClass {\n"
					+ "		public void methodWithLocalClassPoint() {\n"
					+ "			class Point {\n"
					+ "%s"
					+ "				SurroundingClass getThisInstanceOfSurroundingClass() {\n"
					+ "					return SurroundingClass.this;\n"
					+ "				}\n"
					+ "			}\n"
					+ "		}\n"
					+ "	}",
			"" +
					"	void instanceMethod() {\n"
					+ "		\n"
					+ "	}\n"
					+ "\n"
					+ "	public void methodWithLocalClassPoint() {\n"
					+ "		class Point {\n"
					+ "%s"
					+ "			void invokeInstanceMethodOfSurroundingClass() {\n"
					+ "				instanceMethod();\n"
					+ "			}\n"
					+ "		}\n"
					+ "	}",
			"" +
					"	void instanceMethod() {\n"
					+ "		\n"
					+ "	}\n"
					+ "\n"
					+ "	public void methodWithLocalClassPoint() {\n"
					+ "		class Point {\n"
					+ "%s"
					+ "			void invokeInstanceMethodOfSurroundingClass() {\n"
					+ "				instanceMethod();\n"
					+ "				instanceMethod();\n"					
					+ "			}\n"
					+ "		}\n"
					+ "	}",
			"" +
					"	public void methodWithLocalClassPoint() {\n"
					+ "		class Point {\n"
					+ "%s"
					+ "			void invokeInstanceMethodOfSurroundingClass() {\n"
					+ "				undefinedMethod();\n"
					+ "			}\n"
					+ "		}\n"
					+ "	}"
	})
	public void visit_UnsupportedReference_shouldNotTransform(String originalFormatstring)
			throws Exception {

		String original = String.format(originalFormatstring, BODY_DECLARATIONS);

		assertNoChange(original);
	}

	@Test
	public void visit_AccessConstantOfSurroundingClass_shouldTransform() throws Exception {
		String original = "" +
				"	static final int CONSTANT_OF_SURROUNDING_CLASS = 1;\n"
				+ "\n"
				+ "	public void methodWithLocalClassPoint() {\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "			public int getConstantOfSurroundingClass() {\n"
				+ "				return CONSTANT_OF_SURROUNDING_CLASS;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	static final int CONSTANT_OF_SURROUNDING_CLASS = 1;\n"
				+ "\n"
				+ "	public void methodWithLocalClassPoint() {\n"
				+ "		record Point(int x, int y) {\n"
				+ "			;\n"
				+ "			public int getConstantOfSurroundingClass() {\n"
				+ "				return CONSTANT_OF_SURROUNDING_CLASS;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_IntegerMaxValue_shouldTransform() throws Exception {
		String original = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "			public int getIntegerMaxValue() {\n"
				+ "				return Integer.MAX_VALUE;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		record Point(int x, int y) {\n"
				+ "			;\n"
				+ "			public int getIntegerMaxValue() {\n"
				+ "				return Integer.MAX_VALUE;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AccessSuperHashCode_shouldTransform() throws Exception {
		String original = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "			public int getSuperHashCode() {\n"
				+ "				return super.hashCode();\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		record Point(int x, int y) {\n"
				+ "			;\n"
				+ "			public int getSuperHashCode() {\n"
				+ "				return super.hashCode();\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

}
