package eu.jsparrow.core.markers.visitor;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class RemoveRedundantTypeCastResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		RemoveRedundantTypeCastResolver visitor = new RemoveRedundantTypeCastResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveRedundantTypeCastResolver"));
		setVisitor(visitor);
		String original = ""
				+  "((String)\"HelloWorld\").charAt(0);";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		RemoveRedundantTypeCastResolver visitor = new RemoveRedundantTypeCastResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveRedundantTypeCastResolver"));
		setVisitor(visitor);
		String original = ""
				+  "((String)\"HelloWorld\").charAt(0);";
		String expected = ""
				+ "\"HelloWorld\".charAt(0);";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "This rule removes unnecessary type cast operations. If the expression is casted to a type which already is exactly the type of the expression, then the type casting prefix is removed. \n"
				+ "Additionally, also parentheses will be removed if they are not necessary any more.";
		assertAll(
				() -> assertEquals("Remove Redundant Type Casts", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("RemoveRedundantTypeCastResolver", event.getResolver()),
				() -> assertEquals("\"HelloWorld\"", event.getCodePreview()),
				() -> assertEquals(12, event.getHighlightLength()),
				() -> assertEquals(89, event.getOffset()),
				() -> assertEquals(23, event.getLength()),
				() -> assertEquals(6, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		RemoveRedundantTypeCastResolver visitor = new RemoveRedundantTypeCastResolver(node -> node.getStartPosition() == 90);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveRedundantTypeCastResolver"));
		setVisitor(visitor);
		String original = ""
				+  "((String)\"HelloWorld\").charAt(0);";
		String expected = ""
				+ "\"HelloWorld\".charAt(0);";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
