package eu.jsparrow.core.markers.visitor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class CollapseIfStatementsResolverTest extends UsesJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		RefactoringMarkers.clear();
	}
	
	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		CollapseIfStatementsResolver visitor = new CollapseIfStatementsResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("CollapseIfStatementsResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"public void foo(boolean a, boolean b) {\n"
				+ "    if (a) {\n"
				+ "        if (b) {\n"
				+ "            System.out.println(\"Ok\");\n"
				+ "        }\n"
				+ "    }\n"
				+ "}\n" 
				+ "";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		CollapseIfStatementsResolver visitor = new CollapseIfStatementsResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("CollapseIfStatementsResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"public void foo(boolean a, boolean b) {\n"
				+ "    if (a) {\n"
				+ "        if (b) {\n"
				+ "            System.out.println(\"Ok\");\n"
				+ "        }\n"
				+ "    }\n"
				+ "}\n" 
				+ "";
		String expected = ""
				+ "public void foo(boolean a, boolean b) {\n"
				+ "    if (a && b) {\n"
				+ "	    System.out.println(\"Ok\");\n"
				+ "	}\n"
				+ "}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Collapses nested if-statements into a single one when possible. "
				+ "Introduces a boolean variable to store the condition if it consists "
				+ "of a conjunction of more than two expressions.";
		
		assertAll(
				() -> assertEquals("Collapse If Statements", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("CollapseIfStatementsResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(93, event.getOffset()),
				() -> assertEquals(95, event.getLength()),
				() -> assertEquals(6, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		CollapseIfStatementsResolver visitor = new CollapseIfStatementsResolver(node -> node.getStartPosition() == 93);
		visitor.addMarkerListener(RefactoringMarkers.getFor("CollapseIfStatementsResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"public void foo(boolean a, boolean b) {\n"
				+ "    if (a) {\n"
				+ "        if (b) {\n"
				+ "            System.out.println(\"Ok\");\n"
				+ "        }\n"
				+ "    }\n"
				+ "}\n" 
				+ "";
		String expected = ""
				+ "public void foo(boolean a, boolean b) {\n"
				+ "    if (a && b) {\n"
				+ "	    System.out.println(\"Ok\");\n"
				+ "	}\n"
				+ "}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
