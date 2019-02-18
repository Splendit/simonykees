package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;
import static org.junit.Assert.assertFalse;

import org.eclipse.jdt.core.dom.Block;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings({ "nls" })
public class CollapseIfStatementsASTVisitorTest extends UsesJDTUnitFixture {

	private CollapseIfStatementsASTVisitor visitor;
	
	
	@Before
	public void setUp() {
		this.visitor = new CollapseIfStatementsASTVisitor();
	}
	
	@Test
	public void visit_simpleNestedIf_shouldTransform() throws Exception {
		String block = ""
				+ "		boolean condition = true;\n" + 
				"		boolean innerCondition = true;\n" + 
				"		if(condition) {\n" + 
				"			if(innerCondition) {\n" + 
				"				condition = false;\n" + 
				"				innerCondition = false;\n" + 
				"			}\n" + 
				"		}";
		String expectedBlock = ""
				+ "		boolean condition = true;\n" + 
				"		boolean innerCondition = true;\n" + 
				"		if(condition && innerCondition) {\n" + 
				"			condition = false;\n" + 
				"			innerCondition = false;\n" + 
				"		}";
		
		
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(expectedBlock);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_tripleCollapse_shouldTransform() throws Exception {
		String block = ""
				+ "		boolean condition = true;\n" + 
				"		boolean innerCondition = true;\n" + 
				"		if(condition) {\n" + 
				"			if(innerCondition) {\n" + 
				"				if(true) {					\n" + 
				"					condition = false;\n" + 
				"					innerCondition = false;\n" + 
				"				}\n" + 
				"			}\n" + 
				"		}";
		String expectedBlock = ""
				+ "		boolean condition = true;\n" + 
				"		boolean innerCondition = true;\n" + 
				"		if(condition && innerCondition && true) {\n" + 
				"			condition = false;\n" + 
				"			innerCondition = false;\n" + 
				"		}";
		
		
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(expectedBlock);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_compoundConditions_shouldTransform() throws Exception {
		String block = ""
				+ "		boolean condition = true;\n" + 
				"		boolean innerCondition = true;\n" + 
				"		if(condition) {\n" + 
				"			if(innerCondition || true) {					\n" + 
				"					condition = false;\n" + 
				"					innerCondition = false;\n" + 
				"			}\n" + 
				"		}";
		String expectedBlock = ""
				+ "		boolean condition = true;\n" + 
				"		boolean innerCondition = true;\n" + 
				"		if(condition && (innerCondition || true)) {\n" + 
				"			condition = false;\n" + 
				"			innerCondition = false;\n" + 
				"		}";
		
		
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(expectedBlock);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_nestedIfThenElse_shouldNotTransform() throws Exception {
		String block = ""
				+ "		boolean condition = true;\n" + 
				"		boolean innerCondition = true;\n" + 
				"		if(condition) {\n" + 
				"			if(innerCondition) {\n" + 
				"				condition = false;\n" + 
				"				innerCondition = false;\n" + 
				"			} else {} \n" + 
				"		}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}
	
	@Test
	public void visit_wrapperIfThenElse_shouldNotTransform() throws Exception {
		String block = ""
				+ "		boolean condition = true;\n" + 
				"		boolean innerCondition = true;\n" + 
				"		if(condition) {\n" + 
				"			if(innerCondition) {\n" + 
				"				condition = false;\n" + 
				"				innerCondition = false;\n" + 
				"			} \n" + 
				"		}  else {} ";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}
	
	@Test
	public void visit_multipleStatements_shouldNotTransform() throws Exception {
		String block = ""
				+ "		boolean condition = true;\n" + 
				"		boolean innerCondition = true;\n" + 
				"		if(condition) {\n" + 
				"			if(innerCondition) {\n" + 
				"				condition = false;\n" + 
				"				innerCondition = false;\n" + 
				"			} " + 
				"			int i = 0;\n" + 
				"		}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}
	
	@Test
	public void visit_ifWithEmptyBody_shouldNotTransform() throws Exception {
		String block = ""
				+ "		boolean condition = true;\n" + 
				"		boolean innerCondition = true;\n" + 
				"		if(condition) {\n" + 
				"			" + 
				"		}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}
	
	@Test
	public void visit_ifWithMissingNestedIf_shouldNotTransform() throws Exception {
		String block = ""
				+ "		boolean condition = true;\n" + 
				"		boolean innerCondition = true;\n" + 
				"		if(condition) {\n" + 
				"			int i = 0;" + 
				"		}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}
	
	public void samples() {

		boolean condition = true;
		boolean innerCondition = true;
		// before
		if(condition) {
			if(innerCondition || true) {					
					condition = false;
					innerCondition = false;
			}
		}
		
		// before
		if(condition && (innerCondition || true)) {
			condition = false;
			innerCondition = false;
		}
	}
	
}
