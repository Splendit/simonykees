package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;
import static org.junit.Assert.assertFalse;

import org.eclipse.jdt.core.dom.Block;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.core.visitor.arithmetic.ArithmethicAssignmentASTVisitor;

@SuppressWarnings({ "nls" })
public class ArithmeticAssignmentASTVisitorTest extends AbstractASTVisitorTest {

	@Before
	public void setUp() {
		visitor = new ArithmethicAssignmentASTVisitor();
	}

	@Test
	public void visit_AssignmentWithAdd_ShouldReplaceAddAssignment() throws Exception {
		fixture.addMethodBlock("int a;  a = a + 3;");
		visitor.setAstRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock("int a;  a += 3;");
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_AlreadyAddAssignment_ShouldNotReplace() throws Exception {
		fixture.addMethodBlock("int a;  a += 3;");
		visitor.setAstRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}
}
