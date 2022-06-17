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

class ReplaceJUnitTimeoutAnnotationPropertyResolverTest extends UsesJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		RefactoringMarkers.clear();
		addDependency("org.junit.jupiter", "junit-jupiter-api", "5.0.0");
		addDependency("junit", "junit", "4.13");

		defaultFixture.addImport("java.lang.Thread");
		defaultFixture.addImport("java.time.Duration");
		defaultFixture.addImport("org.junit.Test");
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		ReplaceJUnitTimeoutAnnotationPropertyResolver visitor = new ReplaceJUnitTimeoutAnnotationPropertyResolver(
				node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceJUnitTimeoutAnnotationPropertyResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "@Test(timeout = 1)\n"
				+ "public void methodInvocation() {\n"
				+ "		Thread.sleep(500);\n"
				+ "}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		ReplaceJUnitTimeoutAnnotationPropertyResolver visitor = new ReplaceJUnitTimeoutAnnotationPropertyResolver(
				node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceJUnitTimeoutAnnotationPropertyResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "@Test(timeout = 1)\n"
				+ "public void methodInvocation() {\n"
				+ "		Thread.sleep(500);\n"
				+ "}";
		String expected = ""
				+ "@Test\n"
				+ "public void methodInvocation() {\n"
				+ "		assertTimeout(ofMillis(1), () -> Thread.sleep(500));\n"
				+ "}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "JUnit Jupiter API provides timeout assertions, i.e., assertions that execution of some code completes "
				+ "before a timeout exceeds. In JUnit 4 this is achieved by using the 'timeout' property of '@Test(timeout=...)' annotation. \n"
				+ "This rule removes the 'timeout' annotation property and inserts an  'assertTimeout' instead.";

		assertAll(
				() -> assertEquals("Replace JUnit Timeout Annotation Property with assertTimeout", event.getName()),
				() -> assertEquals(description, event.getMessage()),
				() -> assertEquals("ReplaceJUnitTimeoutAnnotationPropertyResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(121, event.getOffset()),
				() -> assertEquals(18, event.getLength()),
				() -> assertEquals(9, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		ReplaceJUnitTimeoutAnnotationPropertyResolver visitor = new ReplaceJUnitTimeoutAnnotationPropertyResolver(
				node -> node.getStartPosition() == 121);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceJUnitTimeoutAnnotationPropertyResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "@Test(timeout = 1)\n"
				+ "public void methodInvocation() {\n"
				+ "		Thread.sleep(500);\n"
				+ "}";
		String expected = ""
				+ "@Test\n"
				+ "public void methodInvocation() {\n"
				+ "		assertTimeout(ofMillis(1), () -> Thread.sleep(500));\n"
				+ "}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}