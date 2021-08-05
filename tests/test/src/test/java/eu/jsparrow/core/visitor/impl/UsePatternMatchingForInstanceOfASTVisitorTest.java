package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.rules.java16.UsePatternMatchingForInstanceofASTVisitor;

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
	public void visit_GetObjectAsInstanceOfLeftOperand_shouldTransform() throws Exception {
		String getObjectMethod = "" +
				"	Object getObject() {\n" +
				"		return \"\";\n" +
				"	}";

		String original = "" +
				"	void test() {\n" +
				"		if (getObject() instanceof String) {\n" +
				"			String value = (String) getObject();\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				getObjectMethod;

		String expected = "" +
				"	void test() {\n" +
				"		if (getObject() instanceof String value) {\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				getObjectMethod;

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
	public void visit_SuperGetMethodAsInstanceOfLeftOperand_shouldTransform() throws Exception {

		String original = "" +
				"	class ClassWithGetObject {\n" +
				"		Object getObject() {\n" +
				"			return \"\";\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				"	class ClassUsingSuperGetObject extends ClassWithGetObject {\n" +
				"		void test() {\n" +
				"			if (super.getObject() instanceof String) {\n" +
				"				String value = (String) super.getObject();\n" +
				"			}\n" +
				"		}\n" +
				"	}";

		String expected = "" +
				"	class ClassWithGetObject {\n" +
				"		Object getObject() {\n" +
				"			return \"\";\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				"	class ClassUsingSuperGetObject extends ClassWithGetObject {\n" +
				"		void test() {\n" +
				"			if (super.getObject() instanceof String value) {\n" +
				"			}\n" +
				"		}\n" +
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
}
