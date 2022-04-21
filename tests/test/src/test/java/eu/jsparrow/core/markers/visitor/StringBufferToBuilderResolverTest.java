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

class StringBufferToBuilderResolverTest extends UsesJDTUnitFixture {

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
		StringBufferToBuilderResolver visitor = new StringBufferToBuilderResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("StringBufferToBuilderResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "String sample() {\n"
				+ "	StringBuffer localStringBuffer = new StringBuffer();\n"
				+ "	localStringBuffer.append(\"foo\");\n"
				+ "	return localStringBuffer.toString();\n"
				+ "}"
				+ "";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		StringBufferToBuilderResolver visitor = new StringBufferToBuilderResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("StringBufferToBuilderResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "String sample() {\n"
				+ "	StringBuffer localStringBuffer = new StringBuffer();\n"
				+ "	localStringBuffer.append(\"foo\");\n"
				+ "	return localStringBuffer.toString();\n"
				+ "}"
				+ "";
		String expected = ""
				+ "String sample() {\n"
				+ "	StringBuilder localStringBuffer = new StringBuilder();\n"
				+ "	localStringBuffer.append(\"foo\");\n"
				+ "	return localStringBuffer.toString();\n"
				+ "}"
				+ "";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "This rule changes the type of local variables from StringBuffer() to StringBuilder().";
		
		assertAll(
				() -> assertEquals("StringBuffer() to StringBuilder()", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("StringBufferToBuilderResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(71, event.getOffset()),
				() -> assertEquals(52, event.getLength()),
				() -> assertEquals(6, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		StringBufferToBuilderResolver visitor = new StringBufferToBuilderResolver(node -> node.getStartPosition() == 71);
		visitor.addMarkerListener(RefactoringMarkers.getFor("StringBufferToBuilderResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "String sample() {\n"
				+ "	StringBuffer localStringBuffer = new StringBuffer();\n"
				+ "	localStringBuffer.append(\"foo\");\n"
				+ "	return localStringBuffer.toString();\n"
				+ "}"
				+ "";
		String expected = ""
				+ "String sample() {\n"
				+ "	StringBuilder localStringBuffer = new StringBuilder();\n"
				+ "	localStringBuffer.append(\"foo\");\n"
				+ "	return localStringBuffer.toString();\n"
				+ "}"
				+ "";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
