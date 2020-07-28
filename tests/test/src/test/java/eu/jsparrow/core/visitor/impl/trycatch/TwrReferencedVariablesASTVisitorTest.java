package eu.jsparrow.core.visitor.impl.trycatch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

public class TwrReferencedVariablesASTVisitorTest extends UsesSimpleJDTUnitFixture {
	
	private ReferencedVariablesASTVisitor visitor;
	
	@BeforeEach
	public void setUp() {
		visitor = new ReferencedVariablesASTVisitor();
	}

	@Test
	public void visit_AssignmentWithAdd_ShouldReplaceAddAssignment() throws Exception {
		fixture.addMethodBlock("int a,b; someMethod(a); someMethod(b);"); //$NON-NLS-1$

		fixture.accept(visitor);

		assertThat(visitor.getReferencedVariables(), hasSize(4));
	}
	
}
