package eu.jsparrow.core.markers.visitor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class InefficientConstructorResolverTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	void setUp() {
		RefactoringMarkers.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		InefficientConstructorResolver visitor = new InefficientConstructorResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("InefficientConstructorResolver"));
		setVisitor(visitor);
		String original = "Integer i = new Integer(1);";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		InefficientConstructorResolver visitor = new InefficientConstructorResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("InefficientConstructorResolver"));
		setVisitor(visitor);
		String original = "Integer i = new Integer(1);";
		String expected = "Integer i = Integer.valueOf(1);";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		assertAll(
				() -> assertEquals("Replace inefficient constructors with valueOf()", event.getName()),
				() -> assertEquals("The factory method valueOf() is generally a better choice as it is likely to yield significantly better space and time performance.", event.getMessage()), 
				() -> assertEquals("InefficientConstructorResolver", event.getResolver()),
				() -> assertEquals("Integer.valueOf(1)", event.getCodePreview()),
				() -> assertEquals(18, event.getHighlightLength()),
				() -> assertEquals(101, event.getOffset()),
				() -> assertEquals(14, event.getLength()),
				() -> assertEquals(5, event.getWeightValue()));
	}
	
	
	@Test
	void test_markerGeneration2_shouldGenerateNoMarkerEvent() throws Exception {
		InefficientConstructorResolver visitor = new InefficientConstructorResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("InefficientConstructorResolver"));
		setVisitor(visitor);
		String original = "Boolean i = Boolean.valueOf();";
		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		InefficientConstructorResolver visitor = new InefficientConstructorResolver(node -> node.getStartPosition() == 102);
		visitor.addMarkerListener(RefactoringMarkers.getFor("InefficientConstructorResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Integer i1 = new Integer(1);\n"
				+ "Integer i2 = new Integer(2);";
		String expected = ""
				+ "Integer i1 = Integer.valueOf(1);\n"
				+ "Integer i2 = new Integer(2);";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
	
	@Test
	void test_resolveMarkers2_shouldResolveOne() throws Exception {
		InefficientConstructorResolver visitor = new InefficientConstructorResolver(node -> node.getStartPosition() == 121);
		visitor.addMarkerListener(RefactoringMarkers.getFor("InefficientConstructorResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Boolean value = Boolean.valueOf(\"true\");\n"
				+ "Boolean value2 = Boolean.valueOf(\"true\");\n";
		String expected = ""
				+ "Boolean value = Boolean.valueOf(true);\n"
				+ "Boolean value2 = Boolean.valueOf(\"true\");\n";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
