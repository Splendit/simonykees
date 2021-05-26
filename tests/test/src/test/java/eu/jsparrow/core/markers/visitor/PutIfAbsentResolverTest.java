package eu.jsparrow.core.markers.visitor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class PutIfAbsentResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
		fixture.addImport(java.util.Map.class.getName());
		fixture.addImport(java.util.HashMap.class.getName());
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		PutIfAbsentResolver visitor = new PutIfAbsentResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.PutIfAbsentResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Map<Integer, Integer> map = new HashMap<>();\n"
				+ "if (!map.containsKey(1)) {\n"
				+ "	map.put(1, 2);\n"
				+ "}";
		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		PutIfAbsentResolver visitor = new PutIfAbsentResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.PutIfAbsentResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Map<Integer, Integer> map = new HashMap<>();\n"
				+ "if (!map.containsKey(1)) {\n"
				+ "	map.put(1, 2);\n"
				+ "}";
		String expected = ""
				+ "Map<Integer, Integer> map = new HashMap<>();\n"
				+ "map.putIfAbsent(1, 2);";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		assertAll(
				() -> assertEquals("Replace put(..) with putIfAbsent(..)", event.getName()),
				() -> assertEquals("Use the Java 8 API that allows for conditionally adding entries to a map.", event.getMessage()), 
				() -> assertEquals("eu.jsparrow.core.markers.visitor.PutIfAbsentResolver", event.getResolver()),
				() -> assertEquals("MISSING.putIfAbsent(0,0);\n", event.getDescription()),
				() -> assertEquals(229, event.getOffset()),
				() -> assertEquals(13, event.getLength()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		PutIfAbsentResolver visitor = new PutIfAbsentResolver(node -> node.getStartPosition() == 230);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.PutIfAbsentResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Map<Integer, Integer> map = new HashMap<>();\n"
				+ "if (!map.containsKey(1)) {\n"
				+ "	map.put(1, 2);\n"
				+ "}"
				+ "if (!map.containsKey(2)) {\n"
				+ "	map.put(2, 3);\n"
				+ "}"
				+ "";
		String expected = ""
				+ "Map<Integer, Integer> map = new HashMap<>();\n"
				+ "map.putIfAbsent(1, 2);"
				+ "if (!map.containsKey(2)) {\n"
				+ "	map.put(2, 3);\n"
				+ "}"
				+ "";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
