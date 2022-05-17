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

class StringUtilsResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		RefactoringMarkers.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		StringUtilsResolver visitor = new StringUtilsResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("StringUtilsResolver"));
		setVisitor(visitor);
		String original = "String value = \"\"; value.isEmpty();";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		StringUtilsResolver visitor = new StringUtilsResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("StringFormatLineSeparatorResolver"));
		setVisitor(visitor);
		String original = "String value = \"\"; value.isEmpty();";
		String expected = "String value = \"\"; StringUtils.isEmpty(value);";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "This rule replaces various String methods with their null-safe counterparts from StringUtils.\n"
				+ "\n"
				+ "For example, '\"String\".trim()' becomes 'StringUtils.trim(\"String\")'.\n"
				+ "\n"
				+ "Using this rule makes null pointer exceptions less likely to occur.";

		assertAll(
				() -> assertEquals("Use StringUtils Methods", event.getName()),
				() -> assertEquals(description, event.getMessage()),
				() -> assertEquals("StringUtilsResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(116, event.getOffset()),
				() -> assertEquals(15, event.getLength()),
				() -> assertEquals(7, event.getLineNumber()),
				() -> assertEquals(10, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		StringUtilsResolver visitor = new StringUtilsResolver(node -> node.getStartPosition() == 116);
		visitor.addMarkerListener(RefactoringMarkers.getFor("StringFormatLineSeparatorResolver"));
		setVisitor(visitor);
		String original = "String value = \"\"; value.isEmpty();";
		String expected = "String value = \"\"; StringUtils.isEmpty(value);";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
