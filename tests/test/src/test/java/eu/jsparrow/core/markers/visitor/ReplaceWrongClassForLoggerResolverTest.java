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
		addDependency("org.slf4j", "slf4j-api", "1.7.25");
		defaultFixture.addImport("org.slf4j.Logger");
		defaultFixture.addImport("org.slf4j.LoggerFactory");

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
				+ "		static final Logger logger = LoggerFactory.getLogger(Object.class);\n"
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
				+ "		static final Logger logger = LoggerFactory.getLogger(Object.class);\n"
				+ "	}";

		String expected = ""
				+ "	static class Employee {\n"
				+ "		static final Logger logger = LoggerFactory.getLogger(Employee.class);\n"
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
				() -> assertEquals(188, event.getOffset()),
				() -> assertEquals(12, event.getLength()),
				() -> assertEquals(9, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_PredicateOnStartPosition_shouldResolveOne() throws Exception {
		ReplaceWrongClassForLoggerResolver visitor = new ReplaceWrongClassForLoggerResolver(
				node -> node.getStartPosition() == 188);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceWrongClassForLoggerResolver"));
		setDefaultVisitor(visitor);

		String original = ""
				+ "	static class Employee {\n"
				+ "		static final Logger logger = LoggerFactory.getLogger(Object.class);\n"
				+ "	}";

		String expected = ""
				+ "	static class Employee {\n"
				+ "		static final Logger logger = LoggerFactory.getLogger(Employee.class);\n"
				+ "	}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

	@Test
	void test_TwoClassNameArgumentsToReplace_shouldResolveFirst() throws Exception {
		ReplaceWrongClassForLoggerResolver visitor = new ReplaceWrongClassForLoggerResolver(
				node -> node.getStartPosition() == 190);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceWrongClassForLoggerResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "	static class FirstClass {\n"
				+ "		static final Logger LOGGER = LoggerFactory.getLogger(Object.class);\n"
				+ "	}\n"
				+ "\n"
				+ "	static class SecondClass {\n"
				+ "		static final Logger LOGGER = LoggerFactory.getLogger(Object.class);\n"
				+ "	}";
		String expected = ""
				+ "	static class FirstClass {\n"
				+ "		static final Logger LOGGER = LoggerFactory.getLogger(FirstClass.class);\n"
				+ "	}\n"
				+ "\n"
				+ "	static class SecondClass {\n"
				+ "		static final Logger LOGGER = LoggerFactory.getLogger(Object.class);\n"
				+ "	}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		assertEquals(190, events.get(0)
			.getOffset());
	}

	@Test
	void test_TwoClassNameArgumentsToReplace_shouldResolveSecond() throws Exception {
		ReplaceWrongClassForLoggerResolver visitor = new ReplaceWrongClassForLoggerResolver(
				node -> node.getStartPosition() == 303);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceWrongClassForLoggerResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "	static class FirstClass {\n"
				+ "		static final Logger LOGGER = LoggerFactory.getLogger(Object.class);\n"
				+ "	}\n"
				+ "\n"
				+ "	static class SecondClass {\n"
				+ "		static final Logger LOGGER = LoggerFactory.getLogger(Object.class);\n"
				+ "	}";
		String expected = ""
				+ "	static class FirstClass {\n"
				+ "		static final Logger LOGGER = LoggerFactory.getLogger(Object.class);\n"
				+ "	}\n"
				+ "\n"
				+ "	static class SecondClass {\n"
				+ "		static final Logger LOGGER = LoggerFactory.getLogger(SecondClass.class);\n"
				+ "	}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		assertEquals(303, events.get(0)
			.getOffset());

	}
}
