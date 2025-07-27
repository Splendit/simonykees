package eu.jsparrow.core.markers.visitor.factory.methods;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class CollectionsFactoryMethodsResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
		fixture.addImport(java.util.Collections.class.getName());
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		CollectionsFactoryMethodsResolver visitor = new CollectionsFactoryMethodsResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("CollectionsFactoryMethodsResolver"));
		setVisitor(visitor);
		String original = "" +
				"List<String> list2 = new ArrayList<>();\n"
				+ "list2.add(\"a\");\n"
				+ "list2.add(\"b\");\n"
				+ "list2.add(\"c\");\n"
				+ "list2 = Collections.unmodifiableList(list2);";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		CollectionsFactoryMethodsResolver visitor = new CollectionsFactoryMethodsResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("CollectionsFactoryMethodsResolver"));
		setVisitor(visitor);
		String original = "" +
				"List<String> list2 = new ArrayList<>();\n"
				+ "list2.add(\"a\");\n"
				+ "list2.add(\"b\");\n"
				+ "list2.add(\"c\");\n"
				+ "list2 = Collections.unmodifiableList(list2);";
		String expected = "" +
				"List<String> list2 = List.of(\"a\", \"b\", \"c\");";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Replace Collections.unmodifiableList/Set/Map with factory methods for collections introduced in Java 9, respectively List.of, Set.of and Map.ofEntries";
		assertAll(
				() -> assertEquals("Use Factory Methods for Collections", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("CollectionsFactoryMethodsResolver", event.getResolver()),
				() -> assertEquals("List.of(\"a\",\"b\",\"c\")", event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(298, event.getOffset()),
				() -> assertEquals(35, event.getLength()),
				() -> assertEquals(13, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		CollectionsFactoryMethodsResolver visitor = new CollectionsFactoryMethodsResolver(node -> node.getStartPosition() == 299);
		visitor.addMarkerListener(RefactoringMarkers.getFor("CollectionsFactoryMethodsResolver"));
		setVisitor(visitor);
		String original = "" +
				"List<String> list2 = new ArrayList<>();\n"
				+ "list2.add(\"a\");\n"
				+ "list2.add(\"b\");\n"
				+ "list2.add(\"c\");\n"
				+ "list2 = Collections.unmodifiableList(list2);";
		String expected = "" +
				"List<String> list2 = List.of(\"a\", \"b\", \"c\");";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
