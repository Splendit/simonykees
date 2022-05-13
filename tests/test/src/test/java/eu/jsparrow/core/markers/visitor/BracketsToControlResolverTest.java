package eu.jsparrow.core.markers.visitor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class BracketsToControlResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		RefactoringMarkers.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		BracketsToControlResolver visitor = new BracketsToControlResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("BracketsToControlResolver"));
		setVisitor(visitor);
		String original = ""
				+ "if (true) System.out.println();";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		BracketsToControlResolver visitor = new BracketsToControlResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("BracketsToControlResolver"));
		setVisitor(visitor);
		String original = ""
				+ "if (true) System.out.println();";
		String expected = ""
				+ "if (true) { System.out.println(); }";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Transforms single statements after control statements to block constructs by adding curly braces. This improves readability. ";

		assertAll(
				() -> assertEquals("Add Braces to Control Statements", event.getName()),
				() -> assertEquals(description, event.getMessage()),
				() -> assertEquals("BracketsToControlResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(111, event.getOffset()),
				() -> assertEquals(21, event.getLength()),
				() -> assertEquals(7, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		BracketsToControlResolver visitor = new BracketsToControlResolver(node -> node.getStartPosition() == 111);
		visitor.addMarkerListener(RefactoringMarkers.getFor("BracketsToControlResolver"));
		setVisitor(visitor);
		String original = ""
				+ "if (true) System.out.println();";
		String expected = ""
				+ "if (true) { System.out.println(); }";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

	@ParameterizedTest
	@MethodSource(value = "loopExamples")
	void test_resolveMarkersInLoopBody_shouldResolveOne(String original, String expected) throws Exception {
		BracketsToControlResolver visitor = new BracketsToControlResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("BracketsToControlResolver"));
		setVisitor(visitor);
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

	private static Stream<Arguments> loopExamples() {
		String forLoopOriginal = ""
				+ "for (int i = 0; i < values.size(); i++) "
				+ "System.out.println(values.get(i));";
		String forLoopExpected = ""
				+ "for (int i = 0; i < values.size(); i++) { "
				+ "System.out.println(values.get(i)); "
				+ "}";

		String whileLoopOriginal = ""
				+ "int i = 0;\n"
				+ "while (i < values.size())\n"
				+ "	System.out.println(values.get(i++));";
		String whileLoopExpected = ""
				+ "int i = 0;\n"
				+ "while (i < values.size()) { \n"
				+ "	System.out.println(values.get(i++));"
				+ "}";

		String enhancedForLoopOriginal = ""
				+ "for (String value : values)\n"
				+ "	System.out.println(value);\n";

		String enhancedForLoopExpected = ""
				+ "for (String value : values) { \n"
				+ "	System.out.println(value);\n"
				+ "}";

		String doWhileLoopOriginal = ""
				+ "int i = 0;\n"
				+ "do\n"
				+ "	System.out.println(values.get(i++));\n"
				+ "while (i < values.size());";
		String doWhileLoopExpected = ""
				+ "int i = 0;\n"
				+ "do {\n"
				+ "	System.out.println(values.get(i++));\n"
				+ "} while (i < values.size());";

		return Stream.of(
				Arguments.of(forLoopOriginal, forLoopExpected),
				Arguments.of(whileLoopOriginal, whileLoopExpected),
				Arguments.of(enhancedForLoopOriginal, enhancedForLoopExpected),
				Arguments.of(doWhileLoopOriginal, doWhileLoopExpected));
	}
}
