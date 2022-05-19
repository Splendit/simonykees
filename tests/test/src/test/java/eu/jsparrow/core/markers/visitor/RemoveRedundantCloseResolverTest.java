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

class RemoveRedundantCloseResolverTest extends UsesJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		RefactoringMarkers.clear();
		defaultFixture.addImport(java.io.BufferedReader.class.getName());
		defaultFixture.addImport(java.io.FileReader.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());

	}
	
	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		RemoveRedundantCloseResolver visitor = new RemoveRedundantCloseResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveRedundantCloseResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "	void readFirstLineFromFile(String path) {\n"
				+ "		try (BufferedReader br = new BufferedReader(new FileReader(path))) {\n"
				+ "			System.out.println(\"First line: \" + br.readLine());\n"
				+ "			br.close();\n"
				+ "		} catch (IOException e) {\n"
				+ "			e.printStackTrace();\n"
				+ "		}\n"
				+ "	}";


		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		RemoveRedundantCloseResolver visitor = new RemoveRedundantCloseResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveRedundantCloseResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "	void readFirstLineFromFile(String path) {\n"
				+ "		try (BufferedReader br = new BufferedReader(new FileReader(path))) {\n"
				+ "			System.out.println(\"First line: \" + br.readLine());\n"
				+ "			br.close();\n"
				+ "		} catch (IOException e) {\n"
				+ "			e.printStackTrace();\n"
				+ "		}\n"
				+ "	}";
		String expected = ""
				+ "	void readFirstLineFromFile(String path) {\n"
				+ "		try (BufferedReader br = new BufferedReader(new FileReader(path))) {\n"
				+ "			System.out.println(\"First line: \" + br.readLine());\n"
				+ "		} catch (IOException e) {\n"
				+ "			e.printStackTrace();\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "In Java, the try-with-resource statements are able to automatically close "
				+ "the resources which are defined in the try-with-resource header. Thus, any "
				+ "explicit 'close()' invocation in the try block is redundant and potentially confusing. "
				+ "This rule eliminates redundant resource 'close()' invocations.";
		
		assertAll(
				() -> assertEquals("Remove Redundant Close", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("RemoveRedundantCloseResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(327, event.getOffset()),
				() -> assertEquals(11, event.getLength()),
				() -> assertEquals(12, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		RemoveRedundantCloseResolver visitor = new RemoveRedundantCloseResolver(node -> node.getStartPosition() == 327);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveRedundantCloseResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "	void readFirstLineFromFile(String path) {\n"
				+ "		try (BufferedReader br = new BufferedReader(new FileReader(path))) {\n"
				+ "			System.out.println(\"First line: \" + br.readLine());\n"
				+ "			br.close();\n"
				+ "		} catch (IOException e) {\n"
				+ "			e.printStackTrace();\n"
				+ "		}\n"
				+ "	}";
		String expected = ""
				+ "	void readFirstLineFromFile(String path) {\n"
				+ "		try (BufferedReader br = new BufferedReader(new FileReader(path))) {\n"
				+ "			System.out.println(\"First line: \" + br.readLine());\n"
				+ "		} catch (IOException e) {\n"
				+ "			e.printStackTrace();\n"
				+ "		}\n"
				+ "	}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
	
	@Test
	void test_resolveOneSingleResourcePerMarker_shouldResolveOne() throws Exception {
		RemoveRedundantCloseResolver visitor = new RemoveRedundantCloseResolver(node -> node.getStartPosition() == 406);
		visitor.addMarkerListener(RefactoringMarkers.getFor("RemoveRedundantCloseResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "	void readFirstLineFromFile(String path) {\n"
				+ "		try (BufferedReader br = new BufferedReader(new FileReader(path));BufferedReader br2 = new BufferedReader(new FileReader(path))) {\n"
				+ "			System.out.println(\"First line: \" + br.readLine());\n"
				+ "			br.close();\n"
				+ "			br2.close();\n"
				+ "		} catch (IOException e) {\n"
				+ "			e.printStackTrace();\n"
				+ "		}\n"
				+ "	}";
		String expected = ""
				+ "	void readFirstLineFromFile(String path) {\n"
				+ "		try (BufferedReader br = new BufferedReader(new FileReader(path));BufferedReader br2 = new BufferedReader(new FileReader(path))) {\n"
				+ "			System.out.println(\"First line: \" + br.readLine());\n"
				+ "			br2.close();\n"
				+ "		} catch (IOException e) {\n"
				+ "			e.printStackTrace();\n"
				+ "		}\n"
				+ "	}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
