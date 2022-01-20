package eu.jsparrow.core.markers.visitor.loop.stream;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class EnhancedForLoopToStreamForEachResolverTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	public void beforeEach() throws Exception {
		RefactoringMarkers.clear();
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.Arrays.class.getName());
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		EnhancedForLoopToStreamForEachResolver visitor = new EnhancedForLoopToStreamForEachResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("EnhancedForLoopToStreamForEachResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<String> values = Arrays.asList(\"a\", \"b\");\n"
				+ "for (String string : values) {\n"
				+ "    System.out.println(string);\n"
				+ "    System.out.println(string);\n"
				+ "}";
		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		EnhancedForLoopToStreamForEachResolver visitor = new EnhancedForLoopToStreamForEachResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("EnhancedForLoopToStreamForEachResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<String> values = Arrays.asList(\"a\", \"b\");\n"
				+ "for (String string : values) {\n"
				+ "    System.out.println(string);\n"
				+ "    System.out.println(string);\n"
				+ "}";

		String expected = ""
				+ "List<String> values = Arrays.asList(\"a\", \"b\");\n"
				+ "values.forEach(string -> {\n"
				+ "		System.out.println(string);\n"
				+ "		System.out.println(string);\n"
				+ "});";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Enhanced For-Loops can be replaced by forEach().\n"
				+ "\n"
				+ "For example 'for(Item item: items) { }' becomes 'items.forEach()'.\n"
				+ "\n"
				+ "This makes code more readable and can be combined with other stream functions such as filter and map.";
		assertAll(
				() -> assertEquals("Replace For-Loop with Stream::forEach", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("EnhancedForLoopToStreamForEachResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(192, event.getOffset()),
				() -> assertEquals(120, event.getLength()),
				() -> assertEquals(9, event.getLineNumber()),
				() -> assertEquals(15, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		EnhancedForLoopToStreamForEachResolver visitor = new EnhancedForLoopToStreamForEachResolver(node -> node.getStartPosition() == 193);
		visitor.addMarkerListener(RefactoringMarkers.getFor("EnhancedForLoopToStreamForEachResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<String> values = Arrays.asList(\"a\", \"b\");\n"
				+ "for (String string : values) {\n"
				+ "    System.out.println(string);\n"
				+ "    System.out.println(string);\n"
				+ "}";

		String expected = ""
				+ "List<String> values = Arrays.asList(\"a\", \"b\");\n"
				+ "values.forEach(string -> {\n"
				+ "		System.out.println(string);\n"
				+ "		System.out.println(string);\n"
				+ "});";


		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
