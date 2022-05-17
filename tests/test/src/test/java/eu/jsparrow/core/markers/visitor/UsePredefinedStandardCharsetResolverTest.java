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

@SuppressWarnings("nls")
class UsePredefinedStandardCharsetResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception{
		
		RefactoringMarkers.clear();
		fixture.addImport(java.nio.charset.Charset.class.getName());
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		UsePredefinedStandardCharsetResolver visitor = new UsePredefinedStandardCharsetResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UsePredefinedStandardCharsetResolver"));
		setVisitor(visitor);

		String original = "Charset c = Charset.forName(\"UTF-8\");\n";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		UsePredefinedStandardCharsetResolver visitor = new UsePredefinedStandardCharsetResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UsePredefinedStandardCharsetResolver"));
		setVisitor(visitor);
		
		String original = "Charset c_UTF_8 = Charset.forName(\"UTF-8\");\n";
		String expected = "Charset c_UTF_8 = StandardCharsets.UTF_8;";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String message = "This rule replaces invocations of 'Charset.forName(String)' by constants defined in 'StandardCharsets'.";

		assertAll(
				() -> assertEquals("Use Predefined Standard Charset", event.getName()),
				() -> assertEquals(message, event.getMessage()),
				() -> assertEquals("UsePredefinedStandardCharsetResolver", event.getResolver()),
				() -> assertEquals(message, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(140, event.getOffset()),
				() -> assertEquals(24, event.getLength()),
				() -> assertEquals(7, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	private static Stream<Arguments> charsetForNameInvocationSamples() {
		String original_UTF_8 = "Charset c_UTF_8 = Charset.forName(\"UTF-8\");";
		String expected_UTF_8 = "Charset c_UTF_8 = StandardCharsets.UTF_8;";
		int position_UTF_8 = 140;
		
		String original_ISO_8859_1 = "Charset c_ISO_8859_1 = Charset.forName(\"ISO-8859-1\");";
		String expected_ISO_8859_1 = "Charset c_ISO_8859_1 = StandardCharsets.ISO_8859_1;";
		int position_ISO_8859_1 = 145;

		String original_US_ASCII = "Charset c_US_ASCII = Charset.forName(\"US-ASCII\");";
		String expected_US_ASCII = "Charset c_US_ASCII = StandardCharsets.US_ASCII;";
		int position_US_ASCII = 143;

		return Stream.of(
				Arguments.of(original_UTF_8, expected_UTF_8, position_UTF_8),
				Arguments.of(original_ISO_8859_1, expected_ISO_8859_1, position_ISO_8859_1),
				Arguments.of(original_US_ASCII, expected_US_ASCII, position_US_ASCII));

	}

	@ParameterizedTest
	@MethodSource(value = "charsetForNameInvocationSamples")
	void test_resolveMarkers_shouldResolveOne(String original, String expected, int startPosition) throws Exception {
		UsePredefinedStandardCharsetResolver visitor = new UsePredefinedStandardCharsetResolver(
				node -> node.getStartPosition() >= startPosition-1 && node.getStartPosition()<=startPosition +1);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UsePredefinedStandardCharsetResolver"));
		setVisitor(visitor);
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
