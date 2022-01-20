package eu.jsparrow.core.markers.visitor.lambdaforeach;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class LambdaForEachMapResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.Arrays.class.getName());
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		LambdaForEachMapResolver visitor = new LambdaForEachMapResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("LambdaForEachMapResolver"));
		setVisitor(visitor);
		String original = "" +
				"List<String> list = Arrays.asList(\"foo\", \"bar\");\n"
				+ "list.stream()\n"
				+ "	.filter(s -> !s.isEmpty())\n"
				+ "	.forEach(s -> {\n"
				+ "		String subString = s.substring(1);\n"
				+ "		System.out.print(subString);\n"
				+ "	});";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		LambdaForEachMapResolver visitor = new LambdaForEachMapResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("LambdaForEachMapResolver"));
		setVisitor(visitor);
		String original = "" +
				"List<String> list = Arrays.asList(\"foo\", \"bar\");\n"
				+ "list.stream()\n"
				+ "	.filter(s -> !s.isEmpty())\n"
				+ "	.forEach(s -> {\n"
				+ "		String subString = s.substring(1);\n"
				+ "		System.out.print(subString);\n"
				+ "	});";
		String expected = "" +
				"List<String> list = Arrays.asList(\"foo\", \"bar\");\n"
				+ "list.stream()\n"
				+ "	.filter(s -> !s.isEmpty())\n"
				+ "	.map(s -> s.substring(1))\n"
				+ "	.forEach(subString -> System.out.print(subString));";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Extracts a block from the body of the consumer of the Stream::forEach method "
				+ "and introduces Stream::map instead. This makes complicated code blocks easier to read and reuse. ";
		assertAll(
				() -> assertEquals("Use Stream::map", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("LambdaForEachMapResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(242, event.getOffset()),
				() -> assertEquals(104, event.getLength()),
				() -> assertEquals(9, event.getLineNumber()),
				() -> assertEquals(15, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		LambdaForEachMapResolver visitor = new LambdaForEachMapResolver(node -> node.getStartPosition() == 243);
		visitor.addMarkerListener(RefactoringMarkers.getFor("LambdaForEachMapResolver"));
		setVisitor(visitor);
		String original = "" +
				"List<String> list = Arrays.asList(\"foo\", \"bar\");\n"
				+ "list.stream()\n"
				+ "	.filter(s -> !s.isEmpty())\n"
				+ "	.forEach(s -> {\n"
				+ "		String subString = s.substring(1);\n"
				+ "		System.out.print(subString);\n"
				+ "	});";
		String expected = "" +
				"List<String> list = Arrays.asList(\"foo\", \"bar\");\n"
				+ "list.stream()\n"
				+ "	.filter(s -> !s.isEmpty())\n"
				+ "	.map(s -> s.substring(1))\n"
				+ "	.forEach(subString -> System.out.print(subString));";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
