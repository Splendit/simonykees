package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.Block;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.dummies.ASTRewriteVisitorListenerStub;

@SuppressWarnings("nls")
public class StatementLambdaToExpressionASTVisitorTest  extends UsesJDTUnitFixture {

	private StatementLambdaToExpressionASTVisitor visitor;
	
	private String blockTemplate = "new ArrayList<>().forEach(element -> %s);";
	
	@Before
	public void setUp() {
		visitor = new StatementLambdaToExpressionASTVisitor();
	}
	
	@Test
	public void visit_simpleExpressionLambda_shouldReplace() throws Exception {
		fixture.addImport("java.util.ArrayList");
		String lambda = "{ new String();}";
		String block = String.format(blockTemplate, lambda);
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

	
		Block expected = createBlock(String.format(blockTemplate, "new String()"));
		assertMatch(expected, fixture.getMethodBlock());
		
		new ArrayList<>().forEach(element -> new String());
	}
	
	@Test
	public void visit_simpleReturnLambda_shouldReplace() throws Exception {
		fixture.addImport("java.util.ArrayList");
		String lambda = "{ new String(); return; }";
		String block = String.format(blockTemplate, lambda);
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

	
		Block expected = createBlock(String.format(blockTemplate, "new String()"));
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_tooManyStatements_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.ArrayList");
		String lambda = "{ new String(); new String(); return; }";
		String block = String.format(blockTemplate, lambda);
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_invalidStatement_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.ArrayList");
		String lambda = "{ int d = 0; }";
		String block = String.format(blockTemplate, lambda);
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_simplePredicate_shouldReplace() throws Exception {
		fixture.addImport("java.util.ArrayList");

		String block = "new ArrayList<>().stream().filter(element -> { return true; });";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock("new ArrayList<>().stream().filter(element -> true);");
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_whenReplacementHappens_ShouldUpdateListeners() throws Exception {
		ASTRewriteVisitorListenerStub listener = new ASTRewriteVisitorListenerStub();
		visitor.addRewriteListener(listener);
		fixture.addImport("java.util.ArrayList");
		String lambda = "{ new String();}";
		String block = String.format(blockTemplate, lambda);
		fixture.addMethodBlock(block);
		
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);


		assertTrue(listener.wasUpdated());
	}



}
