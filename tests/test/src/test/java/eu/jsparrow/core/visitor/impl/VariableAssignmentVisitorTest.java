package eu.jsparrow.core.visitor.impl;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.visitor.helper.VariableAssignmentVisitor;

@SuppressWarnings("nls")
public class VariableAssignmentVisitorTest extends UsesSimpleJDTUnitFixture {

	private static final String JAVA_UTIL_DATE = java.util.Date.class.getName();

	private VariableAssignmentVisitor visitor;
	private String identifier = "date";

	@BeforeEach
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
		fixture.addMethodBlock(String.format("Date %s = new Date();", identifier));

		fixture.accept(visitor);

		List<Assignment> assignments = visitor.getAssignments();
		assertTrue(assignments.isEmpty());
	}

	@Test
	public void visit_reassignment_shouldReturnNonEmptyList() throws Exception {
		fixture.addImport(java.time.Instant.class.getName());
		String block = String.format("Date %s = new Date();Instant now = Instant.now(); %s = Date.from(now);",
				identifier, identifier);
		fixture.addMethodBlock(block);

		fixture.accept(visitor);

		List<Assignment> assignments = visitor.getAssignments();
		assertEquals(1, assignments.size());
	}

	@Test
	public void visit_multipleStatementsNoReassignments_shouldReturnEmpty() throws Exception {
		fixture.addImport(java.time.Instant.class.getName());
		String block = String.format(
				"Date %s = new Date();Instant now = Instant.now(); Instant then = Instant.now(); then = now; ",
				identifier);
		fixture.addMethodBlock(block);

		fixture.accept(visitor);

		List<Assignment> assignments = visitor.getAssignments();
		assertTrue(assignments.isEmpty());
	}

	@Test
	public void visit_multipleReassignment_shouldReturnNonEmptyList() throws Exception {
		fixture.addImport(java.time.Instant.class.getName());
		String block = String.format(
				"Date %s = new Date();Instant now = Instant.now(); %s = Date.from(now); %s = null; %s = Date.from(now);",
				identifier, identifier, identifier, identifier);
		fixture.addMethodBlock(block);

		fixture.accept(visitor);

		List<Assignment> assignments = visitor.getAssignments();
		assertEquals(3, assignments.size());
	}
}
