package eu.jsparrow.core.markers.visitor.security.random;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class UseSecureRandomResolverTest extends UsesJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		defaultFixture.addImport(java.util.Random.class.getName());
		RefactoringMarkers.clear();
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		UseSecureRandomResolver visitor = new UseSecureRandomResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseSecureRandomResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"	void test() {\n" +
				"		Random random = new Random();\n" +
				"	}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		UseSecureRandomResolver visitor = new UseSecureRandomResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseSecureRandomResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"	void test() {\n" +
				"		Random random = new Random();\n" +
				"	}";

		String expected = "" +
				"	void test() {\n" +
				"		Random random = new SecureRandom();\n" +
				"	}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "The java.util.Random class relies on a pseudo-random number generator. "
				+ "A more secure alternative is java.security.SecureRandom class which relies "
				+ "on a cryptographically strong random number generator (RNG). This rule "
				+ "changes the initializations of java.util.Random objects to use java.security.SecureRandom values.";
		
		assertAll(
				() -> assertEquals("Use SecureRandom", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("UseSecureRandomResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(109, event.getOffset()),
				() -> assertEquals(12, event.getLength()),
				() -> assertEquals(8, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		UseSecureRandomResolver visitor = new UseSecureRandomResolver(node -> node.getStartPosition() == 109);
		visitor.addMarkerListener(RefactoringMarkers.getFor("CreateTempFilesUsingJavaNIOResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"	void test() {\n" +
				"		Random random = new Random();\n" +
				"	}";

		String expected = "" +
				"	void test() {\n" +
				"		Random random = new SecureRandom();\n" +
				"	}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
