package eu.jsparrow.core.markers.visitor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class PrimitiveBoxedForStringResolverTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	void setUp() {
		RefactoringMarkers.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		PrimitiveBoxedForStringResolver visitor = new PrimitiveBoxedForStringResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.PrimitiveBoxedForStringResolver"));
		setVisitor(visitor);
		String original = "String myValue = new Integer(4).toString();";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		PrimitiveBoxedForStringResolver visitor = new PrimitiveBoxedForStringResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.PrimitiveBoxedForStringResolver"));
		setVisitor(visitor);
		String original = "String myValue = new Integer(4).toString();";
		String expected = "String myValue = Integer.toString(4);";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		assertAll(
				() -> assertEquals("Remove boxing for String conversions", event.getName()),
				() -> assertEquals("Avoid constructing boxed primitives by using the factory method toString", event.getMessage()), 
				() -> assertEquals("eu.jsparrow.core.markers.visitor.PrimitiveBoxedForStringResolver", event.getResolver()),
				() -> assertEquals("Integer.toString(4)", event.getDescription()),
				() -> assertEquals(19, event.getHighlightLength()),
				() -> assertEquals(106, event.getOffset()),
				() -> assertEquals(25, event.getLength()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		PrimitiveBoxedForStringResolver visitor = new PrimitiveBoxedForStringResolver(node -> node.getStartPosition() == 106);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.PrimitiveBoxedForStringResolver"));
		setVisitor(visitor);
		String original = ""
				+ "String myValue = new Integer(4).toString();\n"
				+ "String myValue2 = new Integer(5).toString();";
		String expected = ""
				+ "String myValue = Integer.toString(4);\n"
				+ "String myValue2 = new Integer(5).toString();";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
	
	@Test
	void test_resolveMarkers2_shouldResolveOne() throws Exception {
		PrimitiveBoxedForStringResolver visitor = new PrimitiveBoxedForStringResolver(node -> node.getStartPosition() == 106);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.PrimitiveBoxedForStringResolver"));
		setVisitor(visitor);
		String original = ""
				+ "String myValue = \"\" + 5;\n"
				+ "String myValue2 = \"\" + 6;";
		String expected = ""
				+ "String myValue = Integer.toString(5);\n"
				+ "String myValue2 = \"\" + 6;";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		assertEquals(0, event.getHighlightLength());
	}
}
