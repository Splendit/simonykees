package org.eu.jsparrow.rules.java16.patternmatching;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.java16.patternmatching.UsePatternMatchingForInstanceofASTVisitor;

@SuppressWarnings("nls")
public class UsePatternMatchingForInstanceOfASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setJavaVersion(JavaCore.VERSION_16);
		setDefaultVisitor(new UsePatternMatchingForInstanceofASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_SimpleNameAsInstanceOfLeftOperand_shouldTransform() throws Exception {
		String original = "" +
				"	void test() {\n" +
				"		Object o = \"\";\n" +
				"		if(o instanceof String) {\n" +
				"			String value = (String)o;\n" +
				"		}\n" +
				"	}";

		String expected = "" +
				"	void test() {\n" +
				"		Object o = \"\";\n" +
				"		if(o instanceof String value) {\n" +
				"		}\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_QualifiedNameAsInstanceOfLeftOperand_shouldTransform() throws Exception {
		String nestedClass = "" +
				"	class NestedClass {\n" +
				"		Object field = \"\";\n" +
				"	}";

		String original = "" +
				"	void test(NestedClass nestedClass) {\n" +
				"		if (nestedClass.field instanceof String) {\n" +
				"			String value = (String) nestedClass.field;\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				nestedClass;

		String expected = "" +
				"	void test(NestedClass nestedClass) {\n" +
				"		if (nestedClass.field instanceof String value) {\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				nestedClass;

		assertChange(original, expected);
	}

	@Test
	public void visit_ThisFieldAsInstanceOfLeftOperand_shouldTransform() throws Exception {

		String original = "" +
				"	Object field = \"\";\n" +
				"\n" +
				"	void test() {\n" +
				"		if (this.field instanceof String) {\n" +
				"			String value = (String) this.field;\n" +
				"		}\n" +
				"	}";

		String expected = "" +
				"	Object field = \"\";\n" +
				"\n" +
				"	void test() {\n" +
				"		if (this.field instanceof String value) {\n" +
				"		}\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_SuperFieldAsInstanceOfLeftOperand_shouldTransform() throws Exception {

		String original = "" +
				"	class ClassWithField {\n" +
				"		Object field = \"\";\n" +
				"	}\n" +
				"\n" +
				"	class ClassWithSuperFieldAccess extends ClassWithField{\n" +
				"		void test() {\n" +
				"			if (super.field instanceof String) {\n" +
				"				String value = (String) super.field;\n" +
				"			}\n" +
				"		}\n" +
				"	}";

		String expected = "" +
				"	class ClassWithField {\n" +
				"		Object field = \"\";\n" +
				"	}\n" +
				"\n" +
				"	class ClassWithSuperFieldAccess extends ClassWithField{\n" +
				"		void test() {\n" +
				"			if (super.field instanceof String value) {\n" +
				"			}\n" +
				"		}\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_TwoValueVariableDeclarationFragments_shouldTransform() throws Exception {

		String original = "" +
				"	void test() {\n" +
				"		Object o = \"\";\n" +
				"		if (o instanceof String) {\n" +
				"			String value = (String) o, value1 = \"\";\n" +
				"			System.out.println(value1);\n" +
				"		}\n" +
				"	}";

		String expected = "" +
				"	void test() {\n" +
				"		Object o = \"\";\n" +
				"		if (o instanceof String value) {\n" +
				"			String value1 = \"\";\n" +
				"			System.out.println(value1);\n" +
				"		}\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_NegatedInstanceOfWithValueInElse_shouldTransform() throws Exception {

		String original = "" +
				"	void test() {\n" +
				"		Object o = \"\";\n" +
				"		if (!(o instanceof String)) {\n" +
				"\n" +
				"		} else {\n" +
				"			String value = (String) o;\n" +
				"			System.out.println(value);\n" +
				"		}\n" +
				"	}";

		String expected = "" +
				"	void test() {\n" +
				"		Object o = \"\";\n" +
				"		if (!(o instanceof String value)) {\n" +
				"\n" +
				"		} else {\n" +
				"			System.out.println(value);\n" +
				"		}\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_IfNegatedInstanceOf_ThenReturn_shouldTransform() throws Exception {

		String original = "" +
				"	boolean test() {\n" +
				"		Object o = \"\";\n" +
				"		if (!(o instanceof String))\n" +
				"			return false;\n" +
				"\n" +
				"		String value = (String) o;\n" +
				"		System.out.println(value);\n" +
				"		return true;\n" +
				"	}";

		String expected = "" +
				"	boolean test() {\n" +
				"		Object o = \"\";\n" +
				"		if (!(o instanceof String value))\n" +
				"			return false;\n" +
				"\n" +
				"		System.out.println(value);\n" +
				"		return true;\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_IfNegatedInstanceOf_ThenBlockEndingWithReturn_shouldTransform() throws Exception {

		String original = "" +
				"	boolean test() {\n" +
				"		Object o = \"\";\n" +
				"		if (!(o instanceof String)) {\n" +
				"			return false;\n" +
				"		}\n" +
				"		String value = (String) o;\n" +
				"		System.out.println(value);\n" +
				"		return true;\n" +
				"	}";

		String expected = "" +
				"	boolean test() {\n" +
				"		Object o = \"\";\n" +
				"		if (!(o instanceof String value)) {\n" +
				"			return false;\n" +
				"		}\n" +
				"		System.out.println(value);\n" +
				"		return true;\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_CastingOtherVariable_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test() {\n" +
				"		Object o = \"\", o1 = \"\";\n" +
				"		if(o instanceof String) {\n" +
				"			String value = (String)o1;\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	/**
	 * Due to dropping the restriction to the first fragment, this test may fail
	 * in the future.
	 */
	@Test
	public void visit_NotFirstVariableFragment_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test() {\n" +
				"		Object o = \"\", o1 = \"\";\n" +
				"		if(o instanceof String) {\n" +
				"			String value = \"\", value1 = (String)o;\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_NoStatementInThenBlock_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test() {\n" +
				"		Object o = \"\", o1 = \"\";\n" +
				"		if(o instanceof String) {\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_ThenStatementIsNotBlock_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test() {\n" +
				"		Object o = \"\";\n" +
				"		String value;\n" +
				"		if(o instanceof String) value = (String)o;\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_VariableDeclarationNotFirstStatement_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test() {\n" +
				"		Object o = \"\";\n" +
				"		if(o instanceof String) {\n" +
				"			o = new Object();\n" +
				"			String value = (String)o;\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_ValueDeclarationWithoutInitializer_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test() {\n" +
				"		Object o = \"\";\n" +
				"		if(o instanceof String) {\n" +
				"			String value;\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_StringVarInstanceOfString_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test() {\n" +
				"		String s = \"\";\n" +
				"		if(s instanceof String) {\n" +
				"			String value = (String)s;\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_InstanceOfCharSequenceCastToString_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test() {\n" +
				"		Object o = \"\";\n" +
				"		if (o instanceof CharSequence) {\n" +
				"			String value = (String) o;\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_InstanceOfClassCastToSubclass_shouldNotTransform() throws Exception {
		String original = "" +
				"	class ExampleClass {\n" +
				"	}\n" +
				"\n" +
				"	class ExampleSubClass extends ExampleClass {\n" +
				"	}\n" +
				"\n" +
				"	void test() {\n" +
				"		Object o = new ExampleSubClass();\n" +
				"		if (o instanceof ExampleClass) {\n" +
				"			ExampleSubClass value = (ExampleSubClass) o;\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_InstanceOfClassCastToSuperClass_shouldNotTransform() throws Exception {
		String original = "" +
				"	class ExampleClass {\n" +
				"	}\n" +
				"\n" +
				"	class ExampleSubClass extends ExampleClass {\n" +
				"	}\n" +
				"\n" +
				"	void test() {\n" +
				"		Object o = new ExampleSubClass();\n" +
				"		if (o instanceof ExampleSubClass) {\n" +
				"			ExampleClass value = (ExampleClass) o;\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_IfNegatedInstanceOf_EmptyThenNoElse_shouldNotTransform() throws Exception {
		String original = "" +
				"	void testWithEmptyThenStatement() {\n" +
				"		Object o = \"\";\n" +
				"		if (!(o instanceof String));\n" +
				"		String value = (String) o;\n" +
				"		System.out.println(value);\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_IfNegatedInstanceOf_ThenEmptyBlockNoElse_shouldNotTransform() throws Exception {
		String original = "" +
				"	void testWithThenEmptyBlock() {\n" +
				"		Object o = \"\";\n" +
				"		if (!(o instanceof String)) {\n" +
				"			\n" +
				"		}\n" +
				"		String value = (String) o;\n" +
				"		System.out.println(value);\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_IfNegatedInstanceOf_ThenNotEndingWithReturn_shouldNotTransform() throws Exception {
		String original = "" +
				"	void testWithThenNotEndingWithReturn() {\n" +
				"		Object o = \"\";\n" +
				"		if (!(o instanceof String)) {\n" +
				"			System.out.println(\"o is t instanceof String\");\n" +
				"		}\n" +
				"		String value = (String) o;\n" +
				"		System.out.println(value);\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_ElseIfNegatedInstanceOf_ThenBlockEndingWithReturn_shouldNotTransform() throws Exception {
		String original = "" +
				"	boolean test(boolean condition) {\n" +
				"		Object o = \"\";\n" +
				"		if (condition) {\n" +
				"		} else if (!(o instanceof String)) {\n" +
				"			return false;\n" +
				"		}\n" +
				"		String value = (String) o;\n" +
				"		System.out.println(value);\n" +
				"		return true;\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_InstanceOfStringAndCondition_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test(boolean condition) {\n" +
				"		Object o = \"\";\n" +
				"		if (o instanceof String && condition) {\n" +
				"			String value = (String) o;\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_ParenthesizedInstanceOfStringAndCondition_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test(boolean condition) {\n" +
				"		Object o = \"\";\n" +
				"		if ((o instanceof String) && condition) {\n" +
				"			String value = (String) o;\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_NegatedParenthesizedInstanceOfStringAndCondition_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test(boolean condition) {\n" +
				"		Object o = \"\";\n" +
				"		if (!(o instanceof String) && condition) {\n" +
				"			String value = (String) o;\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_IfNegatedInstanceOf_EndOfBlockAfterIf_shouldNotTransform() throws Exception {
		String original = "" +
				"	boolean test_IfNegatedInstanceOf_EndOfBlockAfterIf_shouldNotTransform() {\n" +
				"		Object o = \"\";\n" +
				"		{\n" +
				"			if (!(o instanceof String)) {\n" +
				"				return false;\n" +
				"			}\n" +
				"		}\n" +
				"		String value = (String) o;\n" +
				"		return true;\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_IfNegatedInstanceOf_NoVariableDeclarationAfterIf_shouldNotTransform() throws Exception {
		String original = "" +
				"	boolean test() {\n" +
				"		Object o = \"\";\n" +
				"		if (!(o instanceof String)) {\n" +
				"			return false;\n" +
				"		}\n" +
				"		o = new Object();\n" +
				"		String value = (String) o;\n" +
				"		System.out.println(value);\n" +
				"		return true;\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_GetObjectAsInstanceOfLeftOperand_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test() {\n" +
				"		if (getObject() instanceof String) {\n" +
				"			String value = (String) getObject();\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				"	Object getObject() {\n" +
				"		return \"\";\n" +
				"	}";

		assertNoChange(original);
	}
}
