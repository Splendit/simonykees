package eu.jsparrow.core.visitor.assertj;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;

@SuppressWarnings("nls")
public class ChainAssertJAssertThatStatementsNegativesASTVisitorTest extends UsesJDTUnitFixture {

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
	public void visit_ExpressionStatementNotWithInvocation_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat",
				true, false);
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.Arrays.class.getName());
		defaultFixture.addImport("org.assertj.core.api.ListAssert");

		String original = "" +
				"	List<String> stringList = Arrays.asList(\"String-1\", \"String-2\", \"String-3\", \"String-4\");\n"
				+ "\n"
				+ "	public void testExpressionStatementNotWithInvocation() {\n"
				+ "		ListAssert<String> listAssert = assertThat(stringList).isNotNull();\n"
				+ "		listAssert = assertThat(stringList).isNotEmpty();\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_AssertThatWithoutSubsequentChain_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat",
				true, false);
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.Arrays.class.getName());

		String original = "" +
				"	List<String> stringList = Arrays.asList(\"String-1\", \"String-2\", \"String-3\", \"String-4\");\n"
				+ "\n"
				+ "	public void testAssertThatWithoutSubsequentChain() {\n"
				+ "		assertThat(stringList);\n"
				+ "		assertThat(stringList);\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_MethodNameNotAssertThat_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.Arrays.class.getName());
		defaultFixture.addImport("org.assertj.core.api.ListAssert");
		defaultFixture.addImport("org.assertj.core.api.Assertions");

		String original = "" +
				"	List<String> stringList = Arrays.asList(\"String-1\", \"String-2\", \"String-3\", \"String-4\");\n"
				+ "	\n"
				+ "	ListAssert<String> assertStringList(List<String> stringList) {\n"
				+ "		return Assertions.assertThat(stringList);\n"
				+ "	}\n"
				+ "\n"
				+ "	public void testMethodNameNotAssertThat() {\n"
				+ "		assertStringList(stringList).isNotNull();\n"
				+ "		assertStringList(stringList).isNotEmpty();\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_AssertThatWithMultipleArguments_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.Arrays.class.getName());
		defaultFixture.addImport("org.assertj.core.api.ListAssert");
		defaultFixture.addImport("org.assertj.core.api.Assertions");

		String original = "" +
				"	ListAssert<String> assertThat(String... strings) {\n"
				+ "		return Assertions.assertThat(Arrays.asList(strings));\n"
				+ "	}\n"
				+ "\n"
				+ "	public void testAssertThatWithMultipleArguments() {\n"
				+ "		assertThat(\"String-1\", \"String-2\").isNotNull();\n"
				+ "		assertThat(\"String-1\", \"String-2\").isNotEmpty();\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_AssertThatNotResolved_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.Arrays.class.getName());

		String original = "" +
				"	List<String> stringList = Arrays.asList(\"String-1\", \"String-2\", \"String-3\", \"String-4\");\n"
				+ "\n"
				+ "	public void testAssertThatNotResolved() {\n"
				+ "		assertThat(stringList).isNotNull();\n"
				+ "		assertThat(stringList).isNotEmpty();\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_AssertThatNotDeclaredByAssertJAssertions_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.Arrays.class.getName());
		defaultFixture.addImport("org.assertj.core.api.ListAssert");
		defaultFixture.addImport("org.assertj.core.api.Assertions");

		String original = "" +
				"	List<String> stringList = Arrays.asList(\"String-1\", \"String-2\", \"String-3\", \"String-4\");\n"
				+ "	\n"
				+ "	ListAssert<String> assertThat(List<String> stringList) {\n"
				+ "		return Assertions.assertThat(stringList);\n"
				+ "	}\n"
				+ "\n"
				+ "	public void testAssertThatNotDeclaredByAssertJAssertions() {\n"
				+ "		assertThat(stringList).isNotNull();\n"
				+ "		assertThat(stringList).isNotEmpty();\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_NotSupportedInvocationChainElements_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.Arrays.class.getName());

		String original = "" +
				"	List<String> stringList = Arrays.asList(\"String-1\", \"String-2\", \"String-3\", \"String-4\");\n"
				+ "\n"
				+ "	public void assertThatWithNotSupportedInvocationChainElements() {\n"
				+ "		assertThat(stringList).element(1);\n"
				+ "		assertThat(stringList).element(0);\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_FirstAssertThatWithoutFollowing_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat",
				true, false);
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.Arrays.class.getName());

		String original = "" +
				"	List<String> stringList = Arrays.asList(\"String-1\", \"String-2\", \"String-3\", \"String-4\");\n"
				+ "\n"
				+ "	public void testAssertThatWithoutFollowing() {\n"
				+ "		assertThat(stringList).isNotNull();\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_AssertThatOnCreateListInvocation_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat",
				true, false);
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.Arrays.class.getName());

		String original = "" +
				"	List<String> createList() {\n"
				+ "		return Arrays.asList(\"String-1\", \"String-2\", \"String-3\", \"String-4\");\n"
				+ "	}\n"
				+ "\n"
				+ "	public void assertThatOnCreateListInvocation() {\n"
				+ "		assertThat(createList()).isNotNull();\n"
				+ "		assertThat(createList()).isNotEmpty();\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_AssertThatOnDifferentLists_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat",
				true, false);
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.Arrays.class.getName());

		String original = "" +
				"	List<String> stringList1 = Arrays.asList(\"String-1\", \"String-2\", \"String-3\", \"String-4\");\n"
				+ "	List<String> stringList2 = Arrays.asList(\"String-1\", \"String-2\", \"String-3\", \"String-4\");\n"
				+ "	\n"
				+ "	public void testMultipleAssertThatWithoutInvocationChain() {\n"
				+ "		assertThat(stringList1).isNotNull();\n"
				+ "		assertThat(stringList2).isNotEmpty();\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_AssertThatOnIntValueOfNewIntWrapper_shouldNotTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);

		String original = "" +
				"	public void assertThatOnIntValueOfNewIntWrapper() {\n"
				+ "\n"
				+ "		assertThat(new IntWrapper().intValue).isEqualTo(0);\n"
				+ "		assertThat(new IntWrapper().intValue).isGreaterThan(-1);\n"
				+ "	}\n"
				+ "\n"
				+ "	class IntWrapper {\n"
				+ "		int intValue;\n"
				+ "	}";

		assertNoChange(original);
	}
}