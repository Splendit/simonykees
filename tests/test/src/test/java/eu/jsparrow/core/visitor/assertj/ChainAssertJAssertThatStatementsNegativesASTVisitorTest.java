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
	public void visit_AssertThatOnElementsOfSameList_shouldNotTransform() throws Exception {
		
		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
		defaultFixture.addImport(java.util.List.class.getName());

		String original = "" + //
				"	public void assertThatOnDifferentListElements(List<String> stringList) {\n"
				+ "		assertThat(stringList).element(0).asString().contains(\"-1\");\n"
				+ "		assertThat(stringList).element(1).asString().contains(\"-2\");\n"
				+ "	}";

		assertNoChange(original);
	}	
}