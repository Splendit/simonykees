package org.eu.jsparrow.rules.java16.switchexpression;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;
import eu.jsparrow.rules.java16.switchexpression.UseSwitchExpressionResolver;

class UseSwitchExpressionResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		UseSwitchExpressionResolver visitor = new UseSwitchExpressionResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseSwitchExpressionResolver"));
		setVisitor(visitor);
		String original = ""
				+ "String value = System.in.toString();\n"
				+ "switch (value) {\n"
				+ "case \"t\":\n"
				+ "	System.out.println(\"true\");\n"
				+ "	break;\n"
				+ "case \"f\":\n"
				+ "	System.out.println(\"False\");\n"
				+ "	break;\n"
				+ "default:\n"
				+ "	System.out.println(\"None\");\n"
				+ "	break;\n"
				+ "}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		UseSwitchExpressionResolver visitor = new UseSwitchExpressionResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseSwitchExpressionResolver"));
		setVisitor(visitor);
		String original = ""
				+ "String value = System.in.toString();\n"
				+ "switch (value) {\n"
				+ "case \"t\":\n"
				+ "	System.out.println(\"true\");\n"
				+ "	break;\n"
				+ "case \"f\":\n"
				+ "	System.out.println(\"False\");\n"
				+ "	break;\n"
				+ "default:\n"
				+ "	System.out.println(\"None\");\n"
				+ "	break;\n"
				+ "}";
		String expected = ""
				+ "String value = System.in.toString();\n"
				+ "switch (value) {\n"
				+ "case \"t\" -> System.out.println(\"true\");\n"
				+ "case \"f\" -> System.out.println(\"False\");\n"
				+ "default -> System.out.println(\"None\");\n"
				+ "}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "In Java 14, the switch expressions turned to a standard feature. This rule replaces the traditional switch-case statements with switch-case "
				+ "expressions. Thus, avoiding the fall-through semantics of control flow and at the same time, removing some boilerplate code.";

		assertAll(
				() -> assertEquals("Use Switch Expression", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("UseSwitchExpressionResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(134, event.getOffset()),
				() -> assertEquals(296, event.getLength()),
				() -> assertEquals(7, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		UseSwitchExpressionResolver visitor = new UseSwitchExpressionResolver(node -> node.getStartPosition() == 134);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseSwitchExpressionResolver"));
		setVisitor(visitor);
		String original = ""
				+ "String value = System.in.toString();\n"
				+ "switch (value) {\n"
				+ "case \"t\":\n"
				+ "	System.out.println(\"true\");\n"
				+ "	break;\n"
				+ "case \"f\":\n"
				+ "	System.out.println(\"False\");\n"
				+ "	break;\n"
				+ "default:\n"
				+ "	System.out.println(\"None\");\n"
				+ "	break;\n"
				+ "}";
		String expected = ""
				+ "String value = System.in.toString();\n"
				+ "switch (value) {\n"
				+ "case \"t\" -> System.out.println(\"true\");\n"
				+ "case \"f\" -> System.out.println(\"False\");\n"
				+ "default -> System.out.println(\"None\");\n"
				+ "}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
	
	@Test
	void test_initializeVariable_shouldResolveOne() throws Exception {
		UseSwitchExpressionResolver visitor = new UseSwitchExpressionResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseSwitchExpressionResolver"));
		setVisitor(visitor);
		String original = ""
				+ "int finished = 1;\n"
				+ "String medal;\n"
				+ "switch(finished) {\n"
				+ "case 1 : \n"
				+ "    medal = \"Gold\";\n"
				+ "    break;\n"
				+ "case 2: \n"
				+ "    medal = \"Silver\";\n"
				+ "    break;\n"
				+ "case 3: \n"
				+ "    medal = \"Bronze\";\n"
				+ "    break;\n"
				+ "default:\n"
				+ "    medal = \"None\";\n"
				+ "}";
		String expected = ""
				+ "int finished = 1;\n"
				+ "String medal = switch (finished) {\n"
				+ "case 1 -> \"Gold\";\n"
				+ "case 2 -> \"Silver\";\n"
				+ "case 3 -> \"Bronze\";\n"
				+ "default -> \"None\";\n"
				+ "};";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
