package eu.jsparrow.core.visitor.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.dummies.ASTRewriteVisitorListenerStub;

class PrimitiveObjectUseEqualsASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private String template = "Integer a = new Integer(1);Integer b = new Integer(2);if (%s) {}";

	@BeforeEach
	void setUp() {
		setVisitor(new PrimitiveObjectUseEqualsASTVisitor());
	}

	@Test
	void visit_onEqualsInfix_ShouldReplaceWithEquals() throws Exception {
		assertChange(//
				String.format(template, "a == b"), //
				String.format(template, "a.equals(b)"));
	}

	@Test
	void visit_onNotEqualsInfix_ShouldReplaceWithPrefixAndEquals() throws Exception {
		assertChange(//
				String.format(template, "a != b"), //
				String.format(template, "!a.equals(b)"));
	}

	@Test
	void visit_onNewInteger_ShouldReplaceWithEquals() throws Exception {
		assertChange(//
				String.format(template, "new Integer(1) == new Integer(2)"), //
				String.format(template, "new Integer(1).equals(new Integer(2))"));
	}

	@Test
	void visit_onString_ShouldReplaceWithEquals() throws Exception {
		assertChange(//
				String.format(template, "\"String1\" == \"String2\""), //
				String.format(template, "\"String1\".equals(\"String2\")"));
	}

	@Test
	void visit_onOtherInfix_ShouldNotReplaceWithEquals() throws Exception {
		assertNoChange(String.format(template, "a >= b"));
	}

	@Test
	void visit_withExtendedOperands_ShouldReplace() throws Exception {
		
		assertChange(//
				String.format(template, "a == b == Boolean.TRUE"), //
				String.format(template, "a.equals(b) == Boolean.TRUE"));
	}

	@Test
	void visit_onLiteralInt_ShouldNotReplace() throws Exception {
		assertNoChange(String.format(template, "a == 1"));
	}

	@Test
	void visit_onLiteralIntSwitched_ShouldNotReplace() throws Exception {
		assertNoChange(String.format(template, "1 == a"));
	}

	@Test
	void visit_onLiteralChar_ShouldNotReplace() throws Exception {
		assertNoChange(String.format(template, "a == 'c'"));
	}

	@Test
	void visit_onLiteralBool_ShouldNotReplace() throws Exception {
		assertNoChange(String.format(template, "a == true"));
	}

	// Reproduces SIM-824
	@Test
	void visit_infixWithTypecastOnInteger_ShouldReplaceWithEqualsAndInfix() throws Exception {
		assertChange(//
				String.format(template, "(Integer)a == b"), //
				String.format(template, "((Integer)a).equals(b)"));
	}

	@Test
	void visit_infixWithTypecastOnRightInteger_ShouldReplaceWithEquals() throws Exception {
		assertChange(//
				String.format(template, "a == (Integer)b"), //
				String.format(template, "a.equals((Integer)b)"));
	}

	@Test
	void visit_infixWithTypecastOnRightInteger_ShouldUpdateListeners() throws Exception {
		PrimitiveObjectUseEqualsASTVisitor primitiveObjectUseEqualsASTVisitor = new PrimitiveObjectUseEqualsASTVisitor();
		ASTRewriteVisitorListenerStub listener = new ASTRewriteVisitorListenerStub();

		primitiveObjectUseEqualsASTVisitor.addRewriteListener(listener);
		fixture.addMethodBlock(String.format(template, "a == (Integer)b"));
		primitiveObjectUseEqualsASTVisitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(primitiveObjectUseEqualsASTVisitor);

		assertTrue(listener.wasUpdated());
	}

}
