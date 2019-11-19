package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

@SuppressWarnings("nls")
public class RemoveEmptyStatementASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private RemoveEmptyStatementASTVisitor visitor;

	@BeforeEach
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

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_removeEmptyStatementFromSwitchCaseStatement_shouldRefactor() throws Exception {
		String block = "switch (\"\") { case \"\": ; break; default: break; }";
		String expected = "switch (\"\") { case \"\":  break; default: break; }";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_removeEmptyStatementFromSwitchDefaultStatement_shouldRefactor() throws Exception {
		String block = "switch (\"\") { case \"\": break; default: ; break; }";
		String expected = "switch (\"\") { case \"\":  break; default: break; }";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_removeMultipleEmptyStatementFromMethodBody_shouldRefactor() throws Exception {
		String block = ";;;;;; ;;; ;;;; ;; int i = 0;;;;; ;;; ;;";
		String expected = "int i = 0;";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_removeEmptyStatementFromIfBody_shouldRefactor() throws Exception {
		String block = "if(true) {;} int i = 0;";
		String expected = "if(true) { } int i = 0;";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_singleBodyIfStatement_shouldNotRefactor() throws Exception {
		String block = "if(true) ; int i = 0;";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_singleBodyForStatement_shouldNotRefactor() throws Exception {
		String block = "for(String string : Arrays.asList(\"\")) ;";

		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_emptyForLoopHeader_shouldNotRefactor() throws Exception {
		String block = "for(String string : Arrays.asList(\"\")) ;";

		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
}
