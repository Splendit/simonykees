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

class LambdaToMethodReferenceResolverTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
		fixture.addImport(java.util.function.Predicate.class.getName());
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		LambdaToMethodReferenceResolver visitor = new LambdaToMethodReferenceResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.LambdaToMethodReferenceResolver"));
		setVisitor(visitor);
		String original = "Predicate<String> isEmpty = (String s) -> s.isEmpty();";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		LambdaToMethodReferenceResolver visitor = new LambdaToMethodReferenceResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.LambdaToMethodReferenceResolver"));
		setVisitor(visitor);
		String original = "Predicate<String> isEmpty = (String s) -> s.isEmpty();";
		String expected = "Predicate<String> isEmpty = String::isEmpty;";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		assertAll(
				() -> assertEquals("Replace lambda expression with method reference", event.getName()),
				() -> assertEquals("Simplify the lambda expression by using a method reference.", event.getMessage()), 
				() -> assertEquals("eu.jsparrow.core.markers.visitor.LambdaToMethodReferenceResolver", event.getResolver()),
				() -> assertEquals("String::isEmpty", event.getCodePreview()),
				() -> assertEquals(15, event.getHighlightLength()),
				() -> assertEquals(154, event.getOffset()),
				() -> assertEquals(25, event.getLength()),
				() -> assertEquals(2, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		LambdaToMethodReferenceResolver visitor = new LambdaToMethodReferenceResolver(node -> node.getStartPosition() == 155);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.LambdaToMethodReferenceResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Predicate<String> isEmpty = (String s) -> s.isEmpty();\n"
				+ "Predicate<String> isEmpty2 = (String s) -> s.isEmpty();";
		String expected = ""
				+ "Predicate<String> isEmpty = String::isEmpty;\n"
				+ "Predicate<String> isEmpty2 = (String s) -> s.isEmpty();";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

}
