package eu.jsparrow.core.markers.visitor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class RemoveUnnecessaryThrownExceptionsResolverTest extends UsesJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		RefactoringMarkers.clear();
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		RemoveUnnecessaryThrownExceptionsResolver visitor = new RemoveUnnecessaryThrownExceptionsResolver(
				node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveUnnecessaryThrownExceptionsResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "public void foo() throws Child, Parent {}\n"
				+ "class Child extends Parent {}\n"
				+ "class Parent extends Exception {}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		RemoveUnnecessaryThrownExceptionsResolver visitor = new RemoveUnnecessaryThrownExceptionsResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveUnnecessaryThrownExceptionsResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "public void foo() throws Child, Parent {}\n"
				+ "class Child extends Parent {}\n"
				+ "class Parent extends Exception {}";
		String expected = ""
				+ "public void foo() throws Parent {}\n"
				+ "class Child extends Parent {}\n"
				+ "class Parent extends Exception {}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Removes the following thrown exceptions from the method signature: \n"
				+ "	 1) Exceptions which are subtype of already thrown exceptions. \n"
				+ "	 2) Exceptions that are thrown more than once. \n"
				+ "	 3) Runtime exceptions. ";

		assertAll(
				() -> assertEquals("Remove Unnecessary Thrown Exceptions on Method Signatures", event.getName()),
				() -> assertEquals(description, event.getMessage()),
				() -> assertEquals("RemoveUnnecessaryThrownExceptionsResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(70, event.getOffset()),
				() -> assertEquals(5, event.getLength()),
				() -> assertEquals(5, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@ParameterizedTest
	@MethodSource(value = "unnecessaryThrownExceptionSamples")
	void test_resolveMarkers_shouldResolveOne(String original, String expected, int position) throws Exception {
		RemoveUnnecessaryThrownExceptionsResolver visitor = new RemoveUnnecessaryThrownExceptionsResolver(
				node -> node.getStartPosition() == position);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveUnnecessaryThrownExceptionsResolver"));
		setDefaultVisitor(visitor);
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

	private static Stream<Arguments> unnecessaryThrownExceptionSamples() {
		String original1 = ""
				+ "public void foo() throws Child, Parent {}\n"
				+ "class Child extends Parent {}\n"
				+ "class Parent extends Exception {}";
		String expected1 = ""
				+ "public void foo() throws Parent {}\n"
				+ "class Child extends Parent {}\n"
				+ "class Parent extends Exception {}";
		int position1 = 70;

		String original2 = ""
				+ "public void foo() throws Child, Child {}\n"
				+ "class Child extends Exception {}\n";
		String expected2 = ""
				+ "public void foo() throws Child {}\n"
				+ "class Child extends Exception {}\n";
		int position2 = 70;

		String original3 = ""
				+ "public void foo() throws NullPointerException {}";
		String expected3 = ""
				+ "public void foo() {}\n";
		int position3 = 70;

		return Stream.of(
				Arguments.of(original1, expected1, position1),
				Arguments.of(original2, expected2, position2),
				Arguments.of(original3, expected3, position3));
	}
}
