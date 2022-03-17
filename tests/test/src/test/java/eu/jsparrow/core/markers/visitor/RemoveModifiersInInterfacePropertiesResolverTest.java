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

class RemoveModifiersInInterfacePropertiesResolverTest extends UsesJDTUnitFixture {
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
		RemoveModifiersInInterfacePropertiesResolver visitor = new RemoveModifiersInInterfacePropertiesResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveModifiersInInterfacePropertiesResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "interface RemoveModifiersInInterfaceProperties {\n"
				+ "\n"
				+ "	public String FIELD_WITH_PUBLIC_MODIFIER = \"\";\n"
				+ "	public void publicMethod();\n"
				+ "}"
				+ "";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		RemoveModifiersInInterfacePropertiesResolver visitor = new RemoveModifiersInInterfacePropertiesResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveModifiersInInterfacePropertiesResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "interface RemoveModifiersInInterfaceProperties {\n"
				+ "\n"
				+ "	public String FIELD_WITH_PUBLIC_MODIFIER = \"\";\n"
				+ "	public void publicMethod();\n"
				+ "}"
				+ "";
		String expected = ""
				+ "interface RemoveModifiersInInterfaceProperties {\n"
				+ "\n"
				+ "	String FIELD_WITH_PUBLIC_MODIFIER = \"\";\n"
				+ "	void publicMethod();\n"
				+ "}"
				+ "";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(2, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Removes the 'public' modifiers from method declarations and "
				+ "'public static final' modifiers from field declarations in interfaces.";
		
		assertAll(
				() -> assertEquals("Remove Modifiers from Interface Properties", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("RemoveModifiersInInterfacePropertiesResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(102, event.getOffset()),
				() -> assertEquals(6, event.getLength()),
				() -> assertEquals(6, event.getLineNumber()),
				() -> assertEquals(1, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		RemoveModifiersInInterfacePropertiesResolver visitor = new RemoveModifiersInInterfacePropertiesResolver(node -> node.getStartPosition() == 102);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveModifiersInInterfacePropertiesResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "interface RemoveModifiersInInterfaceProperties {\n"
				+ "\n"
				+ "	public String FIELD_WITH_PUBLIC_MODIFIER = \"\";\n"
				+ "	public void publicMethod();\n"
				+ "}"
				+ "";
		String expected = ""
				+ "interface RemoveModifiersInInterfaceProperties {\n"
				+ "\n"
				+ "	String FIELD_WITH_PUBLIC_MODIFIER = \"\";\n"
				+ "	public void publicMethod();\n"
				+ "}"
				+ "";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
