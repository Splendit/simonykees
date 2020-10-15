package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UseComparatorMethodsASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new UseComparatorMethodsASTVisitor());
		defaultFixture.addImport(java.util.Comparator.class.getName());
	}
	
	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}
	
	@Test
	public void visit_Comparator4ArrayListByFirstItem_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.ArrayList.class.getName());
		String original = "" +
				"	void test() {\n" +
				"		Comparator<ArrayList<Integer>> arrayListComparator = (l1, l2) -> l1.get(0).compareTo(l2.get(0));\n" +
				"	}";

		assertNoChange(original);
	}

}
