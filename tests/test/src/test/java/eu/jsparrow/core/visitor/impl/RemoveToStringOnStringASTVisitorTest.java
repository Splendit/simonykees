package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

@SuppressWarnings("nls")
public class RemoveToStringOnStringASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private RemoveToStringOnStringASTVisitor visitor;

	@BeforeEach
	public void setUp() throws Exception {
		visitor = new RemoveToStringOnStringASTVisitor();
	}
	
	@Test
	public void visit_parenthesisedMethodExpression_shouldTransformButNotUnwrap() throws Exception {
		fixture.addMethodBlock("System.out.println((\"abc\".toString() + System.getProperty(\"line.separator\", \"\\n\")).toString().hashCode());");
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		
		Block expected = ASTNodeBuilder.createBlockFromString("System.out.println((\"abc\" + System.getProperty(\"line.separator\", \"\\n\")).hashCode());");
		assertMatch(expected, fixture.getMethodBlock());
	}
}
