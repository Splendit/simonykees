package eu.jsparrow.core.markers.visitor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class FlatMapInsteadOfNestedLoopsResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		RefactoringMarkers.clear();
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		FlatMapInsteadOfNestedLoopsResolver visitor = new FlatMapInsteadOfNestedLoopsResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("FlatMapInsteadOfNestedLoopsResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<List<String>> matrix = new ArrayList<>();\n"
				+ "matrix.forEach(row -> {\n"
				+ "	row.stream().forEach(element -> {\n"
				+ "		System.out.print(element);\n"
				+ "	});\n"
				+ "});";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		FlatMapInsteadOfNestedLoopsResolver visitor = new FlatMapInsteadOfNestedLoopsResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("FlatMapInsteadOfNestedLoopsResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<List<String>> matrix = new ArrayList<>();\n"
				+ "matrix.forEach(row -> {\n"
				+ "	row.stream().forEach(element -> {\n"
				+ "		System.out.print(element);\n"
				+ "	});\n"
				+ "});";
		String expected = ""
				+ "List<List<String>> matrix=new ArrayList<>();\n"
				+ "matrix.stream().flatMap(row -> row.stream()).forEach(element -> {\n"
				+ "	System.out.print(element);\n"
				+ "});";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Compound data structures similar to 'Collection<Collection<T>>' are fairly common. "
				+ "This rule finds the nested invocations of 'Stream::forEach' which are used to "
				+ "iterate over such data structures and replaces them with single invocations of  "
				+ "'Stream::flatMap'. Using 'flatMap()' not only makes the code more readable but "
				+ "also allows for additional combinations with other Stream operations.";

		assertAll(
				() -> assertEquals("Replace Nested Loops with flatMap", event.getName()),
				() -> assertEquals(description, event.getMessage()),
				() -> assertEquals("FlatMapInsteadOfNestedLoopsResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(195, event.getOffset()),
				() -> assertEquals(139, event.getLength()),
				() -> assertEquals(9, event.getLineNumber()),
				() -> assertEquals(15, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		FlatMapInsteadOfNestedLoopsResolver visitor = new FlatMapInsteadOfNestedLoopsResolver(
				node -> node.getStartPosition() == 196);
		visitor.addMarkerListener(RefactoringMarkers.getFor("FlatMapInsteadOfNestedLoopsResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<List<String>> matrix = new ArrayList<>();\n"
				+ "matrix.forEach(row -> {\n"
				+ "	row.stream().forEach(element -> {\n"
				+ "		System.out.print(element);\n"
				+ "	});\n"
				+ "});";
		String expected = ""
				+ "List<List<String>> matrix=new ArrayList<>();\n"
				+ "matrix.stream().flatMap(row -> row.stream()).forEach(element -> {\n"
				+ "	System.out.print(element);\n"
				+ "});";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
