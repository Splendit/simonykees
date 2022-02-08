package eu.jsparrow.core.markers.visitor.optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class OptionalMapResolverTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	void setUpVisitor() throws Exception {
		fixture.addImport(java.util.Optional.class.getName());
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		OptionalMapResolver visitor = new OptionalMapResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("OptionalMapResolver"));
		setVisitor(visitor);
		String original = "" + 
				"Optional<String> optional = Optional.of(\"value\");\n" +
				"optional.ifPresent(value -> {\n" +
				"	String test = value.replace(\"t\", \"o\");\n" +
				"	System.out.print(test);\n" +
				"});";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		OptionalMapResolver visitor = new OptionalMapResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("OptionalMapResolver"));
		setVisitor(visitor);
		String original = "" +
				"Optional<String> optional = Optional.of(\"value\");\n" +
				"optional.ifPresent(value -> {\n" +
				"	String test = value.replace(\"t\", \"o\");\n" +
				"	System.out.print(test);\n" +
				"});";
		String expected = "" +
				"Optional<String> optional = Optional.of(\"value\");\n" +
				"optional\n" +
				"	.map(value -> value.replace(\"t\", \"o\"))\n" +
				"	.ifPresent(test -> System.out.print(test));";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Extracts an Optional::map from the consumer used in Optional::ifPresent. This makes complicated code blocks easier to read and reuse.";
		assertAll(
				() -> assertEquals("Use Optional::map", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("OptionalMapResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(193, event.getOffset()),
				() -> assertEquals(107, event.getLength()),
				() -> assertEquals(8, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		OptionalMapResolver visitor = new OptionalMapResolver(node -> node.getStartPosition() == 194);
		visitor.addMarkerListener(RefactoringMarkers.getFor("OptionalMapResolver"));
		setVisitor(visitor);
		String original = "" +
				"Optional<String> optional = Optional.of(\"value\");\n" +
				"optional.ifPresent(value -> {\n" +
				"	String test = value.replace(\"t\", \"o\");\n" +
				"	System.out.print(test);\n" +
				"});";
		String expected = "" +
				"Optional<String> optional = Optional.of(\"value\");\n" +
				"optional\n" +
				"	.map(value -> value.replace(\"t\", \"o\"))\n" +
				"	.ifPresent(test -> System.out.print(test));";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
