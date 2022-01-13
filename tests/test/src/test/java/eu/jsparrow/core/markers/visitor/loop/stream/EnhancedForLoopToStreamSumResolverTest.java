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

class EnhancedForLoopToStreamSumResolverTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	public void beforeEach() throws Exception {
		RefactoringMarkers.clear();
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.Arrays.class.getName());
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		EnhancedForLoopToStreamSumResolver visitor = new EnhancedForLoopToStreamSumResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("EnhancedForLoopToStreamSumResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<Integer> numbers = Arrays.asList(1, 2, 3);\n"
				+ "int sum = 0;\n"
				+ "for(int n : numbers) {\n"
				+ "    sum += n;\n"
				+ "}";
		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		EnhancedForLoopToStreamSumResolver visitor = new EnhancedForLoopToStreamSumResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("EnhancedForLoopToStreamSumResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<Integer> numbers = Arrays.asList(1, 2, 3);\n"
				+ "int sum = 0;\n"
				+ "for(int n : numbers) {\n"
				+ "    sum += n;\n"
				+ "}";

		String expected = ""
				+ "List<Integer> numbers = Arrays.asList(1, 2, 3);\n"
				+ "int sum = numbers.stream().mapToInt(Integer::intValue).sum();";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Transforms enhanced for-loops which are only used for summing up the elements of a collection to a Stream::sum invocation.";
		assertAll(
				() -> assertEquals("Replace For-Loop with Stream::sum", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("EnhancedForLoopToStreamSumResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(214, event.getOffset()),
				() -> assertEquals(55, event.getLength()),
				() -> assertEquals(10, event.getLineNumber()),
				() -> assertEquals(10, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		EnhancedForLoopToStreamSumResolver visitor = new EnhancedForLoopToStreamSumResolver(node -> node.getStartPosition() == 215);
		visitor.addMarkerListener(RefactoringMarkers.getFor("EnhancedForLoopToStreamSumResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<Integer> numbers = Arrays.asList(1, 2, 3);\n"
				+ "int sum = 0;\n"
				+ "for(int n : numbers) {\n"
				+ "    sum += n;\n"
				+ "}";

		String expected = ""
				+ "List<Integer> numbers = Arrays.asList(1, 2, 3);\n"
				+ "int sum = numbers.stream().mapToInt(Integer::intValue).sum();";


		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
