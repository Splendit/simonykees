package org.eu.jsparrow.rules.java16.patternmatching;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;
import eu.jsparrow.rules.java16.patternmatching.UsePatternMatchingForInstanceofResolver;

class UsePatternMatchingForInstanceofResolverTest  extends UsesJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		UsePatternMatchingForInstanceofResolver visitor = new UsePatternMatchingForInstanceofResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UsePatternMatchingForInstanceofResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"	void test() {\n" +
				"		Object o = \"\";\n" +
				"		if(o instanceof String) {\n" +
				"			String value = (String)o;\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		UsePatternMatchingForInstanceofResolver visitor = new UsePatternMatchingForInstanceofResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UsePatternMatchingForInstanceofResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"	void test() {\n" +
				"		Object o = \"\";\n" +
				"		if(o instanceof String) {\n" +
				"			String value = (String)o;\n" +
				"		}\n" +
				"	}";
		String expected = "" +
				"	void test() {\n" +
				"		Object o = \"\";\n" +
				"		if(o instanceof String value) {\n" +
				"		}\n" +
				"	}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "This rule replaces instanceof expressions by Pattern Matching for instanceof introduced in Java 16. \n"
				+ "\n"
				+ "Commonly, an instanceof expression is followed by a local variable declaration initialized with a casting "
				+ "expression. Pattern Matching for instanceof combines three steps (i.e., type checking, variable declaration, "
				+ "and type casting) into a single step, thus reducing some boilerplate code and eliminating sources of errors.";

		assertAll(
				() -> assertEquals("Use Pattern Matching for Instanceof", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("UsePatternMatchingForInstanceofResolver", event.getResolver()),
				() -> assertEquals("o instanceof String value", event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(94, event.getOffset()),
				() -> assertEquals(19, event.getLength()),
				() -> assertEquals(7, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		UsePatternMatchingForInstanceofResolver visitor = new UsePatternMatchingForInstanceofResolver(node -> node.getStartPosition() == 94);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UsePatternMatchingForInstanceofResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"	void test() {\n" +
				"		Object o = \"\";\n" +
				"		if(o instanceof String) {\n" +
				"			String value = (String)o;\n" +
				"		}\n" +
				"	}";
		String expected = "" +
				"	void test() {\n" +
				"		Object o = \"\";\n" +
				"		if(o instanceof String value) {\n" +
				"		}\n" +
				"	}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
