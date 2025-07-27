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

class UseStringJoinResolverTest extends UsesSimpleJDTUnitFixture {


	@BeforeEach
	void setUp() throws Exception {
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
		fixture.addImport(java.util.stream.Collectors.class.getName());
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		UseStringJoinResolver visitor = new UseStringJoinResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseStringJoinResolver"));
		setVisitor(visitor);
		String original = "" +
				"List<String> values = new ArrayList<>();\n" +
				"values.stream().collect(Collectors.joining(\",\"));";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		UseStringJoinResolver visitor = new UseStringJoinResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseStringJoinResolver"));
		setVisitor(visitor);
		String original = "" +
				"List<String> values = new ArrayList<>();\n" +
				"values.stream().collect(Collectors.joining(\",\"));";
		String expected = "" +
				"List<String> values = new ArrayList<>();\n" +
				"String.join(\",\", values);";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		assertAll(
				() -> assertEquals("Use String Join", event.getName()),
				() -> assertEquals("Replaces stream Collectors that are used for concatenating values of a collection with StringJoiners.", event.getMessage()), 
				() -> assertEquals("UseStringJoinResolver", event.getResolver()),
				() -> assertEquals("String.join(\",\",values)", event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(225, event.getOffset()),
				() -> assertEquals(48, event.getLength()),
				() -> assertEquals(10, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		UseStringJoinResolver visitor = new UseStringJoinResolver(node -> node.getStartPosition() == 226);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseStringJoinResolver"));
		setVisitor(visitor);
		String original = "" +
				"List<String> values = new ArrayList<>();\n" +
				"values.stream().collect(Collectors.joining(\",\"));";
		String expected = "" +
				"List<String> values = new ArrayList<>();\n" +
				"String.join(\",\", values);";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
