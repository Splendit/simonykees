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

class FunctionalInterfaceResolverTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	void setUp() {
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		FunctionalInterfaceResolver visitor = new FunctionalInterfaceResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("FunctionalInterfaceResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Runnable r = new Runnable() {\n"
				+ "	@Override\n"
				+ "	public void run() {\n"
				+ "		System.out.println();\n"
				+ "	}\n"
				+ "};";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		FunctionalInterfaceResolver visitor = new FunctionalInterfaceResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("FunctionalInterfaceResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Runnable r = new Runnable() {\n"
				+ "	@Override\n"
				+ "	public void run() {\n"
				+ "		System.out.println();\n"
				+ "	}\n"
				+ "};";
		String expected = ""
				+ "Runnable r = () -> {\n"
				+ "		System.out.println();\n"
				+ "};";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		assertAll(
				() -> assertEquals("Replace with Lambda Expression", event.getName()),
				() -> assertEquals("Anonymous class can be replaced by lambda expression", event.getMessage()), 
				() -> assertEquals("FunctionalInterfaceResolver", event.getResolver()),
				() -> assertEquals("() -> {\n  System.out.println();\n}\n", event.getCodePreview()),
				() -> assertEquals(34, event.getHighlightLength()),
				() -> assertEquals(102, event.getOffset()),
				() -> assertEquals(132, event.getLength()),
				() -> assertEquals(5, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		FunctionalInterfaceResolver visitor = new FunctionalInterfaceResolver(node -> node.getStartPosition() == 102);
		visitor.addMarkerListener(RefactoringMarkers.getFor("FunctionalInterfaceResolver"));
		setVisitor(visitor);
		String original = ""
				+ "Runnable r = new Runnable() {\n"
				+ "	@Override\n"
				+ "	public void run() {\n"
				+ "		System.out.println();\n"
				+ "	}\n"
				+ "};\n"
				+ "Runnable r2 = new Runnable() {\n"
				+ "	@Override\n"
				+ "	public void run() {\n"
				+ "		System.out.println();\n"
				+ "	}\n"
				+ "};";
		String expected = ""
				+ "Runnable r = () -> {\n"
				+ "		System.out.println();\n"
				+ "};\n"
				+ "Runnable r2 = new Runnable() {\n"
				+ "	@Override\n"
				+ "	public void run() {\n"
				+ "		System.out.println();\n"
				+ "	}\n"
				+ "};";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
