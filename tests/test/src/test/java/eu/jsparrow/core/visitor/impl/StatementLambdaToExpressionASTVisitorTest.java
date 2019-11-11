package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.dummies.ASTRewriteVisitorListenerStub;

@SuppressWarnings("nls")
public class StatementLambdaToExpressionASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private StatementLambdaToExpressionASTVisitor visitor;

	private String blockTemplate = "new ArrayList<>().forEach(element -> %s);";

	@BeforeEach
	public void setUp() throws Exception {
		visitor = new StatementLambdaToExpressionASTVisitor();
		fixture.addImport("java.util.ArrayList");
	}

	@Test
	public void visit_simpleExpressionLambda_shouldReplace() throws Exception {
		String lambda = "{ new String();}";
		String block = String.format(blockTemplate, lambda);
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(String.format(blockTemplate, "new String()"));
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_simpleReturnLambda_shouldReplace() throws Exception {

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
		String lambda = "{ new String();}";
		String block = String.format(blockTemplate, lambda);
		fixture.addMethodBlock(block);

		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertTrue(listener.wasUpdated());
	}

}
