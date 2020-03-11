package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.dummies.ASTRewriteVisitorListenerStub;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

@SuppressWarnings("nls")
public class PutIfAbsentASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private PutIfAbsentASTVisitor visitor;
	
	@BeforeEach
	public void setUp() {
		visitor = new PutIfAbsentASTVisitor();
	}

	@Test
	public void visit_methodWithoutArguments_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");
		// Compilation error because:
		//  - variable not initialized
		//  - map#put(); is not defined
		String block = "Map map; if (!map.containsKey(1)) { map.put(); }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_wrongMethodName_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");
		// Compilation error because:
		//  - variable not initialized
		//  - Map#PUT(Object, Object); is not defined		
		String block = "Map map; if (!map.containsKey(1)) { map.PUT(1,2); }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_putNotOnMapType_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.List");
		String block = "List map; if (!map.containsKey(1)) { map.PUT(1,2); }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_ifStatementWithElse_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");
		String block = "Map map; if (!map.containsKey(1)) { map.put(1,2); } else { int i = 0; }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_putOnElse_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");
		String block = "Map map; if (!map.containsKey(1)) { } else { map.put(1,2); }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_multipleLinesInIf_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");
		String block = "Map map; if (!map.containsKey(1)) { map.put(1,2); map.put(1,2); }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_nestedBlocks_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");
		String block = "Map map; if (!map.containsKey(1)) { { map.put(1,2); } }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_expressionsDoNotMatch_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");
		String block = "Map map; Map map1; if (!map.containsKey(1)) { map1.put(1,2); }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_keysDoNotMatch_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");
		String block = "Map map;  if (!map.containsKey(1)) { map.put(2,2); }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	
	@Test
	public void visit_missingArgumentsInIfCondition_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");
		String block = "Map map;  if (!map.isEmpty()) { map.put(2,2); }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	
	@Test
	public void visit_putSurroundedBlockAndIf_shouldReplace() throws Exception {
		fixture.addImport("java.util.Map");
		String block = "Map map; if (!map.containsKey(1)) { map.put(1, 2); }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString("Map map; map.putIfAbsent(1, 2);");
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_putSurroundedByIf_shouldReplace() throws Exception {
		fixture.addImport("java.util.Map");
		String block = "Map map; if (!map.containsKey(1)) map.put(1, 2);";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString("Map map; map.putIfAbsent(1, 2);");
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_whenReplacementHappens_ShouldUpdateListeners() throws Exception {
		ASTRewriteVisitorListenerStub listener = new ASTRewriteVisitorListenerStub();
		visitor.addRewriteListener(listener);
		fixture.addImport("java.util.Map");
		fixture.addMethodBlock("Map map; if (!map.containsKey(1)) map.put(1, 2);");
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertTrue(listener.wasUpdated());
	}

}
