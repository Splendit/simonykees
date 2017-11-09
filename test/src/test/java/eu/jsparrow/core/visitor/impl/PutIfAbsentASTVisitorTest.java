package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.Before;
import org.junit.Test;

public class PutIfAbsentASTVisitorTest extends AbstractASTVisitorTest {

	@Before
	public void setUp() {
		visitor = new PutIfAbsentASTVisitor();
	}

	@Test
	public void visit_methodWithoutArguments_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");
		String block = "Map map; if (!map.containsKey(1)) { map.put(); }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_wrongMethodName_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");
		String block = "Map map; if (!map.containsKey(1)) { map.PUT(1,2); }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_putNotOnMapType_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.List");
		String block = "List map; if (!map.containsKey(1)) { map.PUT(1,2); }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_ifStatementWithElse_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");
		String block = "Map map; if (!map.containsKey(1)) { map.put(1,2); } else { int i = 0; }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_putSurroundedBlockAndIf_shouldReplace() throws Exception {
		fixture.addImport("java.util.Map");
		String block = "Map map; if (!map.containsKey(1)) { map.put(1, 2); }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock("Map map; map.putIfAbsent(1, 2);");
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_putSurroundedByIf_shouldReplace() throws Exception {
		fixture.addImport("java.util.Map");
		String block = "Map map; if (!map.containsKey(1)) map.put(1, 2);";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock("Map map; map.putIfAbsent(1, 2);");
		assertMatch(expected, fixture.getMethodBlock());
	}

}
