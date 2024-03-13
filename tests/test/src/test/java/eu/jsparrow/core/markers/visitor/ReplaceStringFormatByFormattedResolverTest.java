package eu.jsparrow.core.markers.visitor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class ReplaceStringFormatByFormattedResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		ReplaceStringFormatByFormattedResolver visitor = new ReplaceStringFormatByFormattedResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceStringFormatByFormattedResolver"));
		setVisitor(visitor);
		String original = ""
				+ "String output = String.format(\n"
				+ "	    \"Name: %s, Phone: %s, Address: %s, Salary: $%.2f\",\n"
				+ "	    name, phone, address, salary);";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		ReplaceStringFormatByFormattedResolver visitor = new ReplaceStringFormatByFormattedResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceStringFormatByFormattedResolver"));
		setVisitor(visitor);
		String original = ""
				+ "String output = String.format(\n"
				+ "	    \"Name: %s, Phone: %s, Address: %s, Salary: $%.2f\",\n"
				+ "	    name, phone, address, salary);";
		String expected = "" +
				"String output = \"Name: %s, Phone: %s, Address: %s, Salary: $%.2f\".formatted(name, phone, address, salary);";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "This rule replaces the static invocations of String.format(String, Object...) by invocations of the instance "
				+ "method String.formatted(Object...) introduced in Java 15. This way, eliminating some code clutter.";
		assertAll(
				() -> assertEquals("Replace String.format by String.formatted", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("ReplaceStringFormatByFormattedResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(105, event.getOffset()),
				() -> assertEquals(94, event.getLength()),
				() -> assertEquals(6, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		ReplaceStringFormatByFormattedResolver visitor = new ReplaceStringFormatByFormattedResolver(node -> node.getStartPosition() == 105);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceStringFormatByFormattedResolver"));
		setVisitor(visitor);
		String original = ""
				+ "String output = String.format(\n"
				+ "	    \"Name: %s, Phone: %s, Address: %s, Salary: $%.2f\",\n"
				+ "	    name, phone, address, salary);";
		String expected = "" +
				"String output = \"Name: %s, Phone: %s, Address: %s, Salary: $%.2f\".formatted(name, phone, address, salary);";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
