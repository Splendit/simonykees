package eu.jsparrow.core.visitor;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;
import static org.junit.Assert.assertFalse;

import org.eclipse.jdt.core.dom.Block;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings({ "nls" })
public class PrimitiveObjectUseEqualsASTVisitorTest extends AbstractASTVisitorTest {

	private String template = "Integer a = new Integer(1);Integer b = new Integer(2);if (%s) {}";

	@Before
	public void setUp() {
		visitor = new PrimitiveObjectUseEqualsASTVisitor();
	}

	@Test
	public void visit_onEqualsInfix_ShouldReplaceWithEquals() throws Exception {
		fixture.addMethodBlock(String.format(template, "a == b"));
		visitor.setAstRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(String.format(template, "a.equals(b)"));
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_onNotEqualsInfix_ShouldReplaceWithPrefixAndEquals() throws Exception {
		fixture.addMethodBlock(String.format(template, "a != b"));
		visitor.setAstRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(String.format(template, "!a.equals(b)"));
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_onNewInteger_ShouldReplaceWithEquals() throws Exception {
		fixture.addMethodBlock(String.format(template, "new Integer(1) == new Integer(2)"));
		visitor.setAstRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(String.format(template, "new Integer(1).equals(new Integer(2))"));
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_onSTring_ShouldReplaceWithEquals() throws Exception {
		fixture.addMethodBlock(String.format(template, "\"String1\" == \"String2\""));
		visitor.setAstRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(String.format(template, "\"String1\".equals(\"String2\")"));
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_onOtherInfix_ShouldNotReplaceWithEquals() throws Exception {
		String statement = String.format(template, "a >= b");
		fixture.addMethodBlock(statement);
		visitor.setAstRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}
	
	@Test
	public void visit_withExtendedOperands_ShouldNotReplace() throws Exception {
		String statement = String.format(template, "a == b == new Integer(1) == new Integer(2)");
		fixture.addMethodBlock(statement);
		visitor.setAstRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}
	
	@Test
	public void visit_onLiteralInt_ShouldNotReplace() throws Exception {
		String statement = String.format(template, "a == 1");
		fixture.addMethodBlock(statement);
		visitor.setAstRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}
	
	@Test
	public void visit_onLiteralIntSwitched_ShouldNotReplace() throws Exception {
		String statement = String.format(template, "1 == a");
		fixture.addMethodBlock(statement);
		visitor.setAstRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}
	
	@Test
	public void visit_onLiteralChar_ShouldNotReplace() throws Exception {
		String statement = String.format(template, "a == 'c'");
		fixture.addMethodBlock(statement);
		visitor.setAstRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}
	
	@Test
	public void visit_onLiteralBool_ShouldNotReplace() throws Exception {
		String statement = String.format(template, "a == true");
		fixture.addMethodBlock(statement);
		visitor.setAstRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}

	// Reproduces SIM-824
	@Test
	public void visit_infixWithTypecastOnInteger_ShouldReplaceWithEquals() throws Exception {
		fixture.addMethodBlock(String.format(template, "(Integer)a == b"));
		visitor.setAstRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(String.format(template, "((Integer)a).equals(b)"));
		assertMatch(expected, fixture.getMethodBlock());
	}
}
