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

class UseFilesBufferedWriterResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		setJavaVersion(JavaCore.VERSION_11);
		fixture.addImport(java.io.File.class.getName());
		fixture.addImport(java.io.FileWriter.class.getName());
		fixture.addImport(java.io.BufferedWriter.class.getName());
		fixture.addImport(java.io.IOException.class.getName());
		RefactoringMarkers.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		UseFilesBufferedWriterResolver visitor = new UseFilesBufferedWriterResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseFilesBufferedWriterResolver"));
		setVisitor(visitor);
		String original = "" +
				"try (FileWriter writer = new FileWriter(new File(\"path/to/file\"));\n" +
				"		BufferedWriter bw = new BufferedWriter(writer)) {\n" +
				"} catch (IOException e) {\n" +
				"}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		UseFilesBufferedWriterResolver visitor = new UseFilesBufferedWriterResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseFilesBufferedWriterResolver"));
		setVisitor(visitor);
		String original = "" +
				"try (FileWriter writer = new FileWriter(new File(\"path/to/file\"));\n" +
				"		BufferedWriter bw = new BufferedWriter(writer)) {\n" +
				"} catch (IOException e) {\n" +
				"}";
		String expected = "" +
				"try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(\"path/to/file\"), Charset.defaultCharset())) {\n"
				+
				"} catch (IOException e) {\n" +
				"}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Java 7 introduced the 'java.nio.file.Files' class that contains some convenience methods for operating on files. "
				+ "This rule makes use of the 'Files.newBufferedWriter' method for initializing 'BufferedWriter' objects to write "
				+ "text files in an efficient non-blocking manner.";
		assertAll(
				() -> assertEquals("Use Files.newBufferedWriter", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("UseFilesBufferedWriterResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(299, event.getOffset()),
				() -> assertEquals(26, event.getLength()),
				() -> assertEquals(11, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		UseFilesBufferedWriterResolver visitor = new UseFilesBufferedWriterResolver(node -> node.getStartPosition() == 300);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseFilesBufferedWriterResolver"));
		setVisitor(visitor);
		String original = "" +
				"try (FileWriter writer = new FileWriter(new File(\"path/to/file\"));\n" +
				"		BufferedWriter bw = new BufferedWriter(writer)) {\n" +
				"} catch (IOException e) {\n" +
				"}";
		String expected = "" +
				"try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(\"path/to/file\"), Charset.defaultCharset())) {\n"
				+
				"} catch (IOException e) {\n" +
				"}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
