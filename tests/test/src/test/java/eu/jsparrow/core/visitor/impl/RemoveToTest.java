package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class RemoveToTest extends UsesJDTUnitFixture {

	private RemoveToStringOnStringASTVisitor visitor;

	@Before
	public void setUp() throws Exception {
		visitor = new RemoveToStringOnStringASTVisitor();
	}
	
	@Test
	public void visit_zeroNegation() throws Exception {
		fixture.addMethodBlock("System.out.println((\"abc\".toString() + System.getProperty(\"line.separator\", \"\\n\")).toString().hashCode());");
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		
		Block expected = createBlock("System.out.println((\"abc\" + System.getProperty(\"line.separator\", \"\\n\")).hashCode());");
		assertMatch(expected, fixture.getMethodBlock());
	}
}
