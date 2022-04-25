package eu.jsparrow.core.markers.visitor;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

class PrimitiveObjectUseEqualsResolverTest extends UsesSimpleJDTUnitFixture {

	private String template = "Integer a = new Integer(1);Integer b = new Integer(2);if (%s) {}";

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		PrimitiveObjectUseEqualsResolver visitor = new PrimitiveObjectUseEqualsResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("PrimitiveObjectUseEqualsResolver"));
		setVisitor(visitor);
		String original = String.format(template, "a==b");

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		PrimitiveObjectUseEqualsResolver visitor = new PrimitiveObjectUseEqualsResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("PrimitiveObjectUseEqualsResolver"));
		setVisitor(visitor);
		String original = String.format(template, "a==b");
		String expected = String.format(template, "a.equals(b)");
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "It is recommended that you use equals() on primitive objects. Applying this rule will replace occurrences of != and == with equals().\n"
				+ "\n"
				+ "For example, '\"hello\" == \"world\"' will become '\"hello\".equals(\"world\")'\n"
				+ "\n"
				+ "Using this rule helps to avoid bugs, as == checks for object reference equality instead of value equality.";
		assertAll(
				() -> assertEquals("Use equals() on Primitive Objects", event.getName()),
				() -> assertEquals(description, event.getMessage()),
				() -> assertEquals("PrimitiveObjectUseEqualsResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(165, event.getOffset()),
				() -> assertEquals(6, event.getLength()),
				() -> assertEquals(8, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@ParameterizedTest
	@MethodSource(value = "primitiveObjectsUseEquals")
	void test_resolveMarkers_shouldResolveOne1(String originalCondition, String expectedCondition, int position)
			throws Exception {
		PrimitiveObjectUseEqualsResolver visitor = new PrimitiveObjectUseEqualsResolver(
				node -> node.getStartPosition() == position);
		visitor.addMarkerListener(RefactoringMarkers.getFor("PrimitiveObjectUseEqualsResolver"));
		setVisitor(visitor);
		String original = String.format(template, originalCondition);
		String expected = String.format(template, expectedCondition);
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

	private static Stream<Arguments> primitiveObjectsUseEquals() {
		return Stream.of(
				Arguments.of("a==b", "a.equals(b)", 165),
				Arguments.of("a!=b", "!a.equals(b)", 165),
				Arguments.of("a==(Integer)b", "a.equals((Integer)b)", 165));
	}
}
