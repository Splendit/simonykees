package eu.jsparrow.core.markers.visitor.security.random;

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

class ReuseRandomObjectsResolverTest extends UsesJDTUnitFixture {

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
		ReuseRandomObjectsResolver visitor = new ReuseRandomObjectsResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReuseRandomObjectsResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"private void sampleMethod() {\n" +
				"	Random random = new Random();\n" +
				"	int iRandom = random.nextInt();\n" +
				"	System.out.println(\"\");\n" +
				"}\n" +
				"";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		ReuseRandomObjectsResolver visitor = new ReuseRandomObjectsResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReuseRandomObjectsResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"private void sampleMethod() {\n" +
				"	Random random = new Random();\n" +
				"	int iRandom = random.nextInt();\n" +
				"	System.out.println(\"\");\n" +
				"}\n" +
				"";
		String expected = "" +
				"private Random random = new Random();\n" +
				"private void sampleMethod() {\n" +
				"	int iRandom = random.nextInt();\n" +
				"	System.out.println(\"\");\n" +
				"}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Creating a new Random object each time a random value is needed is inefficient "
				+ "and may produce numbers which are not random. This rule extracts reusable "
				+ "java.util.Random objects, from local variables to class or instance fields.";
		
		assertAll(
				() -> assertEquals("Reuse Random Objects", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("ReuseRandomObjectsResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(109, event.getOffset()),
				() -> assertEquals(29, event.getLength()),
				() -> assertEquals(8, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		ReuseRandomObjectsResolver visitor = new ReuseRandomObjectsResolver(node -> node.getStartPosition() == 109);
		visitor.addMarkerListener(RefactoringMarkers.getFor("CreateTempFilesUsingJavaNIOResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"private void sampleMethod() {\n" +
				"	Random random = new Random();\n" +
				"	int iRandom = random.nextInt();\n" +
				"	System.out.println(\"\");\n" +
				"}\n" +
				"";
		String expected = "" +
				"private Random random = new Random();\n" +
				"private void sampleMethod() {\n" +
				"	int iRandom = random.nextInt();\n" +
				"	System.out.println(\"\");\n" +
				"}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
