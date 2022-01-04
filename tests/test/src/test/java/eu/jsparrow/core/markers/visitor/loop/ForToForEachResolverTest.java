package eu.jsparrow.core.markers.visitor.loop;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class ForToForEachResolverTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	public void beforeEach() throws Exception {
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		ForToForEachResolver visitor = new ForToForEachResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ForToForEachResolver"));
		setVisitor(visitor);
		String original = "" +
				"		String[] ms = {};\n" +
				"		StringBuilder sb = new StringBuilder();\n" +
				"		for (int i = 0; i < ms.length; i++) {\n" +
				"			String s = ms[i];\n" +
				"			sb.append(s);\n" +
				"		}";
		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		ForToForEachResolver visitor = new ForToForEachResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ForToForEachResolver"));
		setVisitor(visitor);
		String original = "" +
				"		String[] ms = {};\n" +
				"		StringBuilder sb = new StringBuilder();\n" +
				"		for (int i = 0; i < ms.length; i++) {\n" +
				"			String s = ms[i];\n" +
				"			sb.append(s);\n" +
				"		}";

		String expected = "" +
				"		String[] ms = {};\n" +
				"		StringBuilder sb = new StringBuilder();\n" +
				"		for (String s : ms) {\n" +
				"			sb.append(s);\n" +
				"		}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Since Java 1.5 enhanced for-loops can be used to iterate over collections. This rule replaces "
				+ "old for-loops utilizing iterators with enhanced for-loops in order to improve readability.";
		assertAll(
				() -> assertEquals("Replace For-Loop with Enhanced-For-Loop", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("ForToForEachResolver", event.getResolver()),
				() -> assertEquals(""
						+ "for (String s : ms) {\n"
						+ " 	...\n"
						+ "}", event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(163, event.getOffset()),
				() -> assertEquals(103, event.getLength()),
				() -> assertEquals(8, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		ForToForEachResolver visitor = new ForToForEachResolver(node -> node.getStartPosition() == 163);
		visitor.addMarkerListener(RefactoringMarkers.getFor("CollectionRemoveAllResolver"));
		setVisitor(visitor);
		String original = "" +
				"		String[] ms = {};\n" +
				"		StringBuilder sb = new StringBuilder();\n" +
				"		for (int i = 0; i < ms.length; i++) {\n" +
				"			String s = ms[i];\n" +
				"			sb.append(s);\n" +
				"		}";

		String expected = "" +
				"		String[] ms = {};\n" +
				"		StringBuilder sb = new StringBuilder();\n" +
				"		for (String s : ms) {\n" +
				"			sb.append(s);\n" +
				"		}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

}
