package eu.jsparrow.core.visitor.assertj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

@SuppressWarnings("nls")
public class ChainAssertJAssertThatStatementsASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("org.assertj", "assertj-core", "3.21.0");
		fixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
		fixture.addImport("org.assertj.core.api.Assertions.atIndex", true, false);
		setVisitor(new ChainAssertJAssertThatStatementsASTVisitor());
	}

	@Test
	public void visit_AllAssertionsOnSameList_shouldTransform() throws Exception {

		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.List.class.getName());

		String original = "" + //
				"		List<String> stringList = Arrays.asList(\"String-1\", \"String-2\", \"String-3\", \"String-4\");\n"
				+ "		assertThat(stringList).isNotNull();\n"
				+ "		assertThat(stringList).isNotEmpty();\n"
				+ "		assertThat(stringList) //\n"
				+ "				.contains(\"String-1\", atIndex(0))\n"
				+ "				.contains(\"String-2\", atIndex(1))\n"
				+ "				.contains(\"String-3\", atIndex(2))\n"
				+ "				.contains(\"String-4\", atIndex(3));\n"
				+ "		assertThat(stringList).containsAll(Arrays.asList(\"String-3\", \"String-4\"));";

		String expected = "" +
				"		List<String> stringList = Arrays.asList(\"String-1\", \"String-2\", \"String-3\", \"String-4\");\n"
				+ "		assertThat(stringList)"
				+ "			.isNotNull()"
				+ "			.isNotEmpty()"
				+ "			.contains(\"String-1\", atIndex(0))"
				+ "			.contains(\"String-2\", atIndex(1))"
				+ "			.contains(\"String-3\", atIndex(2))\n"
				+ "			.contains(\"String-4\", atIndex(3))"
				+ "			.containsAll(Arrays.asList(\"String-3\", \"String-4\"));\n"
				+ "";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertionsOnTwoDifferentLists_shouldNotTransform() throws Exception {

		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.List.class.getName());

		String original = "" + //
				"		List<String> stringList = Arrays.asList(\"\");\n"
				+ "		List<String> stringList2 = Arrays.asList(\"String-1\", \"String-2\", \"String-3\", \"String-4\");\n"
				+ "		assertThat(stringList).isNotNull();\n"
				+ "		assertThat(stringList).isNotEmpty();\n"
				+ "		assertThat(stringList2) //\n"
				+ "				.contains(\"String-1\", atIndex(0)) //\n"
				+ "				.contains(\"String-2\", atIndex(1)) //\n"
				+ "				.contains(\"String-3\", atIndex(2)) //\n"
				+ "				.contains(\"String-4\", atIndex(3));\n"
				+ "		assertThat(stringList2).containsAll(Arrays.asList(\"String-3\", \"String-4\"));";

		String expected = "" +
				"		List<String> stringList = Arrays.asList(\"\");\n"
				+ "		List<String> stringList2 = Arrays.asList(\"String-1\", \"String-2\", \"String-3\", \"String-4\");\n"
				+ "		assertThat(stringList)\n"
				+ "			.isNotNull()\n"
				+ "			.isNotEmpty();\n"
				+ "		assertThat(stringList2) //\n"
				+ "				.contains(\"String-1\", atIndex(0))\n"
				+ "				.contains(\"String-2\", atIndex(1))\n"
				+ "				.contains(\"String-3\", atIndex(2))\n"
				+ "				.contains(\"String-4\", atIndex(3))\n"
				+ "				.containsAll(Arrays.asList(\"String-3\", \"String-4\"));";
		
		assertChange(original, expected);
	}
}