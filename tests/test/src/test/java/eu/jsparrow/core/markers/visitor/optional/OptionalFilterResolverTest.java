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

class OptionalFilterResolverTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	void setUpVisitor() throws Exception {
		fixture.addImport(java.util.Optional.class.getName());
		RefactoringMarkers.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		OptionalFilterResolver visitor = new OptionalFilterResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("OptionalFilterResolver"));
		setVisitor(visitor);
		String original = "" + 
				"Optional<String> optional = Optional.empty();\n" +
				"optional.ifPresent(value -> {\n" +
				"	if(!value.isEmpty()) {\n" +
				"		System.out.println(value);\n" +
				"	}\n" +
				"});";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		OptionalFilterResolver visitor = new OptionalFilterResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("OptionalFilterResolver"));
		setVisitor(visitor);
		String original = "" +
				"Optional<String> optional = Optional.empty();\n" +
				"optional.ifPresent(value -> {\n" +
				"	if(!value.isEmpty()) {\n" +
				"		System.out.println(value);\n" +
				"	}\n" +
				"});";
		String expected = "" +
				"Optional<String> optional = Optional.empty();\n" +
				"optional.filter(value -> !value.isEmpty()).ifPresent(value -> {\n" +
				"	System.out.println(value);\n" +
				"});";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Extracts an Optional::filter from the consumer used in Optional::ifPresent. Hence, simplifying the "
				+ "lambda expressions used with Optional operations. This transformation is feasible when the entire "
				+ "consumer's body is wrapped into an if-statement.";
		assertAll(
				() -> assertEquals("Use Optional::filter", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("OptionalFilterResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(189, event.getOffset()),
				() -> assertEquals(113, event.getLength()),
				() -> assertEquals(8, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		OptionalFilterResolver visitor = new OptionalFilterResolver(node -> node.getStartPosition() == 190);
		visitor.addMarkerListener(RefactoringMarkers.getFor("OptionalFilterResolver"));
		setVisitor(visitor);
		String original = "" +
				"Optional<String> optional = Optional.empty();\n" +
				"optional.ifPresent(value -> {\n" +
				"	if(!value.isEmpty()) {\n" +
				"		System.out.println(value);\n" +
				"	}\n" +
				"});";
		String expected = "" +
				"Optional<String> optional = Optional.empty();\n" +
				"optional.filter(value -> !value.isEmpty()).ifPresent(value -> {\n" +
				"	System.out.println(value);\n" +
				"});";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
