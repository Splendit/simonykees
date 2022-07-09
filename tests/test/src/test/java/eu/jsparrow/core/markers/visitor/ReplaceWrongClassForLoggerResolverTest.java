package eu.jsparrow.core.markers.visitor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class ReplaceWrongClassForLoggerResolverTest extends UsesJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
		defaultFixture.addImport(java.util.logging.Logger.class.getName());

	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		ReplaceWrongClassForLoggerResolver visitor = new ReplaceWrongClassForLoggerResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceWrongClassForLoggerResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "	static class Employee {\n"
				+ "		static final Logger LOGGER = Logger.getLogger(Object.class.getName());\n"
				+ "	}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_AlwaysTruePredicate_shouldGenerateOneMarkerEvent() throws Exception {
		ReplaceWrongClassForLoggerResolver visitor = new ReplaceWrongClassForLoggerResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceWrongClassForLoggerResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "	static class Employee {\n"
				+ "		static final Logger LOGGER = Logger.getLogger(Object.class.getName());\n"
				+ "	}";

		String expected = ""
				+ "	static class Employee {\n"
				+ "		static final Logger LOGGER = Logger.getLogger(Employee.class.getName());\n"
				+ "	}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "If a given logger is initialized with a class which is different from the class where it is declared,"
				+ " then this rule will replace the wrong initialization argument by the correct one."
				+ " For example, if a logger for the class 'Employee' is initialized with 'User.class',"
				+ " then the argument of the initialization will be replaced by 'Employee.class'.";
		assertAll(
				() -> assertEquals("Replace Wrong Class for Logger", event.getName()),
				() -> assertEquals(description, event.getMessage()),
				() -> assertEquals("ReplaceWrongClassForLoggerResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(157, event.getOffset()),
				() -> assertEquals(12, event.getLength()),
				() -> assertEquals(8, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_PredicateOnStartPosition_shouldResolveOne() throws Exception {
		ReplaceWrongClassForLoggerResolver visitor = new ReplaceWrongClassForLoggerResolver(
				node -> node.getStartPosition() == 157);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceWrongClassForLoggerResolver"));
		setDefaultVisitor(visitor);

		String original = ""
				+ "	static class Employee {\n"
				+ "		static final Logger logger = Logger.getLogger(Object.class.getName());\n"
				+ "	}";

		String expected = ""
				+ "	static class Employee {\n"
				+ "		static final Logger logger = Logger.getLogger(Employee.class.getName());\n"
				+ "	}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

	@Test
	void test_TwoClassNameArgumentsToReplace_shouldResolveFirst() throws Exception {
		ReplaceWrongClassForLoggerResolver visitor = new ReplaceWrongClassForLoggerResolver(
				node -> node.getStartPosition() == 159);

		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceWrongClassForLoggerResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "	static class FirstClass {\n"
				+ "		static final Logger LOGGER = Logger.getLogger(Object.class.getName());\n"
				+ "	}\n"
				+ "\n"
				+ "	static class SecondClass {\n"
				+ "		static final Logger LOGGER = Logger.getLogger(Object.class.getName());\n"
				+ "	}";
		String expected = ""
				+ "	static class FirstClass {\n"
				+ "		static final Logger LOGGER = Logger.getLogger(FirstClass.class.getName());\n"
				+ "	}\n"
				+ "\n"
				+ "	static class SecondClass {\n"
				+ "		static final Logger LOGGER = Logger.getLogger(Object.class.getName());\n"
				+ "	}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		assertEquals(159, events.get(0)
			.getOffset());
	}

	@Test
	void test_TwoClassNameArgumentsToReplace_shouldResolveSecond() throws Exception {
		ReplaceWrongClassForLoggerResolver visitor = new ReplaceWrongClassForLoggerResolver(
				node -> node.getStartPosition() == 275);

		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceWrongClassForLoggerResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "	static class FirstClass {\n"
				+ "		static final Logger LOGGER = Logger.getLogger(Object.class.getName());\n"
				+ "	}\n"
				+ "\n"
				+ "	static class SecondClass {\n"
				+ "		static final Logger LOGGER = Logger.getLogger(Object.class.getName());\n"
				+ "	}";
		String expected = ""
				+ "	static class FirstClass {\n"
				+ "		static final Logger LOGGER = Logger.getLogger(Object.class.getName());\n"
				+ "	}\n"
				+ "\n"
				+ "	static class SecondClass {\n"
				+ "		static final Logger LOGGER = Logger.getLogger(SecondClass.class.getName());\n"
				+ "	}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		assertEquals(275, events.get(0)
			.getOffset());
	}
}
