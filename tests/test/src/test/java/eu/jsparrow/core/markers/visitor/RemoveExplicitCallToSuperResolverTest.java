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

class RemoveExplicitCallToSuperResolverTest extends UsesJDTUnitFixture {
	
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
		RemoveExplicitCallToSuperResolver visitor = new RemoveExplicitCallToSuperResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveExplicitCallToSuperResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "class FooBar {\n"
				+ "	public FooBar() {\n"
				+ "		super();\n"
				+ "	}\n"
				+ "}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		RemoveExplicitCallToSuperResolver visitor = new RemoveExplicitCallToSuperResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveExplicitCallToSuperResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "class FooBar {\n"
				+ "	public FooBar() {\n"
				+ "		super();\n"
				+ "	}\n"
				+ "}";
		String expected = ""
				+ "class FooBar {\n"
				+ "	public FooBar() {\n"
				+ "	}\n"
				+ "}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Removes unnecessary explicit call to the default constructor of the super class.";
		
		assertAll(
				() -> assertEquals("Remove Explicit Call To super()", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("RemoveExplicitCallToSuperResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(98, event.getOffset()),
				() -> assertEquals(8, event.getLength()),
				() -> assertEquals(7, event.getLineNumber()),
				() -> assertEquals(1, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		RemoveExplicitCallToSuperResolver visitor = new RemoveExplicitCallToSuperResolver(node -> node.getStartPosition() == 98);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveExplicitCallToSuperResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "class FooBar {\n"
				+ "	public FooBar() {\n"
				+ "		super();\n"
				+ "	}\n"
				+ "}";
		String expected = ""
				+ "class FooBar {\n"
				+ "	public FooBar() {\n"
				+ "	}\n"
				+ "}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
