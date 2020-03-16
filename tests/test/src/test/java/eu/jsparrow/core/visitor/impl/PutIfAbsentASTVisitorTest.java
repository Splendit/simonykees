package eu.jsparrow.core.visitor.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.dummies.ASTRewriteVisitorListenerStub;

@SuppressWarnings("nls")
public class PutIfAbsentASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		visitor = new PutIfAbsentASTVisitor();
	}

	@Test
	public void visit_methodWithoutArguments_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");

		assertNoChange("Map map; if (!map.containsKey(1)) { map.put(); }");
	}

	@Test
	public void visit_wrongMethodName_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");

		assertNoChange("Map map; if (!map.containsKey(1)) { map.PUT(1,2); }");
	}

	@Test
	public void visit_putNotOnMapType_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.List");

		assertNoChange("List map; if (!map.containsKey(1)) { map.PUT(1,2); }");
	}

	@Test
	public void visit_ifStatementWithElse_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");

		assertNoChange("Map map; if (!map.containsKey(1)) { map.put(1,2); } else { int i = 0; }");
	}

	@Test
	public void visit_putOnElse_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");

		assertNoChange("Map map; if (!map.containsKey(1)) { } else { map.put(1,2); }");
	}

	@Test
	public void visit_multipleLinesInIf_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");

		assertNoChange("Map map; if (!map.containsKey(1)) { map.put(1,2); map.put(1,2); }");
	}

	@Test
	public void visit_nestedBlocks_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");

		assertNoChange("Map map; if (!map.containsKey(1)) { { map.put(1,2); } }");
	}

	@Test
	public void visit_expressionsDoNotMatch_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");

		assertNoChange("Map map; Map map1; if (!map.containsKey(1)) { map1.put(1,2); }");
	}

	@Test
	public void visit_keysDoNotMatch_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");

		assertNoChange("Map map;  if (!map.containsKey(1)) { map.put(2,2); }");
	}

	@Test
	public void visit_missingArgumentsInIfCondition_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");

		assertNoChange("Map map;  if (!map.isEmpty()) { map.put(2,2); }");
	}

	@Test
	public void visit_putSurroundedBlockAndIf_shouldReplace() throws Exception {
		fixture.addImport("java.util.Map");
		assertChange(//
				"Map map; if (!map.containsKey(1)) { map.put(1, 2); }", //
				"Map map; map.putIfAbsent(1, 2);");
	}

	@Test
	public void visit_putSurroundedByIf_shouldReplace() throws Exception {
		fixture.addImport("java.util.Map");
		assertChange(//
				"Map map; if (!map.containsKey(1)) map.put(1, 2);", //
				"Map map; map.putIfAbsent(1, 2);");
	}

	@Test
	public void visit_whenReplacementHappens_ShouldUpdateListeners() throws Exception {
		PutIfAbsentASTVisitor putIfAbsentASTVisitor = new PutIfAbsentASTVisitor();

		ASTRewriteVisitorListenerStub listener = new ASTRewriteVisitorListenerStub();
		putIfAbsentASTVisitor.addRewriteListener(listener);
		fixture.addImport("java.util.Map");
		fixture.addMethodBlock("Map map; if (!map.containsKey(1)) map.put(1, 2);");
		putIfAbsentASTVisitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(putIfAbsentASTVisitor);

		assertTrue(listener.wasUpdated());
	}
}
