package org.eu.jsparrow.rules.java16.switchexpression.ifstatement;

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
import eu.jsparrow.rules.java16.switchexpression.ifstatement.ReplaceMultiBranchIfBySwitchResolver;

class ReplaceMultiBranchIfBySwitchResolverTest extends UsesJDTUnitFixture {

	private static final String STRING_CONSTANTS = ""
			+ "	static final String NEGATIVE = \"NEGATIVE\";\n"
			+ "	static final String ZERO = \"ZERO\";\n"
			+ "	static final String ONE = \"ONE\";\n"
			+ "	static final String TWO = \"TWO\";		\n"
			+ "	static final String GREATER_THAN_TWO = \"GREATER THAN TWO\";\n"
			+ "	static final String OTHER = \"OTHER\";		";

	private static final String INITIALIZATION_WITH_SWITCH = STRING_CONSTANTS + "\n"
			+ "	void example(int value) {\n"
			+ "		String result = switch (value) {\n"
			+ "		case 0 -> ZERO;\n"
			+ "		case 1 -> ONE;\n"
			+ "		case 2 -> TWO;\n"
			+ "		default -> OTHER;\n"
			+ "		};\n"
			+ "	}";

	private static final String MULTI_BRANCH_IF = STRING_CONSTANTS + "\n"
			+ "	void example(int value) {\n"
			+ "		String result;\n"
			+ "		if (value == 0) {\n"
			+ "			result = ZERO;\n"
			+ "		} else if (value == 1) {\n"
			+ "			result = ONE;\n"
			+ "		} else if (value == 2) {\n"
			+ "			result = TWO;\n"
			+ "		} else {\n"
			+ "			result = OTHER;\n"
			+ "		}\n"
			+ "	}";

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	private void addResolver(Predicate<ASTNode> positionChecker) {
		ReplaceMultiBranchIfBySwitchResolver visitor = new ReplaceMultiBranchIfBySwitchResolver(positionChecker);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceMultiBranchIfBySwitchResolver"));
		setDefaultVisitor(visitor);
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		addResolver(node -> false);
		String original = MULTI_BRANCH_IF;

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		addResolver(node -> true);
		String original = MULTI_BRANCH_IF;
		String expected = INITIALIZATION_WITH_SWITCH;

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "In Java 14, the switch expressions turned to a standard feature."
				+ " This rule replaces multi-branch if statements by corresponding switch expressions or switch statements with switch labeled rules."
				+ " Because this rule removes a lot of redundant parts of code, readability is improved.";

		assertAll(
				() -> assertEquals("Replace Multi-Branch If By Switch", event.getName()),
				() -> assertEquals(description, event.getMessage()),
				() -> assertEquals("ReplaceMultiBranchIfBySwitchResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(366, event.getOffset()),
				() -> assertEquals(217, event.getLength()),
				() -> assertEquals(13, event.getLineNumber()),
				() -> assertEquals(15, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		addResolver(node -> node.getStartPosition() == 366);
		String original = MULTI_BRANCH_IF;
		String expected = INITIALIZATION_WITH_SWITCH;

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
