package eu.jsparrow.core.markers.visitor.loop;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class WhileToForEachResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void beforeEach() throws Exception {
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.Collections.class.getName());
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		WhileToForEachResolver visitor = new WhileToForEachResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("WhileToForEachResolver"));
		setVisitor(visitor);
		String original = "" 
				+ "List<String>list = Collections.singletonList(\"\");\n"
				+ "int i = 0;\n"
				+ "while (i < list.size()) {\n"
				+ "	String t = list.get(i);\n"
				+ "	System.out.println(t);\n"
				+ "	i++;\n"
				+ "}";
		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		WhileToForEachResolver visitor = new WhileToForEachResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("WhileToForEachResolver"));
		setVisitor(visitor);
		String original = "" 
				+ "List<String>list = Collections.singletonList(\"\");\n"
				+ "int i = 0;\n"
				+ "while (i < list.size()) {\n"
				+ "	String t = list.get(i);\n"
				+ "	System.out.println(t);\n"
				+ "	i++;\n"
				+ "}";

		String expected = ""
				+ "List<String> list=Collections.singletonList(\"\");\n"
				+ "for (String t : list) {\n"
				+ "   System.out.println(t);\n"
				+ "}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Applying this rule replaces While-loops over iterators with an equivalent for-loop. Loops where the body modifies the iterator in some way will not be transformed.";
		assertAll(
				() -> assertEquals("Replace While-Loop with Enhanced For-Loop", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("WhileToForEachResolver", event.getResolver()),
				() -> assertEquals(""
						+ "for (String t : list) {\n"
						+ " 	...\n"
						+ "}", event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(220, event.getOffset()),
				() -> assertEquals(123, event.getLength()),
				() -> assertEquals(10, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		WhileToForEachResolver visitor = new WhileToForEachResolver(node -> node.getStartPosition() == 221);
		visitor.addMarkerListener(RefactoringMarkers.getFor("WhileToForEachResolver"));
		setVisitor(visitor);
		String original = "" 
				+ "List<String>list = Collections.singletonList(\"\");\n"
				+ "int i = 0;\n"
				+ "while (i < list.size()) {\n"
				+ "	String t = list.get(i);\n"
				+ "	System.out.println(t);\n"
				+ "	i++;\n"
				+ "}";

		String expected = ""
				+ "List<String> list=Collections.singletonList(\"\");\n"
				+ "for (String t : list) {\n"
				+ "   System.out.println(t);\n"
				+ "}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
