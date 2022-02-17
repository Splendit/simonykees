package eu.jsparrow.core.markers.visitor.arithmetic;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class ArithmeticAssignmentResolverTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		ArithmeticAssignmentResolver visitor = new ArithmeticAssignmentResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ArithmeticAssignmentResolver"));
		setVisitor(visitor);
		String original = "int a = 0;  a = a + 3;" ;

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		ArithmeticAssignmentResolver visitor = new ArithmeticAssignmentResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ArithmeticAssignmentResolver"));
		setVisitor(visitor);
		String original =  "int a = 0;  a = a + 3;" ;
		String expected = "int a = 0;  a += 3;";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Assignments involving an arithmetic assignment can be simplified by using a compound operator such as '+=', '-=', '/=' or '*='.\n"
				+ "\n"
				+ "For example, this rule will transform 'a=a+1' into 'a+=1'.\n"
				+ "\n"
				+ "The rule only applies if both operands are primitive types.";
		assertAll(
				() -> assertEquals("Replace Assignment with Compound Operator", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("ArithmeticAssignmentResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(108, event.getOffset()),
				() -> assertEquals(9, event.getLength()),
				() -> assertEquals(7, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		ArithmeticAssignmentResolver visitor = new ArithmeticAssignmentResolver(node -> node.getStartPosition() == 108);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ArithmeticAssignmentResolver"));
		setVisitor(visitor);
		String original = "int a = 0;  a = a + 3;";
		String expected = "int a = 0;  a += 3;";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

}
