package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;

class ReplaceSetRemoveAllWithForEachASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new ReplaceSetRemoveAllWithForEachASTVisitor());
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	/*
	 * This test will fail as soon as {@link
	 * ReplaceSetRemoveAllWithForEachASTVisitor} will have been fully
	 * implemented.
	 * 
	 */
	@Test
	void visit_SetRemoveAll_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.Set.class.getName());
		String original = "" +
				"	void exampleWithParametersForSetAndList(Set<String> stringSet, List<String> stringsToRemove) {\n" +
				"		stringSet.removeAll(stringsToRemove);\n" +
				"	}";

		String expected = "" +
				"	void exampleWithParametersForSetAndList(Set<String> stringSet, List<String> stringsToRemove) {\n" +
				"		stringsToRemove.forEach(stringSet::remove);\n" +
				"	}";

		assertChange(original, expected);
	}
}