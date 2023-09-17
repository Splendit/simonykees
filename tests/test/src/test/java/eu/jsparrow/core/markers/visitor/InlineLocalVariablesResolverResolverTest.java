package eu.jsparrow.core.markers.visitor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class InlineLocalVariablesResolverResolverTest extends UsesJDTUnitFixture {

	private static final String RULE_DESCRIPTION = ""
			+ "This rule scans for local variables which are declared and then immediately returned or thrown"
			+ " and in-lines them if this is possible.";

	private static final String RETURN_ORIGINAL = "" +
			"	int returnResult(int a, int b) {\n" +
			"		int result = a + b;\n" +
			"		return result;\n" +
			"	}";
	private static final String RETURN_EXPECTED = "" +
			"	int returnResult(int a, int b) {\n" +
			"		return a + b;\n" +
			"	}";

	private static final String THROW_ORIGINAL = "" +
			"	void throwRuntimeException() {\n" +
			"		RuntimeException runtimeException = new RuntimeException();\n" +
			"		throw runtimeException;\n" +
			"	}";
	private static final String THROW_EXPECTED = "" +
			"	void throwRuntimeException() {\n" +
			"		throw new RuntimeException();\n" +
			"	}";

	@BeforeEach
	void setUpVisitor() throws Exception {
		RefactoringMarkers.clear();
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	private void setResolver(Predicate<ASTNode> positionChecker) {
		InlineLocalVariablesResolver visitor = new InlineLocalVariablesResolver(positionChecker);
		visitor.addMarkerListener(RefactoringMarkers.getFor("InlineLocalVariablesResolver"));
		setDefaultVisitor(visitor);
	}

	@Test
	void test_Return_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		setResolver(node -> false);
		assertNoChange(RETURN_ORIGINAL);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_Return_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		setResolver(node -> true);
		assertChange(RETURN_ORIGINAL, RETURN_EXPECTED);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		assertAll(
				() -> assertEquals("Inline Local Variables", event.getName()),
				() -> assertEquals(RULE_DESCRIPTION, event.getMessage()),
				() -> assertEquals("InlineLocalVariablesResolver", event.getResolver()),
				() -> assertEquals(RULE_DESCRIPTION, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(114, event.getOffset()),
				() -> assertEquals(14, event.getLength()),
				() -> assertEquals(7, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_Return_resolveMarkers_shouldResolveOne() throws Exception {
		setResolver(node -> node.getStartPosition() == 114);
		assertChange(RETURN_ORIGINAL, RETURN_EXPECTED);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

	@Test
	void test_Throw_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		setResolver(node -> false);
		assertNoChange(THROW_ORIGINAL);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_Throw_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		setResolver(node -> true);
		assertChange(THROW_ORIGINAL, THROW_EXPECTED);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		assertAll(
				() -> assertEquals("Inline Local Variables", event.getName()),
				() -> assertEquals(RULE_DESCRIPTION, event.getMessage()),
				() -> assertEquals("InlineLocalVariablesResolver", event.getResolver()),
				() -> assertEquals(RULE_DESCRIPTION, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(152, event.getOffset()),
				() -> assertEquals(23, event.getLength()),
				() -> assertEquals(7, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_Throw_resolveMarkers_shouldResolveOne() throws Exception {
		setResolver(node -> node.getStartPosition() == 152);
		assertChange(THROW_ORIGINAL, THROW_EXPECTED);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
