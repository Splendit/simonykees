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

class AvoidConcatenationInLoggingStatementsResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		addDependency("org.slf4j", "slf4j-api", "1.7.25");
		fixture.addImport("org.slf4j.Logger");
		fixture.addImport("org.slf4j.LoggerFactory");
		fixture.addImport("java.math.BigDecimal");
		fixture.addImport("java.time.Instant");
		RefactoringMarkers.clear();
	}
	
	String original = "" +
			"final Logger logger = LoggerFactory.getLogger(getClass().getName());\n" +
			"logger.info(\"s: \" + 5 + \" i: \" + 6 + \" bd: \" + 7 + \" c: \" + 8);";
	String expected = "" +
			"final Logger logger = LoggerFactory.getLogger(getClass().getName());" +
			"logger.info(\"s: {} i: {} bd: {} c: {}\", 5, 6, 7, 8);";
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		AvoidConcatenationInLoggingStatementsResolver visitor = new AvoidConcatenationInLoggingStatementsResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.AvoidConcatenationInLoggingStatementsResolver"));
		setVisitor(visitor);
		String original = "" +
				"final Logger logger = LoggerFactory.getLogger(getClass().getName());\n" +
				"logger.info(\"s: \" + 5 + \" i: \" + 6 + \" bd: \" + 7 + \" c: \" + 8);";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		AvoidConcatenationInLoggingStatementsResolver visitor = new AvoidConcatenationInLoggingStatementsResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.UseCollectionsSingletonListResolver"));
		setVisitor(visitor);
		String original = "" +
				"final Logger logger = LoggerFactory.getLogger(getClass().getName());\n" +
				"logger.info(\"s: \" + 5 + \" i: \" + 6 + \" bd: \" + 7 + \" c: \" + 8);";
		String expected = "" +
				"final Logger logger = LoggerFactory.getLogger(getClass().getName());" +
				"logger.info(\"s: {} i: {} bd: {} c: {}\", 5, 6, 7, 8);";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Avoid always evaluating concatenated logging messages by introducing parameters, which only evaluate when the logging level is active.";
		assertAll(
				() -> assertEquals("Avoid Concatenation in Logging Statements", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("eu.jsparrow.core.markers.visitor.AvoidConcatenationInLoggingStatementsResolver", event.getResolver()),
				() -> assertEquals("\"s: {} i: {} bd: {} c: {}\"", event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(278, event.getOffset()),
				() -> assertEquals(62, event.getLength()),
				() -> assertEquals(5, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		AvoidConcatenationInLoggingStatementsResolver visitor = new AvoidConcatenationInLoggingStatementsResolver(node -> node.getStartPosition() == 279);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.AvoidConcatenationInLoggingStatementsResolver"));
		setVisitor(visitor);
		String original = "" +
				"final Logger logger = LoggerFactory.getLogger(getClass().getName());\n" +
				"logger.info(\"s: \" + 5 + \" i: \" + 6 + \" bd: \" + 7 + \" c: \" + 8);";
		String expected = "" +
				"final Logger logger = LoggerFactory.getLogger(getClass().getName());" +
				"logger.info(\"s: {} i: {} bd: {} c: {}\", 5, 6, 7, 8);";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}