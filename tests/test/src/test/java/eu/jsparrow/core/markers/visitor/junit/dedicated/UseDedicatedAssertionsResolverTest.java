package eu.jsparrow.core.markers.visitor.junit.dedicated;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class UseDedicatedAssertionsResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		RefactoringMarkers.clear();
		addDependency("junit", "junit", "4.13");
		addDependency("org.junit.jupiter", "junit-jupiter-api", "5.0.0");
		fixture.addImport("org.junit.Assert.assertTrue", true, false);
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		UseDedicatedAssertionsResolver visitor = new UseDedicatedAssertionsResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseDedicatedAssertionsResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Object a = new Object();\n"
				+ "Object b = a;\n"
				+ "assertTrue(a.equals(b));";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		UseDedicatedAssertionsResolver visitor = new UseDedicatedAssertionsResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseDedicatedAssertionsResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Object a = new Object();\n"
				+ "Object b = a;\n"
				+ "assertTrue(a.equals(b));";
		String expected = ""
				+ "Object a = new Object();\n"
				+ "Object b = a;\n"
				+ "assertEquals(a, b);";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Replaces boolean assertions (e.g., 'assertTrue' and 'assertFalse') with the corresponding "
				+ "dedicated assertions when testing for equality or null values. \n"
				+ "For example, 'assertTrue(a.equals(b))' can be replaced by 'assertEquals(a, b)'. Similarly, "
				+ "'assertSame', 'assertNotSame', 'assertNull', or 'assertNotNull' can be used instead of 'assertTrue' or 'assertFalse'.";

		assertAll(
				() -> assertEquals("Use Dedicated Assertions", event.getName()),
				() -> assertEquals(description, event.getMessage()),
				() -> assertEquals("UseDedicatedAssertionsResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(187, event.getOffset()),
				() -> assertEquals(23, event.getLength()),
				() -> assertEquals(9, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		UseDedicatedAssertionsResolver visitor = new UseDedicatedAssertionsResolver(node -> node.getStartPosition() == 188);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseDedicatedAssertionsResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Object a = new Object();\n"
				+ "Object b = a;\n"
				+ "assertTrue(a.equals(b));";
		String expected = ""
				+ "Object a = new Object();\n"
				+ "Object b = a;\n"
				+ "assertEquals(a, b);";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

}
