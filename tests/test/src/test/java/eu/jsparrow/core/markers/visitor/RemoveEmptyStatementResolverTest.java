package eu.jsparrow.core.markers.visitor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class RemoveEmptyStatementResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		RemoveEmptyStatementResolver visitor = new RemoveEmptyStatementResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveEmptyStatementResolver"));
		setVisitor(visitor);
		String original = ""
				+ ";\n"
				+ "int a = 0;";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		RemoveEmptyStatementResolver visitor = new RemoveEmptyStatementResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveEmptyStatementResolver"));
		setVisitor(visitor);
		String original = ""
				+ ";\n"
				+ "int a = 0;";
		String expected = ""
				+ "int a = 0;";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Finds the unnecessary semicolons in code blocks and removes them.";
		assertAll(
				() -> assertEquals("Remove Unnecessary Semicolons", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("RemoveEmptyStatementResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(89, event.getOffset()),
				() -> assertEquals(1, event.getLength()),
				() -> assertEquals(6, event.getLineNumber()),
				() -> assertEquals(1, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		RemoveEmptyStatementResolver visitor = new RemoveEmptyStatementResolver(node -> node.getStartPosition() == 89);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveEmptyStatementResolver"));
		setVisitor(visitor);
		String original = ""
				+ ";\n"
				+ "int a = 0;";
		String expected = ""
				+ "int a = 0;";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
