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

class StringLiteralEqualityCheckResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() {
		RefactoringMarkers.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		StringLiteralEqualityCheckResolver visitor = new StringLiteralEqualityCheckResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.StringLiteralEqualityCheckResolver"));
		setVisitor(visitor);
		String original = ""
				+ "String value = \"\";\n"
				+ "if(value.equals(\"\")) {\n"
				+ "	System.out.println(\"IsEmpty\");\n"
				+ "}";
		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		StringLiteralEqualityCheckResolver visitor = new StringLiteralEqualityCheckResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.StringLiteralEqualityCheckResolver"));
		setVisitor(visitor);
		String original = ""
				+ "String value = \"\";\n"
				+ "if(value.equals(\"\")) {\n"
				+ "	System.out.println(\"IsEmpty\");\n"
				+ "}";
		String expected = ""
				+ "String value = \"\";\n"
				+ "if(\"\".equals(value)) {\n"
				+ "	System.out.println(\"IsEmpty\");\n"
				+ "}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		assertAll(
				() -> assertEquals("Reorder String equality check", event.getName()),
				() -> assertEquals("To avoid NullPointerExceptions, String literals should be placed on the left side when checking for equality.", event.getMessage()), 
				() -> assertEquals("eu.jsparrow.core.markers.visitor.StringLiteralEqualityCheckResolver", event.getResolver()),
				() -> assertEquals("\"\".equals(value)", event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(133, event.getOffset()),
				() -> assertEquals(2, event.getLength()),
				() -> assertEquals(3, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		StringLiteralEqualityCheckResolver visitor = new StringLiteralEqualityCheckResolver(node -> node.getStartPosition() == 133);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.RemoveNullCheckBeforeInstanceofResolver"));
		setVisitor(visitor);
		String original = ""
				+ "String value = \"\";\n"
				+ "if(value.equals(\"\")) {\n"
				+ "	System.out.println(\"IsEmpty\");\n"
				+ "}"
				+ "if(value.equals(\"\")) {\n"
				+ "	System.out.println(\"IsEmpty\");\n"
				+ "}";
		String expected = ""
				+ "String value = \"\";\n"
				+ "if(\"\".equals(value)) {\n"
				+ "	System.out.println(\"IsEmpty\");\n"
				+ "}"
				+ "if(value.equals(\"\")) {\n"
				+ "	System.out.println(\"IsEmpty\");\n"
				+ "}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

}
