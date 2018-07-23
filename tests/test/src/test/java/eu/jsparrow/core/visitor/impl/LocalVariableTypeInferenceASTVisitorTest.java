package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.rules.java10.LocalVariableTypeInferenceASTVisitor;

public class LocalVariableTypeInferenceASTVisitorTest extends UsesJDTUnitFixture {

	private LocalVariableTypeInferenceASTVisitor visitor;
	
	@Before
	public void setUp() {
		visitor = new LocalVariableTypeInferenceASTVisitor();
	}

	@Test
	public void visit_methodWithoutArguments_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");
		fixture.addImport("java.util.HashMap");
		String block = "Map map = new HashMap();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);
		
		fixture.hasChanged();

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}

}
