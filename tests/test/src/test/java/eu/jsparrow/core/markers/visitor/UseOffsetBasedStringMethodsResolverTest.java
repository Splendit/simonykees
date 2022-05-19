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

class UseOffsetBasedStringMethodsResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() {
		RefactoringMarkers.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		UseOffsetBasedStringMethodsResolver visitor = new UseOffsetBasedStringMethodsResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseOffsetBasedStringMethodsResolver"));
		setVisitor(visitor);
		String original = "" +
				"String str = \"Hello World!\";\n" +
				"int index = str.substring(6).indexOf('d');\n";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		UseOffsetBasedStringMethodsResolver visitor = new UseOffsetBasedStringMethodsResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseOffsetBasedStringMethodsResolver"));
		setVisitor(visitor);
		String original = "" +
				"String str = \"Hello World!\";\n" +
				"int index = str.substring(6).indexOf('d');\n";
		String expected = "" +
				"String str = \"Hello World!\";\n" +
				"int index=max(str.indexOf('d',6) - 6,-1);\n";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String message = ""
				+ "This rule avoids creating intermediate String instances by making use of the overloaded offset "
				+ "based methods in the String API. For example, if 'substring(beginIndex)' is followed by 'startsWith(aString)', "
				+ "then both invocations are removed and 'startsWith(aString, beginIndex)' is used instead."
				+ "";
		assertAll(
				() -> assertEquals("Use Offset Based String Methods", event.getName()),
				() -> assertEquals(message, event.getMessage()),
				() -> assertEquals("UseOffsetBasedStringMethodsResolver", event.getResolver()),
				() -> assertEquals(message, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(138, event.getOffset()),
				() -> assertEquals(29, event.getLength()),
				() -> assertEquals(7, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	private static Stream<Arguments> offsetBasedStringMethodsSamples() {
		String indexOfOriginal = "" +
				"String str = \"Hello World!\";\n" +
				"int index = str.substring(6).indexOf('d');\n";
		String indexOfExpected = "" +
				"String str = \"Hello World!\";\n" +
				"int index=max(str.indexOf('d',6) - 6,-1);\n";
		int indexOfPosition = 138;

		String startsWithOriginal = "" +
				"String str = \"Hello World!\";\n" +
				"boolean startsWith = str.substring(6).startsWith(\"World\");";
		String startsWithExpected = "" +
				"String str = \"Hello World!\";\n" +
				"boolean startsWith = str.startsWith(\"World\", 6);";
		int startsWithPostion = 147;

		String lastIndexOfOriginal = "" +
				"String str = \"Hello World!\";\n" +
				"int index = str.substring(6).lastIndexOf('d');";
		String lastIndexOfExpected = "" +
				"String str = \"Hello World!\";\n" +
				"int index=max(str.lastIndexOf('d',6) - 6,-1);";
		int lastIndexOfPosition = 138;
		return Stream.of(
				Arguments.of(indexOfOriginal, indexOfExpected, indexOfPosition),
				Arguments.of(startsWithOriginal, startsWithExpected, startsWithPostion),
				Arguments.of(lastIndexOfOriginal, lastIndexOfExpected, lastIndexOfPosition));

	}

	@ParameterizedTest
	@MethodSource(value = "offsetBasedStringMethodsSamples")
	void test_resolveMarkers_shouldResolveOne(String original, String expected, int startPosition) throws Exception {
		UseOffsetBasedStringMethodsResolver visitor = new UseOffsetBasedStringMethodsResolver(
				node -> node.getStartPosition() == startPosition);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseOffsetBasedStringMethodsResolver"));
		setVisitor(visitor);
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
