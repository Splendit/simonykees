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

class IndexOfToContainsResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.List.class.getName());
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		IndexOfToContainsResolver visitor = new IndexOfToContainsResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("IndexOfToContainsResolver"));
		setVisitor(visitor);
		String original = ""
				+ "		List<String> l = new ArrayList<>();\n"
				+ "		String s = \"searchString\";\n"
				+ "		if (l.indexOf(s) == -1) {\n"
				+ "			l.add(s);\n"
				+ "		}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		IndexOfToContainsResolver visitor = new IndexOfToContainsResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("IndexOfToContainsResolver"));
		setVisitor(visitor);
		String original = ""
				+ "		List<String> l = new ArrayList<>();\n"
				+ "		String s = \"searchString\";\n"
				+ "		if (l.indexOf(s) == -1) {\n"
				+ "			l.add(s);\n"
				+ "		}";
		String expected = ""
				+ "		List<String> l = new ArrayList<>();\n"
				+ "		String s = \"searchString\";\n"
				+ "		if (!l.contains(s)) {\n"
				+ "			l.add(s);\n"
				+ "		}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "This rule replaces calls to indexOf() on instances of String or Collection with calls to the contains() method.\n"
				+ "\n"
				+ "For example 'l.indexOf(s) >= 0' is transformed to 'l.contains(s)'.\n"
				+ "\n"
				+ "'contains()' was introduced in Java 1.4 and helps to make the code more readable. ";
		assertAll(
				() -> assertEquals("Replace indexOf() with contains()", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("IndexOfToContainsResolver", event.getResolver()),
				() -> assertEquals("!l.contains(s)", event.getCodePreview()),
				() -> assertEquals(14, event.getHighlightLength()),
				() -> assertEquals(220, event.getOffset()),
				() -> assertEquals(18, event.getLength()),
				() -> assertEquals(10, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		IndexOfToContainsResolver visitor = new IndexOfToContainsResolver(node -> node.getStartPosition() == 221);
		visitor.addMarkerListener(RefactoringMarkers.getFor("IndexOfToContainsResolver"));
		setVisitor(visitor);
		String original = ""
				+ "		List<String> l = new ArrayList<>();\n"
				+ "		String s = \"searchString\";\n"
				+ "		if (l.indexOf(s) == -1) {\n"
				+ "			l.add(s);\n"
				+ "		}";
		String expected = ""
				+ "		List<String> l = new ArrayList<>();\n"
				+ "		String s = \"searchString\";\n"
				+ "		if (!l.contains(s)) {\n"
				+ "			l.add(s);\n"
				+ "		}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}