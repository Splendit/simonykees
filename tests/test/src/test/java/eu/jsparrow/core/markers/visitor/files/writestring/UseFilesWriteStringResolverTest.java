package eu.jsparrow.core.markers.visitor.files.writestring;

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

class UseFilesWriteStringResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		setJavaVersion(JavaCore.VERSION_11);
		fixture.addImport(java.io.BufferedWriter.class.getName());
		fixture.addImport(java.io.FileWriter.class.getName());
		fixture.addImport(java.nio.charset.Charset.class.getName());
		fixture.addImport(java.nio.charset.StandardCharsets.class.getName());
		
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		UseFilesWriteStringResolver visitor = new UseFilesWriteStringResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseFilesWriteStringResolver"));
		setVisitor(visitor);
		String original = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathString, cs))) {\n" +
				"	bufferedWriter.write(value);\n" +
				"} catch (Exception exception) {\n" +
				"}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		UseFilesWriteStringResolver visitor = new UseFilesWriteStringResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseFilesWriteStringResolver"));
		setVisitor(visitor);
		String original = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathString, cs))) {\n" +
				"	bufferedWriter.write(value);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		String expected = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"try {\n" +
				"	Files.writeString(Paths.get(pathString), value, cs);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Java 11 introduced 'Files.writeString(Path, CharSequence, Charset, OpenOption...)' "
				+ "and 'Files.writeString(Path, CharSequence, OpenOption...)' for writing text into a file "
				+ "by one single invocation and in an efficient non-blocking manner. \n"
				+ "This rule replaces 'BufferedWriters' that are used to write a single value into a file, with 'Files.write(...)'.";
		assertAll(
				() -> assertEquals("Use Files.writeString", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("UseFilesWriteStringResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(357, event.getOffset()),
				() -> assertEquals(181, event.getLength()),
				() -> assertEquals(13, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		UseFilesWriteStringResolver visitor = new UseFilesWriteStringResolver(node -> node.getStartPosition() == 357);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseFilesWriteStringResolver"));
		setVisitor(visitor);
		String original = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathString, cs))) {\n" +
				"	bufferedWriter.write(value);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		String expected = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"try {\n" +
				"	Files.writeString(Paths.get(pathString), value, cs);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
	
	@Test
	void test_multipleBuffereedWriters_shouldResolveOne() throws Exception {
		fixture.addImport(java.nio.file.Paths.class.getName());
		fixture.addImport(java.nio.file.Files.class.getName());
		UseFilesWriteStringResolver visitor = new UseFilesWriteStringResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseFilesWriteStringResolver"));
		setVisitor(visitor);
		String original = ""
				+ "String value = \"Hello World!\";\n"
				+ "try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(\"/home/test/testpath\"));\n"
				+ "			BufferedWriter bufferedWriter2 = new BufferedWriter(new FileWriter(\"/home/test/testpath-2\"))) {\n"
				+ "		bufferedWriter.write(value);\n"
				+ "		bufferedWriter2.write(value);\n"
				+ "}";

		String expected = ""
				+ "String value = \"Hello World!\";\n"
				+ "Files.writeString(Paths.get(\"/home/test/testpath\"), value);\n"
				+ "Files.writeString(Paths.get(\"/home/test/testpath-2\"), value, Charset.defaultCharset());\n";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
	
	@Test
	void test_resolveMarkers_missingCatchClause_shouldResolveOne() throws Exception {
		UseFilesWriteStringResolver visitor = new UseFilesWriteStringResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseFilesWriteStringResolver"));
		setVisitor(visitor);
		String original = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathString, cs))) {\n" +
				"	bufferedWriter.write(value);\n" +
				"}";

		String expected = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"Files.writeString(Paths.get(pathString), value, cs);";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
	
	@Test
	void visit_TWRUsingNewFileWriterNotAllResourcesRemoved_shouldTransform() throws Exception {

		UseFilesWriteStringResolver visitor = new UseFilesWriteStringResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseFilesWriteStringResolver"));
		setVisitor(visitor);
		String original = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathString, cs));\n" +
				"		BufferedWriter bufferedWriter2 = new BufferedWriter(new FileWriter(pathString, cs))) {\n" +
				"	bufferedWriter.write(value);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		String expected = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"try (BufferedWriter bufferedWriter2 = new BufferedWriter(new FileWriter(pathString, cs))) {\n" +
				"	Files.writeString(Paths.get(pathString), value, cs);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

}
