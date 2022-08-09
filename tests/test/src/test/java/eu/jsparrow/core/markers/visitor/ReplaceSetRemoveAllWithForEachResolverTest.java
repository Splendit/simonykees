package eu.jsparrow.core.markers.visitor;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class ReplaceSetRemoveAllWithForEachResolverTest extends UsesJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.Set.class.getName());
		RefactoringMarkers.clear();
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		ReplaceSetRemoveAllWithForEachResolver visitor = new ReplaceSetRemoveAllWithForEachResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceSetRemoveAllWithForEachResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"	void exampleWithParametersForSetAndList(Set<String> stringSet, List<String> stringsToRemove) {\n" +
				"		stringSet.removeAll(stringsToRemove);\n" +
				"	}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());

	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		ReplaceSetRemoveAllWithForEachResolver visitor = new ReplaceSetRemoveAllWithForEachResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceSetRemoveAllWithForEachResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"	void exampleWithParametersForSetAndList(Set<String> stringSet, List<String> stringsToRemove) {\n" +
				"		stringSet.removeAll(stringsToRemove);\n" +
				"	}";

		String expected = "" +
				"	void exampleWithParametersForSetAndList(Set<String> stringSet, List<String> stringsToRemove) {\n" +
				"		stringsToRemove.forEach(stringSet::remove);\n" +
				"	}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = "Using the method 'removeAll(Collection)' in order to removing elements from a Set may lead to performance problems because of a possible O(n^2) complexity."
				+ " This rule replaces 'removeAll' invocations by corresponding 'forEach' constructs."
				+ " For example 'mySet.removeAll(myList);' is replaced by 'myList.forEach(mySet::remove);'";
		assertAll(
				() -> assertEquals("Replace Set.removeAll With ForEach", event.getName()),
				() -> assertEquals(description, event.getMessage()),
				() -> assertEquals("ReplaceSetRemoveAllWithForEachResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(194, event.getOffset()),
				() -> assertEquals(36, event.getLength()),
				() -> assertEquals(9, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		ReplaceSetRemoveAllWithForEachResolver visitor = new ReplaceSetRemoveAllWithForEachResolver(
				node -> node.getStartPosition() == 194);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceSetRemoveAllWithForEachResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"	void exampleWithParametersForSetAndList(Set<String> stringSet, List<String> stringsToRemove) {\n" +
				"		stringSet.removeAll(stringsToRemove);\n" +
				"	}";

		String expected = "" +
				"	void exampleWithParametersForSetAndList(Set<String> stringSet, List<String> stringsToRemove) {\n" +
				"		stringsToRemove.forEach(stringSet::remove);\n" +
				"	}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

}
