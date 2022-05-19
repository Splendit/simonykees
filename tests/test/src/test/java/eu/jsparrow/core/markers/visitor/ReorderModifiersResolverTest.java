package eu.jsparrow.core.markers.visitor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class ReorderModifiersResolverTest extends UsesJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		RefactoringMarkers.clear();
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		ReorderModifiersResolver visitor = new ReorderModifiersResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReorderModifiersResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "static final public int STATIC_FINAL_PUBLIC = 0;";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		ReorderModifiersResolver visitor = new ReorderModifiersResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReorderModifiersResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "static final public int STATIC_FINAL_PUBLIC = 0;";
		String expected = ""
				+ "public static final int STATIC_FINAL_PUBLIC = 0;";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Reorders modifiers of Class, Field and Method declarations according to Java coding conventions.";

		assertAll(
				() -> assertEquals("Reorder Modifiers", event.getName()),
				() -> assertEquals(description, event.getMessage()),
				() -> assertEquals("ReorderModifiersResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(45, event.getOffset()),
				() -> assertEquals(19, event.getLength()),
				() -> assertEquals(5, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@ParameterizedTest
	@MethodSource(value = "reorderModifiersSamples")
	void test_resolveMarkers_shouldResolveOne(String original, String expected, int position) throws Exception {
		ReorderModifiersResolver visitor = new ReorderModifiersResolver(node -> node.getStartPosition() == position);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReorderModifiersResolver"));
		setDefaultVisitor(visitor);
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

	private static Stream<Arguments> reorderModifiersSamples() {
		String original1 = ""
				+ "static final public int STATIC_FINAL_PUBLIC = 0;";
		String expected1 = ""
				+ "public static final int STATIC_FINAL_PUBLIC = 0;";
		int position1 = 45;

		String original2 = ""
				+ "static public void method() {}";
		String expected2 = ""
				+ "public static void method() {}";
		int position2 = 45;

		String original3 = ""
				+ "final public class Foo {}";
		String expected3 = ""
				+ "public final class Foo {}";
		int position3 = 45;

		return Stream.of(
				Arguments.of(original1, expected1, position1),
				Arguments.of(original2, expected2, position2),
				Arguments.of(original3, expected3, position3));
	}
}
