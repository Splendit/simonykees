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

class RemoveDoubleNegationResolverTest extends UsesSimpleJDTUnitFixture {
	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		RemoveDoubleNegationResolver visitor = new RemoveDoubleNegationResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseStringJoinResolver"));
		setVisitor(visitor);
		String original = "" +
				"boolean a = !!true; // 2 times";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		RemoveDoubleNegationResolver visitor = new RemoveDoubleNegationResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveDoubleNegationResolver"));
		setVisitor(visitor);
		String original = "" +
				"boolean a = !!true; // 2 times";
		String expected = "" +
				"boolean a = true; // 2 times";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Removes pairs of negations from boolean expressions until only zero or one negation is left.";
		assertAll(
				() -> assertEquals("Remove Double Negations", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("RemoveDoubleNegationResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(101, event.getOffset()),
				() -> assertEquals(6, event.getLength()),
				() -> assertEquals(6, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		RemoveDoubleNegationResolver visitor = new RemoveDoubleNegationResolver(node -> node.getStartPosition() == 101);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveDoubleNegationResolver"));
		setVisitor(visitor);
		String original = "" +
				"boolean a = !!true; // 2 times";
		String expected = "" +
				"boolean a = true; // 2 times";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
