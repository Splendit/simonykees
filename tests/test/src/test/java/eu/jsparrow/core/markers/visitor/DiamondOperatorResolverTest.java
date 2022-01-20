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

class DiamondOperatorResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		DiamondOperatorResolver visitor = new DiamondOperatorResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("DiamondOperatorResolver"));
		setVisitor(visitor);
		String original = ""
				+  "List<String> values = new ArrayList<String>();";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		DiamondOperatorResolver visitor = new DiamondOperatorResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("DiamondOperatorResolver"));
		setVisitor(visitor);
		String original = ""
				+  "List<String> values = new ArrayList<String>();";
		String expected = ""
				+ "List<String> values = new ArrayList<>();";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Since Java 1.7 the Diamond Operator (<>) can be used to simplify instance creations where generics are involved.\n"
				+ "\n"
				+ "For example, 'Map<String, List<String>> myMap = new HashMap<String, List<String>>()' can be replaced by 'Map<String, List<String>> myMap = new HashMap<>()'.\n"
				+ "\n"
				+ " In order to apply this rule, your project must use Java 1.7 or later.";
		assertAll(
				() -> assertEquals("Remove Explicit Type Argument", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("DiamondOperatorResolver", event.getResolver()),
				() -> assertEquals("ArrayList<>", event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(162, event.getOffset()),
				() -> assertEquals(23, event.getLength()),
				() -> assertEquals(8, event.getLineNumber()),
				() -> assertEquals(1, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		DiamondOperatorResolver visitor = new DiamondOperatorResolver(node -> node.getStartPosition() == 163);
		visitor.addMarkerListener(RefactoringMarkers.getFor(""));
		setVisitor(visitor);
		String original = ""
				+  "List<String> values = new ArrayList<String>();";
		String expected = ""
				+ "List<String> values = new ArrayList<>();";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}