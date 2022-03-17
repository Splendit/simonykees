package eu.jsparrow.core.visitor.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.core.visitor.arithmetic.ArithmethicAssignmentASTVisitor;
import eu.jsparrow.dummies.ASTRewriteVisitorListenerStub;

class ArithmeticAssignmentASTVisitorTest extends UsesSimpleJDTUnitFixture {

	
	@BeforeEach
	void setUp() {
		setVisitor(new ArithmethicAssignmentASTVisitor());
	}

	@Test
	void visit_AssignmentWithAdd_ShouldReplaceAddAssignment() throws Exception {
		assertChange("int a = 0;  a = a + 3;", "int a = 0;  a += 3;");
	}

	@Test
	void visit_AlreadyAddAssignment_ShouldNotReplace() throws Exception {		
		assertNoChange("int a = 0;  a += 3;");
	}

	@Test
	void visit_AssignmentWithAdd_ShouldUpdateListeners() throws Exception {
		ArithmethicAssignmentASTVisitor arithmethicAssignmentVisitor = new ArithmethicAssignmentASTVisitor();
		ASTRewriteVisitorListenerStub listener = new ASTRewriteVisitorListenerStub();
		arithmethicAssignmentVisitor.addRewriteListener(listener);
		fixture.addMethodBlock("int a = 0;  a = a + 3;");
		arithmethicAssignmentVisitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(arithmethicAssignmentVisitor);

		assertTrue(listener.wasUpdated());
	}
}
