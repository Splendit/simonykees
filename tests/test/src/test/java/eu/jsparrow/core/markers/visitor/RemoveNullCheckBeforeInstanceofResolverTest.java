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

class RemoveNullCheckBeforeInstanceofResolverTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	void setUp() {
		RefactoringMarkers.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		RemoveNullCheckBeforeInstanceofResolver visitor = new RemoveNullCheckBeforeInstanceofResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.RemoveNullCheckBeforeInstanceofResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Object name = new Object();\n"
				+ "if(name != null && name instanceof String) {\n"
				+ "	System.out.println(name);\n"
				+ "}";
		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		RemoveNullCheckBeforeInstanceofResolver visitor = new RemoveNullCheckBeforeInstanceofResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.RemoveNullCheckBeforeInstanceofResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Object name = new Object();\n"
				+ "if(name != null && name instanceof String) {\n"
				+ "	System.out.println(name);\n"
				+ "}";
		String expected = ""
				+ "Object name = new Object();\n"
				+ "if(name instanceof String) {\n"
				+ "	System.out.println(name);\n"
				+ "}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		assertAll(
				() -> assertEquals("Remove Null-Checks Before Instanceof", event.getName()),
				() -> assertEquals("null is not an instance of anything, therefore the null-check is redundant.", event.getMessage()), 
				() -> assertEquals("eu.jsparrow.core.markers.visitor.RemoveNullCheckBeforeInstanceofResolver", event.getResolver()),
				() -> assertEquals("if (name instanceof String) {\n"
						+ "  System.out.println(name);\n"
						+ "}\n", event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(129, event.getOffset()),
				() -> assertEquals(12, event.getLength()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		RemoveNullCheckBeforeInstanceofResolver visitor = new RemoveNullCheckBeforeInstanceofResolver(node -> node.getStartPosition() == 151);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.RemoveNullCheckBeforeInstanceofResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Object name = new Object(), value = new Object();\n"
				+ "if(name != null && name instanceof String) {\n"
				+ "	System.out.println(name);\n"
				+ "}"
				+ "if(value != null && value instanceof String) {\n"
				+ "	System.out.println(value);\n"
				+ "}";
		String expected = ""
				+ "Object name = new Object(), value = new Object();\n"
				+ "if(name instanceof String) {\n"
				+ "	System.out.println(name);\n"
				+ "}"
				+ "if(value != null && value instanceof String) {\n"
				+ "	System.out.println(value);\n"
				+ "}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
