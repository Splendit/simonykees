package eu.jsparrow.core.markers.visitor.loop;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class StringBuildingLoopResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void beforeEach() throws Exception {
		fixture.addImport(java.util.List.class.getName());
		RefactoringMarkers.clear();
		setJavaVersion(JavaCore.VERSION_1_8);
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		StringBuildingLoopResolver visitor = new StringBuildingLoopResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("StringBuildingLoopResolver"));
		setVisitor(visitor);
		String original = "" 
				+ "List<String> collectionOfStrings = java.util.Arrays.asList(\"1\", \"2\", \"3\");\n"
				+ "String result = \"\";\n"
				+ "for(String val : collectionOfStrings) {\n"
				+ "	result = result + val;\n"
				+ "}";
		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		StringBuildingLoopResolver visitor = new StringBuildingLoopResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("StringBuildingLoopResolver"));
		setVisitor(visitor);
		String original = "" 
				+ "List<String> collectionOfStrings = java.util.Arrays.asList(\"1\", \"2\", \"3\");\n"
				+ "String result = \"\";\n"
				+ "for(String val : collectionOfStrings) {\n"
				+ "	result = result + val;\n"
				+ "}";

		String expected = ""
				+ "List<String> collectionOfStrings = java.util.Arrays.asList(\"1\", \"2\", \"3\");\n"
				+ "String result=collectionOfStrings.stream().collect(Collectors.joining());";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Transforms loops which are only used for concatenating a string to an invocation of Stream::collect(Collectors.joining()). \n"
				+ " If the Java compliance level is below 1.8 and at least 1.5, then a StringBuilder is introduced for concatenating the values on each iteration of the loop.";
		assertAll(
				() -> assertEquals("Replace For-Loop with Stream::collect(Collectors.joining())", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("StringBuildingLoopResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(223, event.getOffset()),
				() -> assertEquals(85, event.getLength()),
				() -> assertEquals(9, event.getLineNumber()),
				() -> assertEquals(10, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		StringBuildingLoopResolver visitor = new StringBuildingLoopResolver(node -> node.getStartPosition() == 224);
		visitor.addMarkerListener(RefactoringMarkers.getFor("StringBuildingLoopResolver"));
		setVisitor(visitor);
		String original = "" 
				+ "List<String> collectionOfStrings = java.util.Arrays.asList(\"1\", \"2\", \"3\");\n"
				+ "String result = \"\";\n"
				+ "for(String val : collectionOfStrings) {\n"
				+ "	result = result + val;\n"
				+ "}";

		String expected = ""
				+ "List<String> collectionOfStrings = java.util.Arrays.asList(\"1\", \"2\", \"3\");\n"
				+ "String result=collectionOfStrings.stream().collect(Collectors.joining());";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
