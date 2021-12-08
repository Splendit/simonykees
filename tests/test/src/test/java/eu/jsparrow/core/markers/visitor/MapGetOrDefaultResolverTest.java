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

class MapGetOrDefaultResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		fixture.addImport(java.util.concurrent.ConcurrentHashMap.class.getName());
		RefactoringMarkers.clear();
	}
	
	String original = "" +
			"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
			"		String value = map.get(\"key\");\n" + 
			"		if(value == null) {\n" + 
			"			value = \"default\";\n" + 
			"		}";
	String expected = "" + 
			"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
			"		String value = map.getOrDefault(\"key\", \"default\");";
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		MapGetOrDefaultResolver visitor = new MapGetOrDefaultResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.MapGetOrDefaultResolver"));
		setVisitor(visitor);
		String original = "" +
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value = map.get(\"key\");\n" + 
				"		if(value == null) {\n" + 
				"			value = \"default\";\n" + 
				"		}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		MapGetOrDefaultResolver visitor = new MapGetOrDefaultResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.MapGetOrDefaultResolver"));
		setVisitor(visitor);
		String original = "" +
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value = map.get(\"key\");\n" + 
				"		if(value == null) {\n" + 
				"			value = \"default\";\n" + 
				"		}";
		String expected = "" +
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value = map.getOrDefault(\"key\", \"default\");";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Java 8 adds an API that allows for getting a default value if the map contains no mapping for the key. \n"
				+ "For example: \n"
				+ "	 Object value = map.get(key); \n"
				+ "	 if(value == null) { \n"
				+ "		 value = default;\n"
				+ "	 } \n"
				+ "\n"
				+ "is replaced with: \n"
				+ "	 Object value = map.getOrDefault(key, default); \n"
				+ "\n"
				+ "Note: the rule applies only on map implementations that do not allow null values. ";
		assertAll(
				() -> assertEquals("Replace Map::get by Map::getOrDefault", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("eu.jsparrow.core.markers.visitor.MapGetOrDefaultResolver", event.getResolver()),
				() -> assertEquals("map.getOrDefault(\"key\",\"default\")", event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(226, event.getOffset()),
				() -> assertEquals(14, event.getLength()),
				() -> assertEquals(2, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		MapGetOrDefaultResolver visitor = new MapGetOrDefaultResolver(node -> node.getStartPosition() == 227);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.MapGetOrDefaultResolver"));
		setVisitor(visitor);
		String original = "" +
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value = map.get(\"key\");\n" + 
				"		if(value == null) {\n" + 
				"			value = \"default\";\n" + 
				"		}";
		String expected = "" +
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value = map.getOrDefault(\"key\", \"default\");";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
