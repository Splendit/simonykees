package eu.jsparrow.core.visitor.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.rules.common.visitor.helper.VariableAssignmentVisitor;

@SuppressWarnings("nls")
public class VariableAssignmentVisitorTest extends UsesJDTUnitFixture {

	private static final String JAVA_UTIL_DATE = java.util.Date.class.getName();

	private VariableAssignmentVisitor visitor;
	private String identifier = "date";

	@Before
	public void setUp() throws Exception {
		fixture.addImport(JAVA_UTIL_DATE);
		ImportDeclaration dateImport = fixture.getImports()
			.get(0);
		AST ast = dateImport.getAST();
		SimpleName name = ast.newSimpleName(identifier);
		visitor = new VariableAssignmentVisitor(name);
	}

	@Test
	public void visit_noReAssignments_shouldReturnEmpty() throws Exception {
		fixture.addMethodBlock("Date " + identifier + " = new Date();");

		fixture.accept(visitor);

		List<Assignment> assignments = visitor.getAssignments();
		assertTrue(assignments.isEmpty());
	}

	@Test
	public void visit_reassignment_shouldReturnNonEmptyList() throws Exception {
		fixture.addImport(java.time.Instant.class.getName());
		fixture.addMethodBlock("Date " + identifier + " = new Date();Instant now = Instant.now(); " + identifier
				+ " = Date.from(now);");

		fixture.accept(visitor);

		List<Assignment> assignments = visitor.getAssignments();
		assertEquals(1, assignments.size());
	}

	@Test
	public void visit_multipleStatementsNoReassignments_shouldReturnEmpty() throws Exception {
		fixture.addImport(java.time.Instant.class.getName());
		fixture.addMethodBlock("Date " + identifier
				+ " = new Date();Instant now = Instant.now(); Instant then = Instant.now(); then = now; ");

		fixture.accept(visitor);

		List<Assignment> assignments = visitor.getAssignments();
		assertTrue(assignments.isEmpty());
	}
	
	@Test
	public void visit_multipleReassignment_shouldReturnNonEmptyList() throws Exception {
		fixture.addImport(java.time.Instant.class.getName());
		fixture.addMethodBlock("Date " + identifier + " = new Date();Instant now = Instant.now(); " + identifier
				+ " = Date.from(now);" + identifier
				+ " = null;" + identifier
				+ " = Date.from(now);");

		fixture.accept(visitor);

		List<Assignment> assignments = visitor.getAssignments();
		assertEquals(3, assignments.size());
	}
}
