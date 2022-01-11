package eu.jsparrow.core.markers.visitor.stream.tolist;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class ReplaceStreamCollectByToListResolverTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	public void beforeEach() throws Exception {
		RefactoringMarkers.clear();
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.stream.Stream.class.getName());
		fixture.addImport(java.util.stream.Collectors.class.getName());
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		ReplaceStreamCollectByToListResolver visitor = new ReplaceStreamCollectByToListResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceStreamCollectByToListResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<String> values = Stream.of(\"a\", \"b\", \"c\")\n"
				+ "		.filter(value -> !value.isEmpty())\n"
				+ "		.collect(Collectors.toUnmodifiableList());";
		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		ReplaceStreamCollectByToListResolver visitor = new ReplaceStreamCollectByToListResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceStreamCollectByToListResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<String> values = Stream.of(\"a\", \"b\", \"c\")\n"
				+ "		.filter(value -> !value.isEmpty())\n"
				+ "		.collect(Collectors.toUnmodifiableList());";

		String expected = ""
				+ "List<String> values = Stream.of(\"a\", \"b\", \"c\")\n"
				+ "		.filter(value -> !value.isEmpty()).toList();";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Java 16 introduced 'Stream.toList()' as a shorthand method for converting a Stream into an "
				+ "unmodifiable List. This rule replaces invocations of 'collect(Collectors.toUnmodifiableList())' by the new method 'toList()'. \n"
				+ "In case 'Collectors.toList()' is used as a collector, the rule makes additional verifications whether the generated "
				+ "list is modified by the context or not. In the latter case invocations of 'collect(Collectors.toList())' "
				+ "are also replaced by invocations of the simpler method 'toList()'.";
		assertAll(
				() -> assertEquals("Replace Stream.collect() by Stream.toList()", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("ReplaceStreamCollectByToListResolver", event.getResolver()),
				() -> assertEquals("toList()", event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(278, event.getOffset()),
				() -> assertEquals(39, event.getLength()),
				() -> assertEquals(10, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		ReplaceStreamCollectByToListResolver visitor = new ReplaceStreamCollectByToListResolver(node -> node.getStartPosition() == 279);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceStreamCollectByToListResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<String> values = Stream.of(\"a\", \"b\", \"c\")\n"
				+ "		.filter(value -> !value.isEmpty())\n"
				+ "		.collect(Collectors.toUnmodifiableList());";

		String expected = ""
				+ "List<String> values = Stream.of(\"a\", \"b\", \"c\")\n"
				+ "		.filter(value -> !value.isEmpty()).toList();";


		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
