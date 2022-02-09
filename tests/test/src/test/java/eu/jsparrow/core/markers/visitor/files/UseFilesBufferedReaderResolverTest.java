package eu.jsparrow.core.markers.visitor.files;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class UseFilesBufferedReaderResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		setJavaVersion(JavaCore.VERSION_11);
		fixture.addImport(java.io.File.class.getName());
		fixture.addImport(java.io.FileReader.class.getName());
		fixture.addImport(java.io.BufferedReader.class.getName());
		fixture.addImport(java.io.IOException.class.getName());
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		UseFilesBufferedReaderResolver visitor = new UseFilesBufferedReaderResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseFilesBufferedReaderResolver"));
		setVisitor(visitor);
		String original = "" +
				"try (FileReader reader = new FileReader(new File(\"path/to/file\"));\n" +
				"		BufferedReader br = new BufferedReader(reader)) {\n" +
				"} catch (IOException e) {\n" +
				"	e.printStackTrace();\n" +
				"}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		UseFilesBufferedReaderResolver visitor = new UseFilesBufferedReaderResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseFilesBufferedReaderResolver"));
		setVisitor(visitor);
		String original = "" +
				"try (FileReader reader = new FileReader(new File(\"path/to/file\"));\n" +
				"		BufferedReader br = new BufferedReader(reader)) {\n" +
				"} catch (IOException e) {\n" +
				"	e.printStackTrace();\n" +
				"}";
		String expected = "" +
				"try (BufferedReader br = Files.newBufferedReader(Paths.get(\"path/to/file\"), Charset.defaultCharset())) {\n"
				+
				"} catch (IOException e) {\n" +
				"	e.printStackTrace();\n" +
				"}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Java 7 introduced the 'java.nio.file.Files' class that contains some convenience methods for operating on files. "
				+ "This rule makes use of the 'Files.newBufferedReader' method for initializing 'BufferedReader' objects to read text "
				+ "files in an efficient non-blocking manner.";
		assertAll(
				() -> assertEquals("Use Files.newBufferedReader", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("UseFilesBufferedReaderResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(299, event.getOffset()),
				() -> assertEquals(26, event.getLength()),
				() -> assertEquals(11, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		UseFilesBufferedReaderResolver visitor = new UseFilesBufferedReaderResolver(node -> node.getStartPosition() == 300);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseFilesBufferedReaderResolver"));
		setVisitor(visitor);
		String original = "" +
				"try (FileReader reader = new FileReader(new File(\"path/to/file\"));\n" +
				"		BufferedReader br = new BufferedReader(reader)) {\n" +
				"} catch (IOException e) {\n" +
				"	e.printStackTrace();\n" +
				"}";
		String expected = "" +
				"try (BufferedReader br = Files.newBufferedReader(Paths.get(\"path/to/file\"), Charset.defaultCharset())) {\n"
				+
				"} catch (IOException e) {\n" +
				"	e.printStackTrace();\n" +
				"}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

}
