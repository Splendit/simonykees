package eu.jsparrow.core.markers.visitor.security;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class CreateTempFilesUsingJavaNIOResolverTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	void setUpVisitor() throws Exception {
		fixture.addImport(java.io.File.class.getName());
		RefactoringMarkers.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		CreateTempFilesUsingJavaNIOResolver visitor = new CreateTempFilesUsingJavaNIOResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("CreateTempFilesUsingJavaNIOResolver"));
		setVisitor(visitor);
		String original = "" +
				"try {\n" +
				"	File file = File.createTempFile(\"prefix\", \"suffix\");\n" +
				"} catch (Exception e) {\n" +
				"}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		CreateTempFilesUsingJavaNIOResolver visitor = new CreateTempFilesUsingJavaNIOResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("CreateTempFilesUsingJavaNIOResolver"));
		setVisitor(visitor);
		String original = "" +
				"try {\n" +
				"	File file = File.createTempFile(\"prefix\", \"suffix\");\n" +
				"} catch (Exception e) {\n" +
				"}";
		String expected = "" +
				"try {\n" +
				"	File file = Files.createTempFile(\"prefix\", \"suffix\").toFile();\n" +
				"} catch (Exception e) {\n" +
				"}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "According to the documentation of 'File.createTempFile(String, String)', a suitable alternative for "
				+ "creating temporary files in security-sensitive applications is to use "
				+ "'java.nio.file.Files.createTempFile(String, String, FileAttribute<?>...)'. The reason behind it is "
				+ "that files created by the latter have more restrictive access permissions.\n"
				+ "\n"
				+ "This rule replaces the temporary file creation using 'java.io.File' by the alternative "
				+ "methods defined in 'java.nio.file.Files'.";
		assertAll(
				() -> assertEquals("Create Temp Files Using Java NIO", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("CreateTempFilesUsingJavaNIOResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(140, event.getOffset()),
				() -> assertEquals(39, event.getLength()),
				() -> assertEquals(8, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		CreateTempFilesUsingJavaNIOResolver visitor = new CreateTempFilesUsingJavaNIOResolver(node -> node.getStartPosition() == 141);
		visitor.addMarkerListener(RefactoringMarkers.getFor("CreateTempFilesUsingJavaNIOResolver"));
		setVisitor(visitor);
		String original = "" +
				"try {\n" +
				"	File file = File.createTempFile(\"prefix\", \"suffix\");\n" +
				"} catch (Exception e) {\n" +
				"}";
		String expected = "" +
				"try {\n" +
				"	File file = Files.createTempFile(\"prefix\", \"suffix\").toFile();\n" +
				"} catch (Exception e) {\n" +
				"}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

}
