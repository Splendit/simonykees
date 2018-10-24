package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.Before;
import org.junit.Test;

public class UseStringBuilderAppendASTVisitorTest extends UsesJDTUnitFixture {

	private UseStringBuilderAppendASTVisitor visitor;
	
	
	@Before
	public void setUp() {
		visitor = new UseStringBuilderAppendASTVisitor();
	}
	
	@Test
	public void visit_simpleConcatenation_shouldReplace() throws Exception {
		String value = "first" + "second";
		value = new StringBuilder().append("first").append("second").toString();
		String block = "String value = \"first\" + \"second\";";
		String expected = "String value = new StringBuilder().append(\"first\").append(\"second\").toString();";
		
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
}
