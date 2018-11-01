package eu.jsparrow.core.visitor.impl;

import org.junit.Before;

public class RemoveExplicitCallToSuperASTVisitorTest extends UsesJDTUnitFixture {

	RemoveExplicitCallToSuperASTVisitor visitor;

	@Before
	public void setUp() {
		visitor = new RemoveExplicitCallToSuperASTVisitor();
	}

}
