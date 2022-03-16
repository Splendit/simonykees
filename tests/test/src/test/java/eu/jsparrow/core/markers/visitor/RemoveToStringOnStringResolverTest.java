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

class RemoveToStringOnStringResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		RemoveToStringOnStringResolver visitor = new RemoveToStringOnStringResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveToStringOnStringResolver"));
		setVisitor(visitor);
		String original = "" +
				"String s = \"anStringLiteral\".toString();";
		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		RemoveToStringOnStringResolver visitor = new RemoveToStringOnStringResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveToStringOnStringResolver"));
		setVisitor(visitor);
		String original = "" +
				"String s = \"anStringLiteral\".toString();";
		String expected = "" +
				"String s = \"anStringLiteral\";";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "All method invocations of toString() on a String element are not needed. Applying this rule will remove such method calls.\n"
				+ "\n"
				+ "For example, '\"string\".toString()' becomes '\"string\"'.";
		assertAll(
				() -> assertEquals("Remove toString() on String", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("RemoveToStringOnStringResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(100, event.getOffset()),
				() -> assertEquals(28, event.getLength()),
				() -> assertEquals(6, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		RemoveToStringOnStringResolver visitor = new RemoveToStringOnStringResolver(node -> node.getStartPosition() == 100);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveToStringOnStringResolver"));
		setVisitor(visitor);
		String original = "" +
				"String s = \"anStringLiteral\".toString();";
		String expected = "" +
				"String s = \"anStringLiteral\";";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
