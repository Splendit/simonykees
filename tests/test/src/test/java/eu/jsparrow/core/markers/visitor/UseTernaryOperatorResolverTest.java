package eu.jsparrow.core.markers.visitor;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class UseTernaryOperatorResolverTest extends UsesSimpleJDTUnitFixture {

	private static final String ORIGINAL = ""
			+ "		boolean condition = true;\n"
			+ "		int x;\n"
			+ "		if (condition) {\n"
			+ "			x = 1;\n"
			+ "		} else {\n"
			+ "			x = 0;\n"
			+ "		}";

	private static final String EXPECTED = ""
			+ "		boolean condition = true;\n"
			+ "		int x = condition ? 1 : 0;";

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
	}

	void setVisitor(Predicate<ASTNode> positionChecker) {
		UseTernaryOperatorResolver visitor = new UseTernaryOperatorResolver(positionChecker);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseTernaryOperatorResolver"));
		setVisitor(visitor);
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		setVisitor(node -> false);
		String original = ORIGINAL;
		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		setVisitor(node -> true);
		String original = ORIGINAL;
		String expected = EXPECTED;
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "When possible, this rule transforms an if statement to a ternary operations"
				+ " which is either used as expression of a return statement"
				+ " or as left hand side value for an assignment.";
		assertAll(
				() -> assertEquals("Use Ternary Operator", event.getName()),
				() -> assertEquals(description, event.getMessage()),
				() -> assertEquals("UseTernaryOperatorResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(138, event.getOffset()),
				() -> assertEquals(81, event.getLength()),
				() -> assertEquals(8, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		setVisitor(node -> node.getStartPosition() == 138);
		String original = ORIGINAL;
		String expected = EXPECTED;
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
