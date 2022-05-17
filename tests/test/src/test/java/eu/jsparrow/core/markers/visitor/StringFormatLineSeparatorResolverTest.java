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

class StringFormatLineSeparatorResolverTest extends UsesSimpleJDTUnitFixture {

	
	@BeforeEach
	void setUpVisitor() throws Exception {
		RefactoringMarkers.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		StringFormatLineSeparatorResolver visitor = new StringFormatLineSeparatorResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("StringFormatLineSeparatorResolver"));
		setVisitor(visitor);
		String original = "String.format(\"\\n\\n\");";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		StringFormatLineSeparatorResolver visitor = new StringFormatLineSeparatorResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("StringFormatLineSeparatorResolver"));
		setVisitor(visitor);
		String original = "String.format(\"\\n\\n\");";
		String expected = "String.format(\"%n%n\");";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "This rule replaces any occurrences of '\\n' with '%n'.\n"
				+ "\n"
				+ "For example 'String.format(\"\\n\\n\")' is replaced by 'String.format(\"%n%n\")'.\n"
				+ "\n"
				+ "The benefit of this approach is that '%n' is portable across platforms.";

		assertAll(
				() -> assertEquals("Use Portable Newline", event.getName()),
				() -> assertEquals(description, event.getMessage()),
				() -> assertEquals("StringFormatLineSeparatorResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(103, event.getOffset()),
				() -> assertEquals(6, event.getLength()),
				() -> assertEquals(6, event.getLineNumber()),
				() -> assertEquals(1, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		StringFormatLineSeparatorResolver visitor = new StringFormatLineSeparatorResolver(node -> node.getStartPosition() == 103);
		visitor.addMarkerListener(RefactoringMarkers.getFor("StringFormatLineSeparatorResolver"));
		setVisitor(visitor);
		String original = "String.format(\"\\n\\n\");";
		String expected = "String.format(\"%n%n\");";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
