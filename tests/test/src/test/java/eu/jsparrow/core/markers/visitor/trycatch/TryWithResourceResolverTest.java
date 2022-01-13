package eu.jsparrow.core.markers.visitor.trycatch;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class TryWithResourceResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
		fixture.addImport(java.io.BufferedReader.class.getName());
		fixture.addImport(java.io.FileReader.class.getName());
		fixture.addImport(java.io.IOException.class.getName());
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		TryWithResourceResolver visitor = new TryWithResourceResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("TryWithResourceResolver"));
		setVisitor(visitor);
		String original = "" +
				"try {\n"
				+ "    BufferedReader br = new BufferedReader(new FileReader(\"\"));\n"
				+ "    br.readLine();\n"
				+ "    br.readLine();\n"
				+ "    br.close();\n"
				+ "\n"
				+ "} catch (IOException e) {\n"
				+ "    logger.error(e.getMessage(), e);\n"
				+ "}";
		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		TryWithResourceResolver visitor = new TryWithResourceResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("TryWithResourceResolver"));
		setVisitor(visitor);
		String original = "" +
				"try {\n"
				+ "    BufferedReader br = new BufferedReader(new FileReader(\"\"));\n"
				+ "    br.readLine();\n"
				+ "    br.readLine();\n"
				+ "    br.close();\n"
				+ "\n"
				+ "} catch (IOException e) {\n"
				+ "    logger.error(e.getMessage(), e);\n"
				+ "}";
		String expected = ""
				+ "  try (BufferedReader br=new BufferedReader(new FileReader(\"\"))){\n"
				+ "    br.readLine();\n"
				+ "    br.readLine();\n"
				+ "  }\n"
				+ " catch (  IOException e) {\n"
				+ "    logger.error(e.getMessage(),e);\n"
				+ "  }";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "This rule adds the try-with-resources statement introduced in Java 7. Closing statements are "
				+ "removed as the construct takes care of that. Applying this rule makes the code safer and more readable. ";
		assertAll(
				() -> assertEquals("Use Try-With-Resource", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("TryWithResourceResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(175, event.getOffset()),
				() -> assertEquals(244, event.getLength()),
				() -> assertEquals(9, event.getLineNumber()),
				() -> assertEquals(15, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		TryWithResourceResolver visitor = new TryWithResourceResolver(node -> node.getStartPosition() == 176);
		visitor.addMarkerListener(RefactoringMarkers.getFor("TryWithResourceResolver"));
		setVisitor(visitor);
		String original = "" +
				"try {\n"
				+ "    BufferedReader br = new BufferedReader(new FileReader(\"\"));\n"
				+ "    br.readLine();\n"
				+ "    br.readLine();\n"
				+ "    br.close();\n"
				+ "\n"
				+ "} catch (IOException e) {\n"
				+ "    logger.error(e.getMessage(), e);\n"
				+ "}";
		String expected = ""
				+ "  try (BufferedReader br=new BufferedReader(new FileReader(\"\"))){\n"
				+ "    br.readLine();\n"
				+ "    br.readLine();\n"
				+ "  }\n"
				+ " catch (  IOException e) {\n"
				+ "    logger.error(e.getMessage(),e);\n"
				+ "  }";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}