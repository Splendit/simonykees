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

class OptionalIfPresentResolverTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	void setUpVisitor() throws Exception {
		fixture.addImport(java.util.Optional.class.getName());
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		OptionalIfPresentResolver visitor = new OptionalIfPresentResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("OptionalIfPresentResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Optional<String> input = Optional.empty();\n"
				+ "if (input.isPresent()) {\n"
				+ "	String value = input.get();\n"
				+ "	System.out.println(value);\n"
				+ "}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		OptionalIfPresentResolver visitor = new OptionalIfPresentResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("OptionalIfPresentResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Optional<String> input = Optional.empty();\n"
				+ "if (input.isPresent()) {\n"
				+ "	String value = input.get();\n"
				+ "	System.out.println(value);\n"
				+ "}";
		String expected = ""
				+ "Optional<String> input = Optional.empty();\n"
				+ "input.ifPresent(value -> System.out.println(value));";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "The usage of  Optional.get should be avoided in general because it can potentially "
				+ "throw a NoSuchElementException (it is likely to be deprecated in future releases).  "
				+ "It is often the case that the invocation of Optional.get is wrapped by a condition "
				+ "that uses  Optional.isPresent. Such cases can be replaced with the "
				+ "Optional.ifPresent(Consumer<? super T> consumer).";
		assertAll(
				() -> assertEquals("Use Optional::ifPresent", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("OptionalIfPresentResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(171, event.getOffset()),
				() -> assertEquals(17, event.getLength()),
				() -> assertEquals(8, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		OptionalIfPresentResolver visitor = new OptionalIfPresentResolver(node -> node.getStartPosition() == 172);
		visitor.addMarkerListener(RefactoringMarkers.getFor("OptionalIfPresentResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Optional<String> input = Optional.empty();\n"
				+ "if (input.isPresent()) {\n"
				+ "	String value = input.get();\n"
				+ "	System.out.println(value);\n"
				+ "}";
		String expected = ""
				+ "Optional<String> input = Optional.empty();\n"
				+ "input.ifPresent(value -> System.out.println(value));";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
