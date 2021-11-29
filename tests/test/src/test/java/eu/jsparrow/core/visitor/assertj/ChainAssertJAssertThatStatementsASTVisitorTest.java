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
	public void visit_AssertThatIsNotNullAndIsNotEmpty_shouldTransform() throws Exception {

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

	// @Test
	// public void visit__shouldTransform() throws Exception {
	//
	//
	// String original = "" +
	// "";
	//
	// String expected = "" +
	// "";
	//
	// assertChange(original, expected);
	// }
}