package eu.jsparrow.core.markers.visitor.trycatch;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class MultiCatchResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
		fixture.addImport("java.lang.reflect.InvocationTargetException");
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		MultiCatchResolver visitor = new MultiCatchResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("MultiCatchResolver"));
		setVisitor(visitor);
		String original = "" +
				"try {\n"
				+ "	String.class.getConstructor(String.class).newInstance(\"aa\");\n"
				+ "} catch (InstantiationException e) {\n"
				+ "	e.printStackTrace();\n"
				+ "} catch (IllegalAccessException e) {\n"
				+ "	e.printStackTrace();\n"
				+ "} catch (IllegalArgumentException e) {\n"
				+ "	e.printStackTrace();\n"
				+ "} catch (InvocationTargetException e) {\n"
				+ "	e.printStackTrace();\n"
				+ "} catch (NoSuchMethodException e) {\n"
				+ "	e.printStackTrace();\n"
				+ "} catch (SecurityException e) {\n"
				+ "	e.printStackTrace();\n"
				+ "}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		MultiCatchResolver visitor = new MultiCatchResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("MultiCatchResolver"));
		setVisitor(visitor);
		String original = "" +
				"try {\n"
				+ "	String.class.getConstructor(String.class).newInstance(\"aa\");\n"
				+ "} catch (InstantiationException e) {\n"
				+ "	e.printStackTrace();\n"
				+ "} catch (IllegalAccessException e) {\n"
				+ "	e.printStackTrace();\n"
				+ "} catch (IllegalArgumentException e) {\n"
				+ "	e.printStackTrace();\n"
				+ "} catch (InvocationTargetException e) {\n"
				+ "	e.printStackTrace();\n"
				+ "} catch (NoSuchMethodException e) {\n"
				+ "	e.printStackTrace();\n"
				+ "} catch (SecurityException e) {\n"
				+ "	e.printStackTrace();\n"
				+ "}";
		String expected = "" +
				"  try {\n"
				+ "    String.class.getConstructor(String.class).newInstance(\"aa\");\n"
				+ "  }\n"
				+ " catch (  SecurityException|NoSuchMethodException|InvocationTargetException|IllegalArgumentException|IllegalAccessException|InstantiationException e) {\n"
				+ "    e.printStackTrace();\n"
				+ "  }";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Java 7 introduced the possibility to merge multiple catch clauses into a single multi-catch clause. "
				+ "Merging is only possible if the catch statements are identical. Using this rule reduces clutter "
				+ "and improves readability.";
		assertAll(
				() -> assertEquals("Use Multi-Catch", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("MultiCatchResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(141, event.getOffset()),
				() -> assertEquals(555, event.getLength()),
				() -> assertEquals(7, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		MultiCatchResolver visitor = new MultiCatchResolver(node -> node.getStartPosition() == 142);
		visitor.addMarkerListener(RefactoringMarkers.getFor("MultiCatchResolver"));
		setVisitor(visitor);
		String original = "" +
				"try {\n"
				+ "	String.class.getConstructor(String.class).newInstance(\"aa\");\n"
				+ "} catch (InstantiationException e) {\n"
				+ "	e.printStackTrace();\n"
				+ "} catch (IllegalAccessException e) {\n"
				+ "	e.printStackTrace();\n"
				+ "} catch (IllegalArgumentException e) {\n"
				+ "	e.printStackTrace();\n"
				+ "} catch (InvocationTargetException e) {\n"
				+ "	e.printStackTrace();\n"
				+ "} catch (NoSuchMethodException e) {\n"
				+ "	e.printStackTrace();\n"
				+ "} catch (SecurityException e) {\n"
				+ "	e.printStackTrace();\n"
				+ "}";
		String expected = "" +
				"  try {\n"
				+ "    String.class.getConstructor(String.class).newInstance(\"aa\");\n"
				+ "  }\n"
				+ "catch (SecurityException|NoSuchMethodException|InvocationTargetException|IllegalArgumentException|IllegalAccessException|InstantiationException e) {\n"
				+ "  e.printStackTrace();\n"
				+ "}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
