package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;

class ReplaceSetRemoveAllWithForEachASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new ReplaceSetRemoveAllWithForEachASTVisitor());
		defaultFixture.addImport(java.util.HashSet.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.Set.class.getName());
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
		String original = "" +
				"	void exampleWithStringVarargs(Set<String> mySet, String... strings) {\n" +
				"		mySet.removeAll(List.of(strings));\n" +
				"	}";

		String expected = "" +
				"	void exampleWithStringVarargs(Set<String> mySet, String... strings) {\n" +
				"		mySet.removeAll(List.of(strings));\n" +
				"	}";

		assertChange(original, expected);
	}
}