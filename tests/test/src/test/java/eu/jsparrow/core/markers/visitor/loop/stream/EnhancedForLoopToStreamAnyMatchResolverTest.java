package eu.jsparrow.core.markers.visitor.loop.stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class EnhancedForLoopToStreamAnyMatchResolverTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	public void beforeEach() throws Exception {
		RefactoringMarkers.clear();
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.Arrays.class.getName());
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		EnhancedForLoopToStreamAnyMatchResolver visitor = new EnhancedForLoopToStreamAnyMatchResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("EnhancedForLoopToStreamAnyMatchResolver"));
		setVisitor(visitor);
		String original = "" +
				"List<String> strings = Arrays.asList(\"\", \"\");\n"
				+ "boolean containsEmpty = false;\n"
				+ "for(String value : strings) {\n"
				+ "    if(value.isEmpty()) {\n"
				+ "        containsEmpty = true;\n"
				+ "        break;\n"
				+ "    }\n"
				+ "}";
		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		EnhancedForLoopToStreamAnyMatchResolver visitor = new EnhancedForLoopToStreamAnyMatchResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("EnhancedForLoopToStreamAnyMatchResolver"));
		setVisitor(visitor);
		String original = "" +
				"List<String> strings = Arrays.asList(\"\", \"\");\n"
				+ "boolean containsEmpty = false;\n"
				+ "for(String value : strings) {\n"
				+ "    if(value.isEmpty()) {\n"
				+ "        containsEmpty = true;\n"
				+ "        break;\n"
				+ "    }\n"
				+ "}";

		String expected = ""
				+ "List<String> strings = Arrays.asList(\"\", \"\");\n"
				+ "boolean containsEmpty = strings.stream().anyMatch(value -> value.isEmpty());";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Replaces occurrences of enhanced for-loops which are only used to initialize or return a boolean "
				+ "variable with Stream::anyMatch, Stream::allMatch or Stream::noneMatch. The stream syntax is more concise and improves readability.";
		assertAll(
				() -> assertEquals("Replace For-Loop with Stream::Match", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("EnhancedForLoopToStreamAnyMatchResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(230, event.getOffset()),
				() -> assertEquals(150, event.getLength()),
				() -> assertEquals(10, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		EnhancedForLoopToStreamAnyMatchResolver visitor = new EnhancedForLoopToStreamAnyMatchResolver(node -> node.getStartPosition() == 231);
		visitor.addMarkerListener(RefactoringMarkers.getFor("EnhancedForLoopToStreamAnyMatchResolver"));
		setVisitor(visitor);
		String original = "" +
				"List<String> strings = Arrays.asList(\"\", \"\");\n"
				+ "boolean containsEmpty = false;\n"
				+ "for(String value : strings) {\n"
				+ "    if(value.isEmpty()) {\n"
				+ "        containsEmpty = true;\n"
				+ "        break;\n"
				+ "    }\n"
				+ "}";

		String expected = ""
				+ "List<String> strings = Arrays.asList(\"\", \"\");\n"
				+ "boolean containsEmpty = strings.stream().anyMatch(value -> value.isEmpty());";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
