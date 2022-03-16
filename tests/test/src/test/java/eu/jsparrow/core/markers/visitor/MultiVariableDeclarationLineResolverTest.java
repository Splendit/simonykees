package eu.jsparrow.core.markers.visitor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class MultiVariableDeclarationLineResolverTest extends UsesJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		RefactoringMarkers.clear();
	}
	
	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		MultiVariableDeclarationLineResolver visitor = new MultiVariableDeclarationLineResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("MultiVariableDeclarationLineResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "private int a, b;\n"
				+ "private int c, d, e;\n"
				+ "\n"
				+ "void foo() {\n"
				+ "	int f, g, h, i;\n"
				+ "	String j, k = \"k\";\n"
				+ "}"
				+ "";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		MultiVariableDeclarationLineResolver visitor = new MultiVariableDeclarationLineResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("MultiVariableDeclarationLineResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "private int a, b;\n"
				+ "private int c, d, e;\n"
				+ "\n"
				+ "void foo() {\n"
				+ "	int f, g, h, i;\n"
				+ "	String j, k = \"k\";\n"
				+ "}" +
				"";
		String expected = ""
				+ "private int a;\n"
				+ "private int b;\n"
				+ "private int c;\n"
				+ "private int d;\n"
				+ "private int e;\n"
				+ "void foo(){\n"
				+ "  int f;\n"
				+ "  int g;\n"
				+ "  int h;\n"
				+ "  int i;\n"
				+ "  String j;\n"
				+ "  String k=\"k\";\n"
				+ "}"
				+ "";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(4, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Coding conventions for Java recommend that each variable or field is declared on a separate line. "
				+ "This rule will split declarations occurring on the same lines over multiple lines to improve readability.";
		
		assertAll(
				() -> assertEquals("Split Multiple Variable Declarations", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("MultiVariableDeclarationLineResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(45, event.getOffset()),
				() -> assertEquals(17, event.getLength()),
				() -> assertEquals(5, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		MultiVariableDeclarationLineResolver visitor = new MultiVariableDeclarationLineResolver(node -> node.getStartPosition() == 66);
		visitor.addMarkerListener(RefactoringMarkers.getFor("MultiVariableDeclarationLineResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "void foo() {\n"
				+ "	int f, g, h, i;\n"
				+ "}" +
				"";
		String expected = ""
				+ "void foo(){\n"
				+ "  int f;\n"
				+ "  int g;\n"
				+ "  int h;\n"
				+ "  int i;\n"
				+ "}"
				+ "";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
