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

class EnhancedForLoopToStreamTakeWhileResolverTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	public void beforeEach() throws Exception {
		RefactoringMarkers.clear();
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.Arrays.class.getName());
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		EnhancedForLoopToStreamTakeWhileResolver visitor = new EnhancedForLoopToStreamTakeWhileResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("EnhancedForLoopToStreamTakeWhileResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<String> values = Arrays.asList(\"a\", \"b\");\n"
				+ "for(String value : values) {\n"
				+ "    if(!value.isEmpty()) {\n"
				+ "        break;\n"
				+ "    }\n"
				+ "    System.out.println(value);\n"
				+ "}";
		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		EnhancedForLoopToStreamTakeWhileResolver visitor = new EnhancedForLoopToStreamTakeWhileResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("EnhancedForLoopToStreamTakeWhileResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<String> values = Arrays.asList(\"a\", \"b\");\n"
				+ "for(String value : values) {\n"
				+ "    if(!value.isEmpty()) {\n"
				+ "        break;\n"
				+ "    }\n"
				+ "    System.out.println(value);\n"
				+ "}";

		String expected = ""
				+ "List<String> values = Arrays.asList(\"a\", \"b\");\n"
				+ "values.stream().takeWhile(value -> value.isEmpty()).forEach(value -> {\n"
				+ "		System.out.println(value);\n"
				+ "});";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Replaces Enhanced for-loops iterating over the prefix of a collection with Stream::takeWhile introduced in Java 9.";
		assertAll(
				() -> assertEquals("Replace For-Loop with Stream::takeWhile", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("EnhancedForLoopToStreamTakeWhileResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(192, event.getOffset()),
				() -> assertEquals(151, event.getLength()),
				() -> assertEquals(9, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		EnhancedForLoopToStreamTakeWhileResolver visitor = new EnhancedForLoopToStreamTakeWhileResolver(node -> node.getStartPosition() == 193);
		visitor.addMarkerListener(RefactoringMarkers.getFor("EnhancedForLoopToStreamTakeWhileResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<String> values = Arrays.asList(\"a\", \"b\");\n"
				+ "for(String value : values) {\n"
				+ "    if(!value.isEmpty()) {\n"
				+ "        break;\n"
				+ "    }\n"
				+ "    System.out.println(value);\n"
				+ "}";

		String expected = ""
				+ "List<String> values = Arrays.asList(\"a\", \"b\");\n"
				+ "values.stream().takeWhile(value -> value.isEmpty()).forEach(value -> {\n"
				+ "		System.out.println(value);\n"
				+ "});";


		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
