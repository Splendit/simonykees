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

class UseListSortResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.Collections.class.getName());
		fixture.addImport(java.util.Comparator.class.getName());
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		UseListSortResolver visitor = new UseListSortResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseListSortResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<String> values = Collections.emptyList();\n"
				+ "Comparator<String> comparator = Comparator.naturalOrder();\n"
				+ "Collections.sort(values, comparator);";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		UseListSortResolver visitor = new UseListSortResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseListSortResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<String> values = Collections.emptyList();\n"
				+ "Comparator<String> comparator = Comparator.naturalOrder();\n"
				+ "Collections.sort(values, comparator);";
		String expected = ""
				+ "List<String> values = Collections.emptyList();\n"
				+ "Comparator<String> comparator = Comparator.naturalOrder();\n"
				+ "values.sort(comparator);";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Replaces static invocations of Collections.sort(list, comparator) with list.sort(comparator).";
		assertAll(
				() -> assertEquals("Replace Collection.sort with List.sort", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("UseListSortResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(293, event.getOffset()),
				() -> assertEquals(36, event.getLength()),
				() -> assertEquals(11, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		UseListSortResolver visitor = new UseListSortResolver(node -> node.getStartPosition() == 294);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseListSortResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<String> values = Collections.emptyList();\n"
				+ "Comparator<String> comparator = Comparator.naturalOrder();\n"
				+ "Collections.sort(values, comparator);";
		String expected = ""
				+ "List<String> values = Collections.emptyList();\n"
				+ "Comparator<String> comparator = Comparator.naturalOrder();\n"
				+ "values.sort(comparator);";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
