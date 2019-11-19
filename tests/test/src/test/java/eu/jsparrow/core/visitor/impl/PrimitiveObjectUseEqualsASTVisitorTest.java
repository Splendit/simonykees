package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.dummies.ASTRewriteVisitorListenerStub;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

@SuppressWarnings({ "nls" })
public class PrimitiveObjectUseEqualsASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private PrimitiveObjectUseEqualsASTVisitor visitor;
	
	private String template = "Integer a = new Integer(1);Integer b = new Integer(2);if (%s) {}";

	@BeforeEach
	public void setUp() {
		visitor = new PrimitiveObjectUseEqualsASTVisitor();
	}

	@Test
	public void visit_onEqualsInfix_ShouldReplaceWithEquals() throws Exception {
		fixture.addMethodBlock(String.format(template, "a == b"));
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString(String.format(template, "a.equals(b)"));
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_onNotEqualsInfix_ShouldReplaceWithPrefixAndEquals() throws Exception {
		fixture.addMethodBlock(String.format(template, "a != b"));
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString(String.format(template, "!a.equals(b)"));
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_onNewInteger_ShouldReplaceWithEquals() throws Exception {
		fixture.addMethodBlock(String.format(template, "new Integer(1) == new Integer(2)"));
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString(String.format(template, "new Integer(1).equals(new Integer(2))"));
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_onString_ShouldReplaceWithEquals() throws Exception {
		fixture.addMethodBlock(String.format(template, "\"String1\" == \"String2\""));
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString(String.format(template, "\"String1\".equals(\"String2\")"));
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_onOtherInfix_ShouldNotReplaceWithEquals() throws Exception {
		String statement = String.format(template, "a >= b");
		fixture.addMethodBlock(statement);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}

	@Test
	public void visit_withExtendedOperands_ShouldNotReplace() throws Exception {
		String statement = String.format(template, "a == b == new Integer(1) == new Integer(2)");
		fixture.addMethodBlock(statement);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}

	@Test
	public void visit_onLiteralInt_ShouldNotReplace() throws Exception {
		String statement = String.format(template, "a == 1");
		fixture.addMethodBlock(statement);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}

	@Test
	public void visit_onLiteralIntSwitched_ShouldNotReplace() throws Exception {
		String statement = String.format(template, "1 == a");
		fixture.addMethodBlock(statement);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}

	@Test
	public void visit_onLiteralChar_ShouldNotReplace() throws Exception {
		String statement = String.format(template, "a == 'c'");
		fixture.addMethodBlock(statement);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}

	@Test
	public void visit_onLiteralBool_ShouldNotReplace() throws Exception {
		String statement = String.format(template, "a == true");
		fixture.addMethodBlock(statement);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}

	// Reproduces SIM-824
	@Test
	public void visit_infixWithTypecastOnInteger_ShouldReplaceWithEqualsAndInfix() throws Exception {
		fixture.addMethodBlock(String.format(template, "(Integer)a == b"));
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString(String.format(template, "((Integer)a).equals(b)"));
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_infixWithTypecastOnRightInteger_ShouldReplaceWithEquals() throws Exception {
		fixture.addMethodBlock(String.format(template, "a == (Integer)b"));
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString(String.format(template, "a.equals((Integer)b)"));
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_infixWithTypecastOnRightInteger_ShouldUpdateListeners() throws Exception {
		ASTRewriteVisitorListenerStub listener = new ASTRewriteVisitorListenerStub();
		visitor.addRewriteListener(listener);
		fixture.addMethodBlock(String.format(template, "a == (Integer)b"));
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertTrue(listener.wasUpdated());
	}

}
