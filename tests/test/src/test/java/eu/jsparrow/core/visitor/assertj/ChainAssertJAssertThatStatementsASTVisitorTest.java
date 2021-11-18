package eu.jsparrow.core.visitor.assertj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

public class ChainAssertJAssertThatStatementsASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("org.assertj", "assertj-core", "3.21.0");
		fixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
		setVisitor(new ChainAssertJAssertThatStatementsASTVisitor());
	}

	@Test
	public void visit_ForResearch_AllAssertionsOnSameObject_shouldNotTransform() throws Exception {

		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.List.class.getName());

		String original = "" + //
				"		List<String> stringList =  Arrays.asList(\"String-1\", \"String-2\", \"String-3\", \"String-4\");\n"
				+ "		assertThat(stringList).isNotNull();\n"
				+ "		assertThat(stringList).isNotEmpty();\n"
				+ "		assertThat(stringList).contains(\"String-1\");\n"
				+ "		assertThat(stringList).contains(\"String-2\", atIndex(1)).contains(\"String-3\", atIndex(2));\n"
				+ "		assertThat(stringList).containsAll(Arrays.asList(\"String-3\", \"String-4\"));";

		assertNoChange(original);
	}
	
	@Test
	public void visit_ForResearch_AssertionsOnDifferentObjects_shouldNotTransform() throws Exception {

		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.List.class.getName());

		String original = "" + //
				"		List<String> stringList =  Arrays.asList(\"String-1\", \"String-2\", \"String-3\", \"String-4\");\n"
				+ "		List<String> stringList2 =  Arrays.asList(\"String-1\", \"String-2\", \"String-3\", \"String-4\");\n"
				+ "		assertThat(stringList).isNotNull();\n"
				+ "		assertThat(stringList).isNotEmpty();\n"
				+ "		assertThat(stringList2).contains(\"String-1\");\n"
				+ "		assertThat(stringList2).contains(\"String-2\", atIndex(1)).contains(\"String-3\", atIndex(2));\n"
				+ "		assertThat(stringList2).containsAll(Arrays.asList(\"String-3\", \"String-4\"));";

		assertNoChange(original);
	}
}