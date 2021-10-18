package eu.jsparrow.core.visitor.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.dummies.ASTRewriteVisitorListenerStub;

@SuppressWarnings("nls")
public class StatementLambdaToExpressionASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private String blockTemplate = "new ArrayList<>().forEach(element -> %s);";

	@BeforeEach
	public void setUp() throws Exception {
		setVisitor(new StatementLambdaToExpressionASTVisitor());
		fixture.addImport("java.util.ArrayList");
	}

	@Test
	public void visit_simpleExpressionLambda_shouldReplace() throws Exception {
		assertChange(//
				String.format(blockTemplate, "{ new String();}"), //
				String.format(blockTemplate, "new String()"));
	}

	@Test
	public void visit_simpleReturnLambda_shouldReplace() throws Exception {
		assertChange(//
				String.format(blockTemplate, "{ new String(); return; }"),
				String.format(blockTemplate, "new String()"));
	}

	@Test
	public void visit_tooManyStatements_shouldNotReplace() throws Exception {
		assertNoChange(String.format(blockTemplate, "{ new String(); new String(); return; }"));
	}

	@Test
	public void visit_invalidStatement_shouldNotReplace() throws Exception {
		assertNoChange(String.format(blockTemplate, "{ int d = 0; }"));
	}

	@Test
	public void visit_simplePredicate_shouldReplace() throws Exception {
		assertChange(//
				"new ArrayList<>().stream().filter(element -> { return true; });",
				"new ArrayList<>().stream().filter(element -> true);");
	}

	@Test
	public void visit_whenReplacementHappens_ShouldUpdateListeners() throws Exception {
		StatementLambdaToExpressionASTVisitor statementLambdaToExpressionASTVisitor = new StatementLambdaToExpressionASTVisitor();
		ASTRewriteVisitorListenerStub listener = new ASTRewriteVisitorListenerStub();
		statementLambdaToExpressionASTVisitor.addRewriteListener(listener);

		String block = String.format(blockTemplate, "{ new String();}");
		fixture.addMethodBlock(block);
		statementLambdaToExpressionASTVisitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(statementLambdaToExpressionASTVisitor);

		assertTrue(listener.wasUpdated());
	}
}
