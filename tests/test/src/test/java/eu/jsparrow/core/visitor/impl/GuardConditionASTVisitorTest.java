package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.Before;
import org.junit.Test;

public class GuardConditionASTVisitorTest extends UsesJDTUnitFixture {
	
	
	private GuardConditionASTVisitor visitor;
	
	@Before
	public void setUp() {
		visitor = new GuardConditionASTVisitor();
	}
	
	@Test
	public void visit_singleIfStatementBody_shouldReplace() throws Exception {
		
		String block = "if(true) { System.out.println(\"always true\"); if(false) { System.out.println(\"always false\");}}";
		String varDeclaration = "if(false) {return;} System.out.println(\"always true\"); if(false) { System.out.println(\"always false\"); }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = createBlock(varDeclaration);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
}
