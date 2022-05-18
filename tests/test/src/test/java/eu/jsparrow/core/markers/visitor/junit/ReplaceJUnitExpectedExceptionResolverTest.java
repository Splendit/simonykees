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

class ReplaceJUnitExpectedExceptionResolverTest extends UsesJDTUnitFixture {

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
		ReplaceJUnitExpectedExceptionResolver visitor = new ReplaceJUnitExpectedExceptionResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceJUnitExpectedExceptionResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void methodInvocation() throws IOException {\n"
				+ "		expectedException.expect(IOException.class);\n"
				+ "		throwIOException();\n"
				+ "}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		ReplaceJUnitExpectedExceptionResolver visitor = new ReplaceJUnitExpectedExceptionResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceJUnitExpectedExceptionResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void methodInvocation() throws IOException {\n"
				+ "		expectedException.expect(IOException.class);\n"
				+ "		throwIOException();\n"
				+ "}";
		String expected = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();"
				+ ""
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
				+ "The 'ExpectedException.none()' rule is deprecated since JUnit 4.13. The recommended alternative "
				+ "is to use 'assertThrows()'. This makes JUnit tests easier to understand and prevents scenarios "
				+ "where some parts of the test code are unreachable. \n"
				+ "The goal of this rule is to replace 'expectedException.expect()' with 'assertThrows()'. "
				+ "Additionally, new assertions are added for each invocation of 'expectMessage()' and 'expectCause()'.";

		assertAll(
				() -> assertEquals("Replace JUnit ExpectedException with assertThrows", event.getName()),
				() -> assertEquals(description, event.getMessage()),
				() -> assertEquals("ReplaceJUnitExpectedExceptionResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(462, event.getOffset()),
				() -> assertEquals(18, event.getLength()),
				() -> assertEquals(18, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		ReplaceJUnitExpectedExceptionResolver visitor = new ReplaceJUnitExpectedExceptionResolver(
				node -> node.getStartPosition() == 462);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceJUnitExpectedExceptionResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();"
				+ ""
				+ "private void throwIOException() throws IOException {}"
				+ ""
				+ "@Test\n"
				+ "public void methodInvocation() throws IOException {\n"
				+ "		expectedException.expect(IOException.class);\n"
				+ "		throwIOException();\n"
				+ "}";
		String expected = ""
				+ "@Rule\n"
				+ "public ExpectedException expectedException = ExpectedException.none();"
				+ ""
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