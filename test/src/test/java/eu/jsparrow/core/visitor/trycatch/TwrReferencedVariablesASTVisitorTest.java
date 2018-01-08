package eu.jsparrow.core.visitor.trycatch;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

public class TwrReferencedVariablesASTVisitorTest extends UsesJDTUnitFixture {
	
	private TwrReferencedVariablesASTVisitor visitor;
	
	@Before
	public void setUp() {
		visitor = new TwrReferencedVariablesASTVisitor();
	}

	@Test
	public void visit_AssignmentWithAdd_ShouldReplaceAddAssignment() throws Exception {
		fixture.addMethodBlock("int a,b; someMethod(a); someMethod(b);");

		fixture.accept(visitor);

		assertThat(visitor.getReferencedVariables(), hasSize(4));
	}
	
}
