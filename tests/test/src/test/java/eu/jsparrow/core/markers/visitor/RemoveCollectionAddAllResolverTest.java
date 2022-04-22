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

class RemoveCollectionAddAllResolverTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
		fixture.addImport(java.util.Set.class.getName());
		fixture.addImport(java.util.HashSet.class.getName());
		fixture.addImport(java.util.Arrays.class.getName());
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		RemoveCollectionAddAllResolver visitor = new RemoveCollectionAddAllResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveCollectionAddAllResolver"));
		setVisitor(visitor);
		String original = "" +
				"Set<String> set = new HashSet<>();\n" +
				"set.addAll(Arrays.asList(\"value1\", \"value2\"));";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		RemoveCollectionAddAllResolver visitor = new RemoveCollectionAddAllResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveCollectionAddAllResolver"));
		setVisitor(visitor);
		String original = "" +
				"Set<String> set = new HashSet<>();\n" +
				"set.addAll(Arrays.asList(\"value1\", \"value2\"));";
		String expected = "Set<String> set = new HashSet<>(Arrays.asList(\"value1\", \"value2\"));";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "In order to apply the rule, the following two statements must occur:\n"
				+ "A declaration of a local variable storing an instance of java.util.Collection which is initialized with "
				+ "an empty constructor call and which is immediately followed by an invocation of the “addAll” - Method on the same variable.";
		assertAll(
				() -> assertEquals("Remove Collection::addAll", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("RemoveCollectionAddAllResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(205, event.getOffset()),
				() -> assertEquals(46, event.getLength()),
				() -> assertEquals(10, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		RemoveCollectionAddAllResolver visitor = new RemoveCollectionAddAllResolver(node -> node.getStartPosition() == 206);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveCollectionAddAllResolver"));
		setVisitor(visitor);
		String original = "" +
				"Set<String> set = new HashSet<>();\n" +
				"set.addAll(Arrays.asList(\"value1\", \"value2\"));";
		String expected = "Set<String> set = new HashSet<>(Arrays.asList(\"value1\", \"value2\"));";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
