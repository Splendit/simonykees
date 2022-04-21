package eu.jsparrow.core.markers.visitor;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class StatementLambdaToExpressionResolverTest extends UsesSimpleJDTUnitFixture {

	private String blockTemplate = "new ArrayList<>().forEach(element -> %s);";

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
		fixture.addImport("java.util.ArrayList");
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		StatementLambdaToExpressionResolver visitor = new StatementLambdaToExpressionResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("StatementLambdaToExpressionResolver"));
		setVisitor(visitor);
		String original = String.format(blockTemplate, "{ new String();}");

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		StatementLambdaToExpressionResolver visitor = new StatementLambdaToExpressionResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("StatementLambdaToExpressionResolver"));
		setVisitor(visitor);
		String original = String.format(blockTemplate, "{ new String();}");
		String expected = String.format(blockTemplate, "new String()");
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "If the body of a Lambda statement contains only a single expression braces are optional. It can be reduced to a lambda expression by removing the braces.\n"
				+ "\n"
				+ "For example 'stream.map(x -> { return x*2 })' is replaced by 'stream.map(x -> return x*2)'.\n"
				+ "\n"
				+ "This makes the code more readable and more concise.";
		assertAll(
				() -> assertEquals("Remove Lambda Expression Braces", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("StatementLambdaToExpressionResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(143, event.getOffset()),
				() -> assertEquals(48, event.getLength()),
				() -> assertEquals(7, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		StatementLambdaToExpressionResolver visitor = new StatementLambdaToExpressionResolver(node -> node.getStartPosition() == 144);
		visitor.addMarkerListener(RefactoringMarkers.getFor("StatementLambdaToExpressionResolver"));
		setVisitor(visitor);
		String original = String.format(blockTemplate, "{ new String();}");
		String expected = String.format(blockTemplate, "new String()");
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
