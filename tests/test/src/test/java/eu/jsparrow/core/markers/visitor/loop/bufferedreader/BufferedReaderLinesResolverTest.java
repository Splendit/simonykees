package eu.jsparrow.core.markers.visitor.loop.bufferedreader;

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

class BufferedReaderLinesResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		setJavaVersion(JavaCore.VERSION_11);
		fixture.addImport(java.io.FileReader.class.getName());
		fixture.addImport(java.io.BufferedReader.class.getName());
		RefactoringMarkers.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		BufferedReaderLinesResolver visitor = new BufferedReaderLinesResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("BufferedReaderLinesResolver"));
		setVisitor(visitor);
		String original = "" + 
				"try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n" +
				"	String line;\n" +
				"	while((line = bufferedReader.readLine()) != null) {\n" +
				"		System.out.println(line);\n" +
				"	}\n" +
				"} catch (Exception e) {\n" +
				"	e.printStackTrace();\n" +
				"}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		BufferedReaderLinesResolver visitor = new BufferedReaderLinesResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("BufferedReaderLinesResolver"));
		setVisitor(visitor);
		String original = "" + 
				"try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n" +
				"	String line;\n" +
				"	while((line = bufferedReader.readLine()) != null) {\n" +
				"		System.out.println(line);\n" +
				"	}\n" +
				"} catch (Exception e) {\n" +
				"	e.printStackTrace();\n" +
				"}";
		String expected = "" +
				"try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n" +
				"	bufferedReader.lines().forEach(line -> {\n" +
				"		System.out.println(line);\n" +
				"	});\n" +
				"} catch (Exception e) {\n" +
				"	e.printStackTrace();\n" +
				"}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Replaces loops iterating over lines of a file by BufferedReader::lines stream.";
		assertAll(
				() -> assertEquals("Use BufferedReader::lines", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("BufferedReaderLinesResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(276, event.getOffset()),
				() -> assertEquals(108, event.getLength()),
				() -> assertEquals(10, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		BufferedReaderLinesResolver visitor = new BufferedReaderLinesResolver(node -> node.getStartPosition() == 277);
		visitor.addMarkerListener(RefactoringMarkers.getFor("BufferedReaderLinesResolver"));
		setVisitor(visitor);
		String original = "" + 
				"try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n" +
				"	String line;\n" +
				"	while((line = bufferedReader.readLine()) != null) {\n" +
				"		System.out.println(line);\n" +
				"	}\n" +
				"} catch (Exception e) {\n" +
				"	e.printStackTrace();\n" +
				"}";
		String expected = "" +
				"try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n" +
				"	bufferedReader.lines().forEach(line -> {\n" +
				"		System.out.println(line);\n" +
				"	});\n" +
				"} catch (Exception e) {\n" +
				"	e.printStackTrace();\n" +
				"}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
