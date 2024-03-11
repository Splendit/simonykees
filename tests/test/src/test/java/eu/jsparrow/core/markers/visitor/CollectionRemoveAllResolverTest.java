package eu.jsparrow.core.markers.visitor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class CollectionRemoveAllResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		CollectionRemoveAllResolver visitor = new CollectionRemoveAllResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("CollectionRemoveAllResolver"));
		setVisitor(visitor);
		String original = ""
				+  "List<String> list = new ArrayList<>();\n"
				+ "list.add(\"value\");\n"
				+ "list.removeAll(list);";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		CollectionRemoveAllResolver visitor = new CollectionRemoveAllResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("CollectionRemoveAllResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<String> list = new ArrayList<>();\n"
				+ "list.add(\"value\");\n"
				+ "list.removeAll(list);";
		String expected = ""
				+ "List<String> list = new ArrayList<>();\n"
				+ "list.add(\"value\");\n"
				+ "list.clear();";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Simplifies the code by replacing all occurrences of removeAll() which have the current collection as parameter with clear(). For example, list.removeAll(list) becomes list.clear(). ";
		assertAll(
				() -> assertEquals("Replace removeAll() with clear()", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("CollectionRemoveAllResolver", event.getResolver()),
				() -> assertEquals("list.clear()", event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(214, event.getOffset()),
				() -> assertEquals(20, event.getLength()),
				() -> assertEquals(10, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		CollectionRemoveAllResolver visitor = new CollectionRemoveAllResolver(node -> node.getStartPosition() == 215);
		visitor.addMarkerListener(RefactoringMarkers.getFor("CollectionRemoveAllResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<String> list = new ArrayList<>();\n"
				+ "list.add(\"value\");\n"
				+ "list.removeAll(list);";
		String expected = ""
				+ "List<String> list = new ArrayList<>();\n"
				+ "list.add(\"value\");\n"
				+ "list.clear();";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}