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

class DateDeprecatedResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		RefactoringMarkers.clear();
		fixture.addImport(java.util.Date.class.getName());
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		DateDeprecatedResolver visitor = new DateDeprecatedResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("DateDeprecatedResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Date date = new Date(99, 1, 1);";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		DateDeprecatedResolver visitor = new DateDeprecatedResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("DateDeprecatedResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Date date = new Date(99, 1, 1);";
		String expected = ""
				+ "Calendar calendar = Calendar.getInstance();\n"
				+ "calendar.set(1999, 1, 1);\n"
				+ "Date date = calendar.getTime();";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Removes deprecated Date constructs which are obsolete since JDK version 1.1.";

		assertAll(
				() -> assertEquals("Remove Deprecated Date Constructs", event.getName()),
				() -> assertEquals(description, event.getMessage()),
				() -> assertEquals("DateDeprecatedResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(124, event.getOffset()),
				() -> assertEquals(18, event.getLength()),
				() -> assertEquals(7, event.getLineNumber()),
				() -> assertEquals(1, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		DateDeprecatedResolver visitor = new DateDeprecatedResolver(node -> node.getStartPosition() == 125);
		visitor.addMarkerListener(RefactoringMarkers.getFor("DateDeprecatedResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Date date = new Date(99, 1, 1);";
		String expected = ""
				+ "Calendar calendar = Calendar.getInstance();\n"
				+ "calendar.set(1999, 1, 1);\n"
				+ "Date date = calendar.getTime();";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

}
