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

class GuardIfConditionResolverTest extends UsesJDTUnitFixture {
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
		GuardConditionResolver visitor = new GuardConditionResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("GuardConditionResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "public void voidMethod(int i) {\n"
				+ "	doSomething(\"whatever\");\n"
				+ "	if(i > 0) {\n"
				+ "		doSomething(\"Should create guard condition with less\");\n"
				+ "		doSomething(\"Should transform\");\n"
				+ "	}\n"
				+ "}\n"
				+ "\n"
				+ "private void doSomething(String string) {\n"
				+ "	System.out.println(string);\n"
				+ "}" +
				"";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		GuardConditionResolver visitor = new GuardConditionResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("GuardConditionResolver"));
		setDefaultVisitor(visitor);
		String original = "" 
				+ "public void voidMethod(int i) {\n"
				+ "	doSomething(\"whatever\");\n"
				+ "	if(i > 0) {\n"
				+ "		doSomething(\"Should create guard condition with less\");\n"
				+ "		doSomething(\"Should transform\");\n"
				+ "	}\n"
				+ "}\n"
				+ "\n"
				+ "private void doSomething(String string) {\n"
				+ "	System.out.println(string);\n"
				+ "}" +
				"";
		String expected = "" +
				"public void voidMethod(int i) {\n"
				+ "	doSomething(\"whatever\");\n"
				+ "	if (i <= 0) {\n"
				+ "		return;\n"
				+ "	}\n"
				+ "	doSomething(\"Should create guard condition with less\");\n"
				+ "	doSomething(\"Should transform\");\n"
				+ "}\n"
				+ "\n"
				+ "private void doSomething(String string) {\n"
				+ "	System.out.println(string);\n"
				+ "}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "If the last statement on a method is an if-statement consisting of two or more "
				+ "statements, then a guard-if can be used instead and the body of the existing if-statement can be unwrapped.";
		
		assertAll(
				() -> assertEquals("Use Guard Condition", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("GuardConditionResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(118, event.getOffset()),
				() -> assertEquals(135, event.getLength()),
				() -> assertEquals(7, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		GuardConditionResolver visitor = new GuardConditionResolver(node -> node.getStartPosition() == 118);
		visitor.addMarkerListener(RefactoringMarkers.getFor("GuardConditionResolver"));
		setDefaultVisitor(visitor);
		String original = "" 
				+ "public void voidMethod(int i) {\n"
				+ "	doSomething(\"whatever\");\n"
				+ "	if(i > 0) {\n"
				+ "		doSomething(\"Should create guard condition with less\");\n"
				+ "		doSomething(\"Should transform\");\n"
				+ "	}\n"
				+ "}\n"
				+ "\n"
				+ "private void doSomething(String string) {\n"
				+ "	System.out.println(string);\n"
				+ "}" +
				"";
		String expected = "" +
				"public void voidMethod(int i) {\n"
				+ "	doSomething(\"whatever\");\n"
				+ "	if (i <= 0) {\n"
				+ "		return;\n"
				+ "	}\n"
				+ "	doSomething(\"Should create guard condition with less\");\n"
				+ "	doSomething(\"Should transform\");\n"
				+ "}\n"
				+ "\n"
				+ "private void doSomething(String string) {\n"
				+ "	System.out.println(string);\n"
				+ "}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
