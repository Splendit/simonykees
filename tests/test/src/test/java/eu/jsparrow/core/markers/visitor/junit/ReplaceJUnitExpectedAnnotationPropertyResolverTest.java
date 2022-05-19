package eu.jsparrow.core.markers.visitor.junit;

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

class ReplaceJUnitExpectedAnnotationPropertyResolverTest extends UsesJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		RefactoringMarkers.clear();
		addDependency("junit", "junit", "4.13");
		addDependency("org.hamcrest", "hamcrest-library", "1.3");
		addDependency("org.hamcrest", "hamcrest-core", "1.3");

		defaultFixture.addImport("org.junit.Rule");
		defaultFixture.addImport("org.junit.Test");
		defaultFixture.addImport("org.junit.rules.ExpectedException");
		defaultFixture.addImport("java.io.IOException");
		defaultFixture.addImport("org.hamcrest.Matcher");
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		ReplaceJUnitExpectedAnnotationPropertyResolver visitor = new ReplaceJUnitExpectedAnnotationPropertyResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceJUnitExpectedAnnotationPropertyResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test(expected = IOException.class)\n"
				+ "public void methodInvocation() throws IOException {\n"
				+ "		throwIOException();\n"
				+ "}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		ReplaceJUnitExpectedAnnotationPropertyResolver visitor = new ReplaceJUnitExpectedAnnotationPropertyResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceJUnitExpectedAnnotationPropertyResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test(expected = IOException.class)\n"
				+ "public void methodInvocation() throws IOException {\n"
				+ "		throwIOException();\n"
				+ "}";
		String expected = ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void methodInvocation() {\n"
				+ "		assertThrows(IOException.class, () -> throwIOException());\n"
				+ "}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Using the 'expected' annotation property for testing the thrown exceptions is "
				+ "rather misleading. Often it becomes unclear which part of the test code is responsible "
				+ "for throwing the exception. This rule aims to overcome this problem by replacing the "
				+ "'expected' annotation property with 'assertThrows()' introduced in JUnit 4.13.";

		assertAll(
				() -> assertEquals("Replace JUnit Expected Annotation Property with assertThrows", event.getName()),
				() -> assertEquals(description, event.getMessage()),
				() -> assertEquals("ReplaceJUnitExpectedAnnotationPropertyResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(254, event.getOffset()),
				() -> assertEquals(35, event.getLength()),
				() -> assertEquals(13, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		ReplaceJUnitExpectedAnnotationPropertyResolver visitor = new ReplaceJUnitExpectedAnnotationPropertyResolver(node -> node.getStartPosition() == 254);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceJUnitExpectedAnnotationPropertyResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test(expected = IOException.class)\n"
				+ "public void methodInvocation() throws IOException {\n"
				+ "		throwIOException();\n"
				+ "}";
		String expected = ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void methodInvocation() {\n"
				+ "		assertThrows(IOException.class, () -> throwIOException());\n"
				+ "}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

}