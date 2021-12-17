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

class UseCollectionsSingletonListResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.List.class.getName());
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		UseCollectionsSingletonListResolver visitor = new UseCollectionsSingletonListResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseCollectionsSingletonListResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<String> strings = Arrays.asList();";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		UseCollectionsSingletonListResolver visitor = new UseCollectionsSingletonListResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseCollectionsSingletonListResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<String> strings = Arrays.asList();";
		String expected = ""
				+ "List<String> strings = Collections.emptyList();";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Replaces Arrays.asList with 0 or 1 parameters respectively with Collections.emptyList() or Collections.singletonList(..). \n"
				+ "\n"
				+ "Note: Arrays.asList creates a fixed size list while Collections.singletonList creates an immutable list and therefore does not allow operations like set(index, element).";
		assertAll(
				() -> assertEquals("Use Collections Singleton List", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("UseCollectionsSingletonListResolver", event.getResolver()),
				() -> assertEquals("emptyList", event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(160, event.getOffset()),
				() -> assertEquals(15, event.getLength()),
				() -> assertEquals(2, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		UseCollectionsSingletonListResolver visitor = new UseCollectionsSingletonListResolver(node -> node.getStartPosition() == 161);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseCollectionsSingletonListResolver"));
		setVisitor(visitor);
		String original = ""
				+ "List<String> strings = Arrays.asList();";
		String expected = ""
				+ "List<String> strings = Collections.emptyList();";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
