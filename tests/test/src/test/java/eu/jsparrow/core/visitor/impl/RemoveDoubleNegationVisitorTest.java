package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class RemoveDoubleNegationVisitorTest extends UsesJDTUnitFixture {

	private RemoveDoubleNegationASTVisitor visitor;

	@Before
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

}
