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

class LambdaForEachIfWrapperToFilterResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.Arrays.class.getName());
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		LambdaForEachIfWrapperToFilterResolver visitor = new LambdaForEachIfWrapperToFilterResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("LambdaForEachIfWrapperToFilterResolver"));
		setVisitor(visitor);
		String original = "" +
				"List<String> list = Arrays.asList(\"foo\", \"bar\");\n"
				+ "list.stream().forEach(s -> {\n"
				+ "	if (s.length() > 3) {\n"
				+ "		System.out.println(s);\n"
				+ "		System.out.println(s + s);\n"
				+ "	}\n"
				+ "});";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		LambdaForEachIfWrapperToFilterResolver visitor = new LambdaForEachIfWrapperToFilterResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("LambdaForEachIfWrapperToFilterResolver"));
		setVisitor(visitor);
		String original = "" +
				"List<String> list = Arrays.asList(\"foo\", \"bar\");\n"
				+ "list.stream().forEach(s -> {\n"
				+ "	if (s.length() > 3) {\n"
				+ "		System.out.println(s);\n"
				+ "		System.out.println(s + s);\n"
				+ "	}\n"
				+ "});";
		String expected = ""
				+ "List<String> list = Arrays.asList(\"foo\", \"bar\");\n"
				+ "list.stream().filter(s -> s.length() > 3).forEach(s -> {\n"
				+ "	System.out.println(s);\n"
				+ "	System.out.println(s + s);\n"
				+ "});";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "If-Statements making up the entire execution block of a Stream::forEach method can be "
				+ "transformed into a call to Stream::filter. This only applies if there are no other statements "
				+ "passed as arguments to forEach(). filter() can be used with other stream functions and improves readability. ";
		assertAll(
				() -> assertEquals("Use Stream::filter", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("LambdaForEachIfWrapperToFilterResolver", event.getResolver()),
				() -> assertEquals("filter((s) -> s.length() > 3)", event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(216, event.getOffset()),
				() -> assertEquals(146, event.getLength()),
				() -> assertEquals(9, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		LambdaForEachIfWrapperToFilterResolver visitor = new LambdaForEachIfWrapperToFilterResolver(node -> node.getStartPosition() == 217);
		visitor.addMarkerListener(RefactoringMarkers.getFor("LambdaForEachIfWrapperToFilterResolver"));
		setVisitor(visitor);
		String original = "" +
				"List<String> list = Arrays.asList(\"foo\", \"bar\");\n"
				+ "list.stream().forEach(s -> {\n"
				+ "	if (s.length() > 3) {\n"
				+ "		System.out.println(s);\n"
				+ "		System.out.println(s + s);\n"
				+ "	}\n"
				+ "});";
		String expected = ""
				+ "List<String> list = Arrays.asList(\"foo\", \"bar\");\n"
				+ "list.stream().filter(s -> s.length() > 3).forEach(s -> {\n"
				+ "	System.out.println(s);\n"
				+ "	System.out.println(s + s);\n"
				+ "});";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
