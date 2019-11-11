package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.arithmetic.ArithmethicAssignmentASTVisitor;
import eu.jsparrow.dummies.ASTRewriteVisitorListenerStub;

@SuppressWarnings({ "nls" })
public class ArithmeticAssignmentASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private ArithmethicAssignmentASTVisitor visitor;

	@BeforeEach
	public void setUp() {
		visitor = new ArithmethicAssignmentASTVisitor();
	}

	@Test
	public void visit_AssignmentWithAdd_ShouldReplaceAddAssignment() throws Exception {
		fixture.addMethodBlock("int a;  a = a + 3;");
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock("int a;  a += 3;");
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_AlreadyAddAssignment_ShouldNotReplace() throws Exception {
		fixture.addMethodBlock("int a;  a += 3;");
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}

	@Test
	public void visit__AssignmentWithAdd_ShouldUpdateListeners() throws Exception {
		ASTRewriteVisitorListenerStub listener = new ASTRewriteVisitorListenerStub();
		visitor.addRewriteListener(listener);
		fixture.addMethodBlock("int a;  a = a + 3;");
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertTrue(listener.wasUpdated());
	}
}
