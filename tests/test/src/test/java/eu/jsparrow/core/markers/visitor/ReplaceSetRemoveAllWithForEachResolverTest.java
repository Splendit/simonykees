package eu.jsparrow.core.markers.visitor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class ReplaceSetRemoveAllWithForEachResolverTest extends UsesJDTUnitFixture {

	private static final String REMOVE_ALL_ONE_OCCURRANCE = "" +
			"	void testWithOneOccurrence(Set<String> stringSet, List<String> stringsToRemove) {\n" +
			"		" + "stringSet.removeAll(stringsToRemove);\n" +
			"	}";

	private void prepareResolver(Predicate<ASTNode> positionChecker) {
		ReplaceSetRemoveAllWithForEachResolver visitor = new ReplaceSetRemoveAllWithForEachResolver(positionChecker);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceSetRemoveAllWithForEachResolver"));
		setDefaultVisitor(visitor);
	}

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
		prepareResolver(node -> false);

		assertNoChange(REMOVE_ALL_ONE_OCCURRANCE);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());

	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		prepareResolver(node -> true);
		String expected = "" +
				"	void testWithOneOccurrence(Set<String> stringSet, List<String> stringsToRemove) {\n" +
				"		stringsToRemove.forEach(stringSet::remove);\n" +
				"	}";

		assertChange(REMOVE_ALL_ONE_OCCURRANCE, expected);
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
				() -> assertEquals(181, event.getOffset()),
				() -> assertEquals(36, event.getLength()),
				() -> assertEquals(9, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		final int startPosition = 181;
		prepareResolver(node -> node.getStartPosition() == startPosition);

		String expected = "" +
				"	void testWithOneOccurrence(Set<String> stringSet, List<String> stringsToRemove) {\n" +
				"		stringsToRemove.forEach(stringSet::remove);\n" +
				"	}";

		assertChange(REMOVE_ALL_ONE_OCCURRANCE, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		assertEquals(startPosition, events.get(0)
			.getOffset());
	}

	@Test
	void test_markerGeneration_shouldGenerateTwoMarkerEvents() throws Exception {
		prepareResolver(node -> true);
		String original = ""
				+ "	void testWithTwoOccurrences(Set<String> stringSet1, Set<String> stringSet2, List<String> stringsToRemove) {\n"
				+ "		stringSet1.removeAll(stringsToRemove);\n"
				+ "		stringSet2.removeAll(stringsToRemove);\n"
				+ "	}";

		String expected = ""
				+ "	void testWithTwoOccurrences(Set<String> stringSet1,  Set<String> stringSet2,  List<String> stringsToRemove){\n"
				+ "		stringsToRemove.forEach(stringSet1::remove);\n"
				+ "		stringsToRemove.forEach(stringSet2::remove);\n"
				+ "	}\n";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(2, events.size());
		assertEquals(207, events.get(0)
			.getOffset());
		assertEquals(254, events.get(1)
			.getOffset());
	}

	@Test
	void test_markerGeneration_shouldGenerateForFirst() throws Exception {
		int startPosition = 207;
		prepareResolver(node -> node.getStartPosition() == startPosition);
		String original = ""
				+ "	void testWithTwoOccurrences(Set<String> stringSet1, Set<String> stringSet2, List<String> stringsToRemove) {\n"
				+ "		stringSet1.removeAll(stringsToRemove);\n"
				+ "		stringSet2.removeAll(stringsToRemove);\n"
				+ "	}";

		String expected = ""
				+ "	void testWithTwoOccurrences(Set<String> stringSet1,  Set<String> stringSet2,  List<String> stringsToRemove){\n"
				+ "		stringsToRemove.forEach(stringSet1::remove);\n"
				+ "		stringSet2.removeAll(stringsToRemove);\n"
				+ "	}\n";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		assertEquals(startPosition, events.get(0)
			.getOffset());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateForSecond() throws Exception {
		int startPosition = 254;
		prepareResolver(node -> node.getStartPosition() == startPosition);
		String original = ""
				+ "	void testWithTwoOccurrences(Set<String> stringSet1, Set<String> stringSet2, List<String> stringsToRemove) {\n"
				+ "		stringSet1.removeAll(stringsToRemove);\n"
				+ "		stringSet2.removeAll(stringsToRemove);\n"
				+ "	}";

		String expected = ""
				+ "	void testWithTwoOccurrences(Set<String> stringSet1,  Set<String> stringSet2,  List<String> stringsToRemove){\n"
				+ "		stringSet1.removeAll(stringsToRemove);\n"
				+ "		stringsToRemove.forEach(stringSet2::remove);\n"
				+ "	}\n";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		assertEquals(startPosition, events.get(0)
			.getOffset());
	}
}
