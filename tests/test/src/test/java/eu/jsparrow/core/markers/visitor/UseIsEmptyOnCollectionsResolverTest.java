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

class UseIsEmptyOnCollectionsResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		UseIsEmptyOnCollectionsResolver visitor = new UseIsEmptyOnCollectionsResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseIsEmptyOnCollectionsResolver"));
		setVisitor(visitor);
		String original = ""
				+ "String s = \"\";\n"
				+ "if (s.length() == 0) {}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		UseIsEmptyOnCollectionsResolver visitor = new UseIsEmptyOnCollectionsResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseIsEmptyOnCollectionsResolver"));
		setVisitor(visitor);
		String original = ""
				+ "String s = \"\";\n"
				+ "if (s.length() == 0) {}";
		String expected = ""
				+ "String s = \"\";\n"
				+ "if (s.isEmpty()) {}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		assertAll(
				() -> assertEquals("Replace Equality Check with isEmpty()", event.getName()),
				() -> assertEquals("Use isEmpty() on Strings, Maps, and Collections.", event.getMessage()), 
				() -> assertEquals("UseIsEmptyOnCollectionsResolver", event.getResolver()),
				() -> assertEquals("if (s.isEmpty()) {\n}\n", event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(116, event.getOffset()),
				() -> assertEquals(15, event.getLength()),
				() -> assertEquals(7, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		UseIsEmptyOnCollectionsResolver visitor = new UseIsEmptyOnCollectionsResolver(node -> node.getStartPosition() == 116);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseIsEmptyOnCollectionsResolver"));
		setVisitor(visitor);
		String original = ""
				+ "String s = \"\";\n"
				+ "if (s.length() == 0) {}"
				+ "if (s.length() == 0) {}";
		String expected = ""
				+ "String s = \"\";\n"
				+ "if (s.isEmpty()) {}"
				+ "if (s.length() == 0) {}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
