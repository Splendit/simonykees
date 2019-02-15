package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings({ "nls" })
public class CollapseIfStatementsASTVisitorTest extends UsesJDTUnitFixture {

	private CollapseIfStatementsASTVisitor visitor;
	
	
	@Before
	public void setUp() {
		this.visitor = new CollapseIfStatementsASTVisitor();
	}
	
	@Test
	public void visit_simpleNestedIf_shouldTransform() throws Exception {
		String block = ""
				+ "		boolean condition = true;\n" + 
				"		boolean innerCondition = true;\n" + 
				"		if(condition) {\n" + 
				"			if(innerCondition) {\n" + 
				"				condition = false;\n" + 
				"				innerCondition = false;\n" + 
				"			}\n" + 
				"		}";
		String expectedBlock = ""
				+ "		boolean condition = true;\n" + 
				"		boolean innerCondition = true;\n" + 
				"		if(condition && innerCondition) {\n" + 
				"			condition = false;\n" + 
				"			innerCondition = false;\n" + 
				"		}";
		
		
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(expectedBlock);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	private void samples() {

		boolean condition = true;
		boolean innerCondition = true;
		// before
		if(condition) {
			if(innerCondition) {
				condition = false;
				innerCondition = false;
			}
		}
		
		// before
		if(condition && innerCondition) {
			condition = false;
			innerCondition = false;
		}
	}
	
}
