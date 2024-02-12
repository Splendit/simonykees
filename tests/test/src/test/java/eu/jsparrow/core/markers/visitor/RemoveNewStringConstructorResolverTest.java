package eu.jsparrow.core.markers.visitor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class RemoveNewStringConstructorResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		RemoveNewStringConstructorResolver visitor = new RemoveNewStringConstructorResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveNewStringConstructorResolver"));
		setVisitor(visitor);
		String original = ""
				+  "System.out.println(new String(\"StringLiteral\"));";
		
		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		RemoveNewStringConstructorResolver visitor = new RemoveNewStringConstructorResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveNewStringConstructorResolver"));
		setVisitor(visitor);
		String original = ""
				+  "System.out.println(new String(\"StringLiteral\"));";
		String expected = ""
				+ "System.out.println(\"StringLiteral\");";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Removes all class instantiations of String if the constructor parameter is empty or a String itself.\n"
				+ "\n"
				+ "For example 'new String(\"StringLiteral\")' becomes '\"StringLiteral\"'. This improves performance and readability.";
		assertAll(
				() -> assertEquals("Use String Literals", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("RemoveNewStringConstructorResolver", event.getResolver()),
				() -> assertEquals("\"StringLiteral\"", event.getCodePreview()),
				() -> assertEquals(15, event.getHighlightLength()),
				() -> assertEquals(108, event.getOffset()),
				() -> assertEquals(27, event.getLength()),
				() -> assertEquals(6, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		RemoveNewStringConstructorResolver visitor = new RemoveNewStringConstructorResolver(node -> node.getStartPosition() == 108);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveNewStringConstructorResolver"));
		setVisitor(visitor);
		String original = ""
				+  "System.out.println(new String(\"StringLiteral\"));";
		String expected = ""
				+ "System.out.println(\"StringLiteral\");";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
