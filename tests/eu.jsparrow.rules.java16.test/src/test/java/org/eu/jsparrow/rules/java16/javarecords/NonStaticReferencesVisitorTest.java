package org.eu.jsparrow.rules.java16.javarecords;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.rules.java16.javarecords.UseJavaRecordsASTVisitor;

@SuppressWarnings("nls")
public class NonStaticReferencesVisitorTest extends AbstractUseJavaRecordsTest {

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
	public void visit_ConstantOfTopLevelClass_shouldTransform() throws Exception {
		String original = "" +
				"	static final int CONSTANT_OF_TOP_LEVEL_CLASS = 1;\n"
				+ "\n"
				+ "	public void methodWithLocalClassPoint() {\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			public int getConstantOfTopLevelClass() {\n"
				+ "				return CONSTANT_OF_TOP_LEVEL_CLASS;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	static final int CONSTANT_OF_TOP_LEVEL_CLASS = 1;\n"
				+ "\n"
				+ "	public void methodWithLocalClassPoint() {\n"
				+ "		record Point(int x, int y) {\n"
				+ "			;\n"
				+ "			public int getConstantOfTopLevelClass() {\n"
				+ "				return CONSTANT_OF_TOP_LEVEL_CLASS;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_QualifiedConstantOfSurroundingStaticClass_shouldTransform() throws Exception {
		String original = "" +
				"	static class StaticSurroundingClass {\n"
				+ "		static final int CONSTANT_OF_SURROUNDING_CLASS = 1;\n"
				+ "\n"
				+ "		public void methodWithLocalClassPoint() {\n"
				+ "			class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "				public int getConstantOfSurroundingClass() {\n"
				+ "					return StaticSurroundingClass.CONSTANT_OF_SURROUNDING_CLASS;\n"
				+ "				}\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	static class StaticSurroundingClass {\n"
				+ "		static final int CONSTANT_OF_SURROUNDING_CLASS = 1;\n"
				+ "\n"
				+ "		public void methodWithLocalClassPoint() {\n"
				+ "			record Point(int x, int y) {\n"
				+ "				;\n"
				+ "				public int getConstantOfSurroundingClass() {\n"
				+ "					return StaticSurroundingClass.CONSTANT_OF_SURROUNDING_CLASS;\n"
				+ "				}\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_QualifiedConstantOfSurroundingNonStaticClass_shouldTransform() throws Exception {
		String original = "" +
				"	class NonStaticSurroundingClass {\n"
				+ "		static final int CONSTANT_OF_SURROUNDING_CLASS = 1;\n"
				+ "\n"
				+ "		public void methodWithLocalClassPoint() {\n"
				+ "			class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "				public int getConstantOfSurroundingClass() {\n"
				+ "					return NonStaticSurroundingClass.CONSTANT_OF_SURROUNDING_CLASS;\n"
				+ "				}\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	class NonStaticSurroundingClass {\n"
				+ "		static final int CONSTANT_OF_SURROUNDING_CLASS = 1;\n"
				+ "\n"
				+ "		public void methodWithLocalClassPoint() {\n"
				+ "			record Point(int x, int y) {\n"
				+ "				;\n"
				+ "				public int getConstantOfSurroundingClass() {\n"
				+ "					return NonStaticSurroundingClass.CONSTANT_OF_SURROUNDING_CLASS;\n"
				+ "				}\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_IntegerMaxValueFullyQualifiedName_shouldTransform() throws Exception {
		String original = ""
				+ "	public void methodWithLocalClassPoint() {\n"
				+ "\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			int getIntegerMaxValue() {\n"
				+ "				return java.lang.Integer.MAX_VALUE;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";
		String expected = ""
				+ "	public void methodWithLocalClassPoint() {\n"
				+ "\n"
				+ "		record Point(int x, int y) {\n"
				+ "			;\n"
				+ "			int getIntegerMaxValue() {\n"
				+ "				return java.lang.Integer.MAX_VALUE;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";
		assertChange(original, expected);
	}

	@Test
	public void visit_QualifiedThisOfSameClass_shouldTransform() throws Exception {
		String original = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			public Point getQualifiedThisOfPoint() {\n"
				+ "				return Point.this;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		record Point(int x, int y) {\n"
				+ "			;\n"
				+ "			public Point getQualifiedThisOfPoint() {\n"
				+ "				return Point.this;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_InvokeStaticMethodOfTopLevelClass_shouldTransform() throws Exception {
		String original = "" +
				"	static int staticMethodOfTopLevelClass() {\n"
				+ "		return  1;\n"
				+ "	}\n"
				+ "\n"
				+ "	public void methodWithLocalClassPoint() {\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			public int callStaticMethodOfTopLevelClass() {\n"
				+ "				return staticMethodOfTopLevelClass();\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	static int staticMethodOfTopLevelClass() {\n"
				+ "		return  1;\n"
				+ "	}\n"
				+ "\n"
				+ "	public void methodWithLocalClassPoint() {\n"
				+ "		record Point(int x, int y) {\n"
				+ "			;\n"
				+ "			public int callStaticMethodOfTopLevelClass() {\n"
				+ "				return staticMethodOfTopLevelClass();\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_CallToString_shouldTransform() throws Exception {
		String original = ""
				+ "	public void methodWithLocalClassPoint() {\n"
				+ "\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			String callToString() {\n"
				+ "				return toString();\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";
		String expected = ""
				+ "	public void methodWithLocalClassPoint() {\n"
				+ "\n"
				+ "		record Point(int x, int y) {\n"
				+ "			;\n"
				+ "			String callToString() {\n"
				+ "				return toString();\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";
		assertChange(original, expected);
	}

	@Test
	public void visit_InstanceCreationOfStaticClass_shouldTransform() throws Exception {

		String original = "" +
				"	static class StaticClass {\n"
				+ "\n"
				+ "	}\n"
				+ "	\n"
				+ "	public void methodWithLocalClassPoint() {\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			public StaticClass createStaticClassInstance() {\n"
				+ "				return new StaticClass();\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	static class StaticClass {\n"
				+ "\n"
				+ "	}\n"
				+ "	\n"
				+ "	public void methodWithLocalClassPoint() {\n"
				+ "		record Point(int x, int y) {\n"
				+ "			;\n"
				+ "			public StaticClass createStaticClassInstance() {\n"
				+ "				return new StaticClass();\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_CreateNewObject_shouldTransform() throws Exception {
		String original = ""
				+ "	public void methodWithLocalClassPoint() {\n"
				+ "\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			Object  createNewObject() {\n"
				+ "				return new Object();\n"
				+ "			}			\n"
				+ "		}\n"
				+ "	}";
		String expected = ""
				+ "	public void methodWithLocalClassPoint() {\n"
				+ "\n"
				+ "		record Point(int x, int y) {\n"
				+ "			;\n"
				+ "			Object  createNewObject() {\n"
				+ "				return new Object();\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";
		assertChange(original, expected);
	}

	@Test
	public void visit_LabeledStatement_shouldTransform() throws Exception {
		String original = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			public void methodWithLabeledStatement() {\n"
				+ "				int x = 1;\n"
				+ "				label: x = 2;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		record Point(int x, int y) {\n"
				+ "			;\n"
				+ "			public void methodWithLabeledStatement() {\n"
				+ "				int x = 1;\n"
				+ "				label: x = 2;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_ContinueWithLabel_shouldTransform() throws Exception {
		String original = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			public void methodWithContinueWithLabel() {\n"
				+ "				loop: while (true) {\n"
				+ "					continue loop;\n"
				+ "				}\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		record Point(int x, int y) {\n"
				+ "			;\n"
				+ "			public void methodWithContinueWithLabel() {\n"
				+ "				loop: while (true) {\n"
				+ "					continue loop;\n"
				+ "				}\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_BreakWithLabel_shouldTransform() throws Exception {
		String original = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			public void methodWithLabeledStatement() {\n"
				+ "				loop: while (true) {\n"
				+ "					break loop;\n"
				+ "				}\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		record Point(int x, int y) {\n"
				+ "			;\n"
				+ "			public void methodWithLabeledStatement() {\n"
				+ "				loop: while (true) {\n"
				+ "					break loop;\n"
				+ "				}\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_SimpleNameOfInstanceFieldOfSurroundingClass_shouldNotTransform() throws Exception {
		String original = "" +
				"	int instanceFieldOfTopLevelClass = 1;\n"
				+ "\n"
				+ "	public void methodWithLocalClassPoint() {\n"
				+ "		\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			public int getInstanceFieldOfTopLevelClass() {\n"
				+ "				return instanceFieldOfTopLevelClass;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_QualifiedNameOfInstanceField_shouldNotTransform() throws Exception {
		String original = "" +
				"	IntWrapper intWrapper = new IntWrapper();\n"
				+ "\n"
				+ "	public void methodWithLocalClassPoint() {\n"
				+ "\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			public int getIntWrapperIntValue() {\n"
				+ "				return intWrapper.intValue;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	static class IntWrapper {\n"
				+ "		int intValue;\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_SumOfInstanceFields_shouldNotTransform() throws Exception {
		String original = "" +
				"	int instanceFieldOfTopLevelClass = 1;\n"
				+ "\n"
				+ "	public void methodWithLocalClassPoint() {\n"
				+ "		\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			public int getInstanceFieldOfSurroundingClass() {\n"
				+ "				return instanceFieldOfTopLevelClass  + instanceFieldOfTopLevelClass;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_LocalVariableOfSurroundingMethod_shouldNotTransform() throws Exception {
		String original = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		int localVariableFromSurroundingMethod = 1;\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			public int getLocalVariableFromSurroundingMethod() {\n"
				+ "				return localVariableFromSurroundingMethod;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_InstanceFieldOfLocalClass_shouldNotTransform() throws Exception {
		String original = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		class LocalWrapper {\n"
				+ "			int x;\n"
				+ "		}\n"
				+ "		LocalWrapper localWrapper  = new LocalWrapper();\n"
				+ "		\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			int getXOfVarV() {\n"
				+ "				return localWrapper.x;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_LocalVariableOfTypeVarFieldX_shouldNotTransform() throws Exception {
		String original = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		var v = new Object() {\n"
				+ "			int x;\n"
				+ "		};\n"
				+ "\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			int getXOfVarV() {\n"
				+ "				return v.x;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_CallInstanceMethodOfSurroundingClass_shouldNotTransform() throws Exception {
		String original = "" +
				"	int instanceMethodOfSurroundingClass() {\n"
				+ "		return 1;\n"
				+ "	}\n"
				+ "\n"
				+ "	public void methodWithLocalClassPoint() {\n"
				+ "		\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			public int callInstanceMethodOfSurroundingClass() {\n"
				+ "				return instanceMethodOfSurroundingClass();\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_SuperHashCode_shouldNotTransform() throws Exception {
		String original = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			public int getSuperHashCode() {\n"
				+ "				return super.hashCode();\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_ThisExpressionOfSurroundingClass_shouldNotTransform() throws Exception {
		String original = "" +
				"	static class SurroundingClass {\n"
				+ "		public void methodWithLocalClassPoint() {\n"
				+ "\n"
				+ "			class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "				SurroundingClass getThisInstanceOfSurroundingClass() {\n"
				+ "					return SurroundingClass.this;\n"
				+ "				}\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_InstanceFiledOfSuperClassOfSurroundingClass_shouldNotTransform() throws Exception {
		String original = "" +
				"	static class SuperClassOfSurroundingClass {\n"
				+ "		int instanceField;\n"
				+ "	}\n"
				+ "\n"
				+ "	static class SurroundingClass extends SuperClassOfSurroundingClass {\n"
				+ "\n"
				+ "		private class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			public int getQualifiedThisOfPoint() {\n"
				+ "				return instanceField;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_InstanceCreationOfNonStaticClass_shouldNotTransform() throws Exception {
		String original = "" +
				"	class NonStaticClass {\n"
				+ "		\n"
				+ "	}\n"
				+ "\n"
				+ "	public void methodWithLocalClassPoint() {\n"
				+ "		\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			public NonStaticClass createNonStaticClassInstance() {\n"
				+ "				return new NonStaticClass();\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_CallUndefinedMethodInvocation_shouldNotTransform() throws Exception {
		String original = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "\n"
				+ "			void callUndefinedMethod() {\n"
				+ "				unndefinedMethod();\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}
}
