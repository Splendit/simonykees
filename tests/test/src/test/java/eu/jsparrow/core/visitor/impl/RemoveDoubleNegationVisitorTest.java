package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
public class RemoveDoubleNegationVisitorTest extends UsesJDTUnitFixture {

	private RemoveDoubleNegationASTVisitor visitor;

	@BeforeEach
	public void setUp() throws Exception {
		visitor = new RemoveDoubleNegationASTVisitor();
	}
	
	@Test
	public void visit_zeroNegation() throws Exception {
		fixture.addMethodBlock("boolean a = true;");
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expected = createBlock("boolean a = true;");
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_singleNegation() throws Exception {
		fixture.addMethodBlock("boolean a = !true;");
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expected = createBlock("boolean a = !true;");
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_doubleNegation() throws Exception {
		fixture.addMethodBlock("boolean a = !!true;");
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expected = createBlock("boolean a = true;");
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_trippeNegation() throws Exception {
		fixture.addMethodBlock("boolean a = !!!true;");
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expected = createBlock("boolean a = !true;");
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_4timesNegation() throws Exception {
		fixture.addMethodBlock("boolean a = !!!!true;");
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expected = createBlock("boolean a = true;");
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_5timesNegation() throws Exception {
		fixture.addMethodBlock("boolean a = !!!!!true;");
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expected = createBlock("boolean a = !true;");
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_numericExpressionPrefix() throws Exception {
		fixture.addMethodBlock("int i = 0; boolean a = ++i == 0;");
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expected = createBlock("int i = 0; boolean a = ++i == 0;");
		assertMatch(expected, fixture.getMethodBlock());
	}

}
