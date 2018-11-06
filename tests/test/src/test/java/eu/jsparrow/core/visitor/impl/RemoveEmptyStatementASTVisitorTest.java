package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.Before;
import org.junit.Test;

public class RemoveEmptyStatementASTVisitorTest extends UsesJDTUnitFixture {
	
	private RemoveEmptyStatementASTVisitor visitor;
	
	@Before
	public void setUp() {
		 visitor = new RemoveEmptyStatementASTVisitor();
	}
	
	@Test
	public void visit_removeEmptyStatementFromMethodBody_shouldRefactor() throws Exception {
		String block = "; int i = 0;";
		String expected = "int i = 0;";
		
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = createBlock(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_removeMultipleEmptyStatementFromMethodBody_shouldRefactor() throws Exception {
		String block = ";;;;;; ;;; ;;;; ;; int i = 0;;;;; ;;; ;;";
		String expected = "int i = 0;";
		
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = createBlock(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	
	@Test
	public void visit_removeEmptyStatementFromIfBody_shouldRefactor() throws Exception {
		String block = "if(true) {;} int i = 0;";
		String expected = "if(true) { } int i = 0;";
		
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = createBlock(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_singleBodyIfStatement_shouldNotRefactor() throws Exception {
		String block = "if(true) ; int i = 0;";
		
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = createBlock(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	
	@Test
	public void visit_singleBodyForStatement_shouldNotRefactor() throws Exception {
		String block = "for(String string : Arrays.asList(\"\")) ;";
		
		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = createBlock(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_emptyForLoopHeader_shouldNotRefactor() throws Exception {
		String block = "for(String string : Arrays.asList(\"\")) ;";
		
		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = createBlock(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	

}
