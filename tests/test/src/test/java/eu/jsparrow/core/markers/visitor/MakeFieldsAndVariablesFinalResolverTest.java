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

class MakeFieldsAndVariablesFinalResolverTest extends UsesJDTUnitFixture {

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
		MakeFieldsAndVariablesFinalResolver visitor = new MakeFieldsAndVariablesFinalResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("MakeFieldsAndVariablesFinalResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "private String nonStaticField =\"nonStaticField\";"
				+ "private String nonStaticFieldEffectivelyFinal =\"nonStaticFieldEffectivelyFinal\";"
				+ ""
				+ "public void c() {"
				+ "	int i = 0;"
				+ "	int j = 0;"
				+ "	System.out.println(i);"
				+ "	System.out.println(j++);"
				+ "}"
				+ ""
				+ "public void d() {"
				+ "	nonStaticField += \"Altered\";"
				+ "}"
				+ "";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateTwoMarkerEvents() throws Exception {
		MakeFieldsAndVariablesFinalResolver visitor = new MakeFieldsAndVariablesFinalResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("MakeFieldsAndVariablesFinalResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "private String nonStaticField =\"nonStaticField\";"
				+ "private String nonStaticFieldEffectivelyFinal =\"nonStaticFieldEffectivelyFinal\";"
				+ ""
				+ "public void c() {"
				+ "	int i = 0;"
				+ "	int j = 0;"
				+ "	System.out.println(i);"
				+ "	System.out.println(j++);"
				+ "}"
				+ ""
				+ "public void d() {"
				+ "	nonStaticField += \"Altered\";"
				+ "}"
				+ "";
		String expected = ""
				+ "private String nonStaticField=\"nonStaticField\";\n"
				+ "private final String nonStaticFieldEffectivelyFinal=\"nonStaticFieldEffectivelyFinal\";\n"
				+ "public void c(){\n"
				+ " final int i=0;\n"
				+ "  int j=0;\n"
				+ "  System.out.println(i);\n"
				+ "  System.out.println(j++);\n"
				+ "}\n"
				+ "public void d(){\n"
				+ "  nonStaticField+=\"Altered\";\n"
				+ "}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(2, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "This rule declares local variables and private fields final if they meet the following criteria:\n"
				+ "	1.) Static fields have to be initialised either at the declaration or in a static initialiser\n"
				+ "	2.) Non-static fields have to be initialised at the declaration, in a class initialiser or in all constructors of a class\n"
				+ "	3.) The field or variable is not assigned after its initialisation";
		
		assertAll(
				() -> assertEquals("Make Fields And Variables Final", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("MakeFieldsAndVariablesFinalResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(99, event.getOffset()),
				() -> assertEquals(81, event.getLength()),
				() -> assertEquals(6, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		MakeFieldsAndVariablesFinalResolver visitor = new MakeFieldsAndVariablesFinalResolver(node -> node.getStartPosition() == 99);
		visitor.addMarkerListener(RefactoringMarkers.getFor("MakeFieldsAndVariablesFinalResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "private String nonStaticField =\"nonStaticField\";"
				+ "private String nonStaticFieldEffectivelyFinal =\"nonStaticFieldEffectivelyFinal\";"
				+ ""
				+ "public void c() {"
				+ "	int i = 0;"
				+ "	int j = 0;"
				+ "	System.out.println(i);"
				+ "	System.out.println(j++);"
				+ "}"
				+ ""
				+ "public void d() {"
				+ "	nonStaticField += \"Altered\";"
				+ "}"
				+ "";
		String expected = ""
				+ "private String nonStaticField=\"nonStaticField\";\n"
				+ "private final String nonStaticFieldEffectivelyFinal=\"nonStaticFieldEffectivelyFinal\";\n"
				+ "public void c(){\n"
				+ "  int i=0;\n"
				+ "  int j=0;\n"
				+ "  System.out.println(i);\n"
				+ "  System.out.println(j++);\n"
				+ "}\n"
				+ "public void d(){\n"
				+ "  nonStaticField+=\"Altered\";\n"
				+ "}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
