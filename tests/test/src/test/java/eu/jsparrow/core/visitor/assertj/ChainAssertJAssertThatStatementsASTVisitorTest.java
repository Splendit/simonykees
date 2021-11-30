package eu.jsparrow.core.visitor.assertj;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;

@SuppressWarnings("nls")
public class ChainAssertJAssertThatStatementsASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("org.assertj", "assertj-core", "3.21.0");
		setDefaultVisitor(new ChainAssertJAssertThatStatementsASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_AssertThatListIsNotNullAndIsNotEmpty_shouldTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.Arrays.class.getName());

		String original = "" +
				"	List<String> stringList = Arrays.asList(\"s1\", \"s2\");\n"
				+ "\n"
				+ "	public void testIsNotNullIsNotEmpty() {\n"
				+ "		assertThat(stringList).isNotNull();\n"
				+ "		assertThat(stringList).isNotEmpty();\n"
				+ "	}";

		String expected = "" +
				"	List<String> stringList = Arrays.asList(\"s1\", \"s2\");\n"
				+ "\n"
				+ "	public void testIsNotNullIsNotEmpty() {\n"
				+ "		assertThat(stringList).isNotNull().isNotEmpty();\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertThatOnThisField_shouldTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.Arrays.class.getName());

		String original = "" +
				"	List<String> stringList = Arrays.asList(\"s1\", \"s2\");\n"
				+ "\n"
				+ "	public void assertNotNullAndNotEmpty() {\n"
				+ "		assertThat(this.stringList).isNotNull();\n"
				+ "		assertThat(this.stringList).isNotEmpty();\n"
				+ "	}";

		String expected = "" +
				"	List<String> stringList = Arrays.asList(\"s1\", \"s2\");\n"
				+ "\n"
				+ "	public void assertNotNullAndNotEmpty() {\n"
				+ "		assertThat(this.stringList).isNotNull().isNotEmpty();\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertThatOnSuperField_shouldTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.Arrays.class.getName());

		String original = "" +
				"	class SuperClass {\n"
				+ "		List<String> stringList = Arrays.asList(\"s1\", \"s2\");\n"
				+ "	}\n"
				+ "\n"
				+ "	class SubClass extends SuperClass {\n"
				+ "		public void assertNotNullAndNotEmpty() {\n"
				+ "			assertThat(super.stringList).isNotNull();\n"
				+ "			assertThat(super.stringList).isNotEmpty();\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	class SuperClass {\n"
				+ "		List<String> stringList = Arrays.asList(\"s1\", \"s2\");\n"
				+ "	}\n"
				+ "\n"
				+ "	class SubClass extends SuperClass {\n"
				+ "		public void assertNotNullAndNotEmpty() {\n"
				+ "			assertThat(super.stringList).isNotNull().isNotEmpty();\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertionsOnConstant_shouldTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);

		String original = "" +
				"	public void test() {\n"
				+ "		assertThat(java.lang.Integer.MAX_VALUE).isNotEqualTo(1000);\n"
				+ "		assertThat(java.lang.Integer.MAX_VALUE).isGreaterThan(1000);\n"
				+ "	}";

		String expected = "" +
				"	public void test() {\n"
				+ "		assertThat(java.lang.Integer.MAX_VALUE).isNotEqualTo(1000).isGreaterThan(1000);\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertThatOnThis_shouldTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);

		String original = "" +
				"	class ExampleClass {\n"
				+ "		void test() {\n"
				+ "			assertThat(this).isNotNull();\n"
				+ "			assertThat(this).isInstanceOf(ExampleClass.class);\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	class ExampleClass {\n"
				+ "		void test() {\n"
				+ "			assertThat(this).isNotNull().isInstanceOf(ExampleClass.class);\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertThatOnNumberLiteral_shouldTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);

		String original = "" +
				"	public void assertThatOnIntLiteral() {\n"
				+ "		assertThat(1000).isEqualTo(1000);\n"
				+ "		assertThat(1000).isGreaterThan(999);\n"
				+ "	}";

		String expected = "" +
				"	public void assertThatOnIntLiteral() {\n"
				+ "		assertThat(1000).isEqualTo(1000).isGreaterThan(999);\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertThatOnCharatcerLiteral_shouldTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);

		String original = "" +
				"	public void assertThatOnCharacterLiteral() {\n"
				+ "		assertThat('A').isEqualTo('A');\n"
				+ "		assertThat('A').isGreaterThan('@');\n"
				+ "	}";

		String expected = "" +
				"	public void assertThatOnCharacterLiteral() {\n"
				+ "		assertThat('A').isEqualTo('A').isGreaterThan('@');\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertThatOnStringLiteral_shouldTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat",
				true, false);

		String original = "" +
				"	public void assertThatOnStringLiteral() {\n"
				+ "		assertThat(\"HelloWorld\").isNotNull();\n"
				+ "		assertThat(\"HelloWorld\").isNotEmpty();\n"
				+ "	}";

		String expected = "" +
				"	public void assertThatOnStringLiteral() {\n"
				+ "		assertThat(\"HelloWorld\").isNotNull().isNotEmpty();\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertThatOnTypeLiteral_shouldTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat",
				true, false);

		String original = "" +
				"	public void assertThatOnTypeLiteral() {\n"
				+ "		assertThat(String.class).isFinal();\n"
				+ "		assertThat(String.class).isNotInterface();\n"
				+ "	}";

		String expected = "" +
				"	public void assertThatOnTypeLiteral() {\n"
				+ "		assertThat(String.class).isFinal().isNotInterface();\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertThatOnThisFieldAccessChain_shouldTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat",
				true, false);

		String original = "" +
				"	IntWrapper intWrapper = new IntWrapper();\n"
				+ "\n"
				+ "	public void assertThatOnThisFieldAccessChain() {\n"
				+ "\n"
				+ "		assertThat(this.intWrapper.intValue).isEqualTo(0);\n"
				+ "		assertThat(this.intWrapper.intValue).isGreaterThan(-1);\n"
				+ "	}\n"
				+ "\n"
				+ "	class IntWrapper {\n"
				+ "		int intValue;\n"
				+ "	}";

		String expected = "" +
				"	IntWrapper intWrapper = new IntWrapper();\n"
				+ "\n"
				+ "	public void assertThatOnThisFieldAccessChain() {\n"
				+ "\n"
				+ "		assertThat(this.intWrapper.intValue).isEqualTo(0).isGreaterThan(-1);\n"
				+ "	}\n"
				+ "\n"
				+ "	class IntWrapper {\n"
				+ "		int intValue;\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertThatOnSuperFieldAccessChain_shouldTransform() throws Exception {
		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat",
				true, false);

		String original = "" +
				"	class SuperClass {\n"
				+ "		IntWrapper intWrapper = new IntWrapper();\n"
				+ "	}\n"
				+ "\n"
				+ "	class SubClass extends SuperClass {\n"
				+ "		public void assertThatOnSuperFieldAccessChain() {\n"
				+ "			assertThat(super.intWrapper.intValue).isEqualTo(0);\n"
				+ "			assertThat(super.intWrapper.intValue).isGreaterThan(-1);\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	class IntWrapper {\n"
				+ "		int intValue;\n"
				+ "	}";

		String expected = "" +
				"	class SuperClass {\n"
				+ "		IntWrapper intWrapper = new IntWrapper();\n"
				+ "	}\n"
				+ "\n"
				+ "	class SubClass extends SuperClass {\n"
				+ "		public void assertThatOnSuperFieldAccessChain() {\n"
				+ "			assertThat(super.intWrapper.intValue).isEqualTo(0).isGreaterThan(-1);\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	class IntWrapper {\n"
				+ "		int intValue;\n"
				+ "	}";

		assertChange(original, expected);
	}
	
	
//	@Test
//	public void visit__shouldTransform() throws Exception {
//		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat",
//				true, false);
//
//		String original = "" +
//				"";
//
//		String expected = "" +
//				"";
//
//		assertChange(original, expected);
//	}

}