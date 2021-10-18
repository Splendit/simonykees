package eu.jsparrow.core.markers.visitor;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class UseComparatorMethodsResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		fixture.addImport(java.util.Comparator.class.getName());
		RefactoringMarkers.clear();
	}

	@AfterEach
	void tearDown() throws Exception {
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		UseComparatorMethodsResolver visitor = new UseComparatorMethodsResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.UseComparatorMethodsResolver"));
		setVisitor(visitor);
		String original = "Comparator<Integer> comparator = (lhs, rhs) -> lhs.compareTo(rhs);";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		UseComparatorMethodsResolver visitor = new UseComparatorMethodsResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.UseComparatorMethodsResolver"));
		setVisitor(visitor);
		String original = "Comparator<Integer> comparator = (lhs, rhs) -> lhs.compareTo(rhs);";
		String expected = "Comparator<Integer> comparator = Comparator.naturalOrder();";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		assertAll(
				() -> assertEquals("Use predefined comparator", event.getName()),
				() -> assertEquals("Lambda expression can be replaced with predefined comparator", event.getMessage()), 
				() -> assertEquals("eu.jsparrow.core.markers.visitor.UseComparatorMethodsResolver", event.getResolver()),
				() -> assertEquals("Comparator.naturalOrder()", event.getCodePreview()),
				() -> assertEquals(25, event.getHighlightLength()),
				() -> assertEquals(151, event.getOffset()),
				() -> assertEquals(32, event.getLength()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		UseComparatorMethodsResolver visitor = new UseComparatorMethodsResolver(node -> node.getStartPosition() >= 151 && node.getStartPosition() <= 183);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.UseComparatorMethodsResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Comparator<Integer> comparator = (lhs, rhs) -> lhs.compareTo(rhs);\n"
				+ "Comparator<Integer> comparator2 = (lhs, rhs) -> lhs.compareTo(rhs);";
		String expected = ""
				+ "Comparator<Integer> comparator = Comparator.naturalOrder();"
				+ "Comparator<Integer> comparator2 = (lhs, rhs) -> lhs.compareTo(rhs);";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
