package eu.jsparrow.core.markers.visitor.optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class OptionalIfPresentOrElseResovlerTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	void setUpVisitor() throws Exception {
		setJavaVersion(JavaCore.VERSION_11);
		fixture.addImport(java.util.Optional.class.getName());
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		OptionalIfPresentOrElseResolver visitor = new OptionalIfPresentOrElseResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("OptionalIfPresentOrElseResolver"));
		setVisitor(visitor);
		String original = "" +
				"Optional<String> optional = Optional.empty();\n" +
				"if(optional.isPresent()) {\n" +
				"	String value = optional.get();\n" +
				"	System.out.print(value);\n" +
				"} else {\n" +
				"	System.out.println(\"No value\");\n" +
				"}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		OptionalIfPresentOrElseResolver visitor = new OptionalIfPresentOrElseResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("OptionalIfPresentOrElseResolver"));
		setVisitor(visitor);
		String original = "" +
				"Optional<String> optional = Optional.empty();\n" +
				"if(optional.isPresent()) {\n" +
				"	String value = optional.get();\n" +
				"	System.out.print(value);\n" +
				"} else {\n" +
				"	System.out.println(\"No value\");\n" +
				"}";
		String expected = "" +
				"Optional<String> optional = Optional.empty();\n" +
				"optional.ifPresentOrElse(\n" +
				"	value -> System.out.print(value),\n" +
				"	() -> System.out.println(\"No value\"));";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "It is common to have an else-statement following an Optional.isPresent check. "
				+ "One of the extensions of the Optional API in Java 9 is Optional.ifPresentOrElse, "
				+ "which performs either a Consumer or a Runnable depending on the presence of the "
				+ "value. This rule replaces an 'isPresent' check followed by an else-statement "
				+ "with a single 'ifPresentOrElse' invocation.";
	
		assertAll(
				() -> assertEquals("Use Optional::ifPresentOrElse", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("OptionalIfPresentOrElseResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(174, event.getOffset()),
				() -> assertEquals(20, event.getLength()),
				() -> assertEquals(8, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		OptionalIfPresentOrElseResolver visitor = new OptionalIfPresentOrElseResolver(node -> node.getStartPosition() == 175);
		visitor.addMarkerListener(RefactoringMarkers.getFor("OptionalIfPresentOrElseResolver"));
		setVisitor(visitor);
		String original = "" +
				"Optional<String> optional = Optional.empty();\n" +
				"if(optional.isPresent()) {\n" +
				"	String value = optional.get();\n" +
				"	System.out.print(value);\n" +
				"} else {\n" +
				"	System.out.println(\"No value\");\n" +
				"}";
		String expected = "" +
				"Optional<String> optional = Optional.empty();\n" +
				"optional.ifPresentOrElse(\n" +
				"	value -> System.out.print(value),\n" +
				"	() -> System.out.println(\"No value\"));";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

}
