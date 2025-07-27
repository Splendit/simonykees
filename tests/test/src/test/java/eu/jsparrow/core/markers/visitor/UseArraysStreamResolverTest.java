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

class UseArraysStreamResolverTest extends UsesSimpleJDTUnitFixture {


	@BeforeEach
	void setUp() throws Exception {
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.Arrays.class.getName());
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		UseArraysStreamResolver visitor = new UseArraysStreamResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseArraysStreamResolver"));
		setVisitor(visitor);
		String original = "" +
				"String[] values = new String[] {\"1\", \"2\", \"3\"};\n" +
				"Arrays.asList(values).stream();";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		UseArraysStreamResolver visitor = new UseArraysStreamResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseArraysStreamResolver"));
		setVisitor(visitor);
		String original = "" +
				"String[] values = new String[] {\"1\", \"2\", \"3\"};\n" +
				"Arrays.asList(values).stream();";
		String expected = "" +
				"String[] values = new String[] {\"1\", \"2\", \"3\"};\n" +
				"Stream.of(values);";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String message = "Replaces Arrays.asList(..).stream() with Arrays.stream(..) when the boxing of stream elements can be avoided. "
				+ "Otherwise, replaces Arrays.asList.stream(..) with the shorthand method Stream.of(..).";
		assertAll(
				() -> assertEquals("Use Arrays Stream", event.getName()),
				() -> assertEquals(message, event.getMessage()), 
				() -> assertEquals("UseArraysStreamResolver", event.getResolver()),
				() -> assertEquals("Stream.of(values)", event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(192, event.getOffset()),
				() -> assertEquals(30, event.getLength()),
				() -> assertEquals(9, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		UseArraysStreamResolver visitor = new UseArraysStreamResolver(node -> node.getStartPosition() == 193);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseArraysStreamResolver"));
		setVisitor(visitor);
		String original = "" +
				"String[] values = new String[] {\"1\", \"2\", \"3\"};\n" +
				"Arrays.asList(values).stream();";
		String expected = "" +
				"String[] values = new String[] {\"1\", \"2\", \"3\"};\n" +
				"Stream.of(values);";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
	
	@Test
	void test_resolveMarkerWithArraysStream_shouldResolveOne() throws Exception {
		UseArraysStreamResolver visitor = new UseArraysStreamResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseArraysStreamResolver"));
		setVisitor(visitor);
		String original = "" +
				"Arrays.asList(1, 2, 3).stream();";
		String expected = "" +
				"Arrays.stream(new int[] {1, 2, 3});";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		assertEquals("Arrays.stream(new int[]{1,2,3})", event.getCodePreview());
	}
}