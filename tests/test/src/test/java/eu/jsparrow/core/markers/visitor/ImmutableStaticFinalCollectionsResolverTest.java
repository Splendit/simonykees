package eu.jsparrow.core.markers.visitor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class ImmutableStaticFinalCollectionsResolverTest extends UsesJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		RefactoringMarkers.clear();
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.ArrayList.class.getName());
	}
	
	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		ImmutableStaticFinalCollectionsResolver visitor = new ImmutableStaticFinalCollectionsResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ImmutableStaticFinalCollectionsResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "private static final List<String> CONSTANT_LIST = new ArrayList<>();";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		ImmutableStaticFinalCollectionsResolver visitor = new ImmutableStaticFinalCollectionsResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ImmutableStaticFinalCollectionsResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "private static final List<String> CONSTANT_LIST = new ArrayList<>();";
		String expected = ""
				+ "private static final List<String> CONSTANT_LIST = Collections.unmodifiableList(new ArrayList<>());";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "An unmodifiable Collection can be created with the matching "
				+ "Collections.unmodifiable...() method. Some examples are Collections.unmodifiableList(), "
				+ "Collections.unmodifiableSortedSet(), etc.\n"
				+ "A declaration of a Collection with the static and final modifiers is not sufficient "
				+ "because it might still be modifiable.\n"
				+ "The Collections which are created with Collections.unmodifiable...() throw an "
				+ "UnsupportedOperationException as soon as a modification is attempted.";
		
		assertAll(
				() -> assertEquals("Replace static final Collections with Collections.unmodifiable...()", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("ImmutableStaticFinalCollectionsResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(131, event.getOffset()),
				() -> assertEquals(33, event.getLength()),
				() -> assertEquals(8, event.getLineNumber()),
				() -> assertEquals(10, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		ImmutableStaticFinalCollectionsResolver visitor = new ImmutableStaticFinalCollectionsResolver(node -> node.getStartPosition() == 131);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ImmutableStaticFinalCollectionsResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "private static final List<String> CONSTANT_LIST = new ArrayList<>();";
		String expected = ""
				+ "private static final List<String> CONSTANT_LIST = Collections.unmodifiableList(new ArrayList<>());";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
