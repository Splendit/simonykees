package eu.jsparrow.core.markers.visitor.loop.stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class EnhancedForLoopToStreamFindFirstResolverTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	public void beforeEach() throws Exception {
		RefactoringMarkers.clear();
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.Arrays.class.getName());
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		EnhancedForLoopToStreamFindFirstResolver visitor = new EnhancedForLoopToStreamFindFirstResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("EnhancedForLoopToStreamFindFirstResolver"));
		setVisitor(visitor);
		String original = ""
				+ "		String key = \"b\";\n"
				+ "		List<String> values = Arrays.asList(\"a\", \"b\");\n"
				+ "		for(String value : values) {\n"
				+ "		    if(value.length() > 4) {\n"
				+ "		         key = value;\n"
				+ "		         break;\n"
				+ "		    }\n"
				+ "		}";
		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		EnhancedForLoopToStreamFindFirstResolver visitor = new EnhancedForLoopToStreamFindFirstResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("EnhancedForLoopToStreamFindFirstResolver"));
		setVisitor(visitor);
		String original = ""
				+ "		String key = \"b\";\n"
				+ "		List<String> values = Arrays.asList(\"a\", \"b\");\n"
				+ "		for(String value : values) {\n"
				+ "		    if(value.length() > 4) {\n"
				+ "		         key = value;\n"
				+ "		         break;\n"
				+ "		    }\n"
				+ "		}";

		String expected = ""
				+ "		List<String> values=Arrays.asList(\"a\",\"b\");\n"
				+ "		String key=values.stream().filter(value -> value.length() > 4).findFirst().orElse(\"b\");";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Enhanced for-loops which are used to find an element within a collection can be replaced by Stream::findFirst. "
				+ "Using the stream syntax a multi-line control statement can be reduced to a single line. ";
		assertAll(
				() -> assertEquals("Replace For-Loop with Stream::findFirst", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("EnhancedForLoopToStreamFindFirstResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(218, event.getOffset()),
				() -> assertEquals(143, event.getLength()),
				() -> assertEquals(10, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		EnhancedForLoopToStreamFindFirstResolver visitor = new EnhancedForLoopToStreamFindFirstResolver(node -> node.getStartPosition() == 219);
		visitor.addMarkerListener(RefactoringMarkers.getFor("EnhancedForLoopToStreamFindFirstResolver"));
		setVisitor(visitor);
		String original = ""
				+ "		String key = \"b\";\n"
				+ "		List<String> values = Arrays.asList(\"a\", \"b\");\n"
				+ "		for(String value : values) {\n"
				+ "		    if(value.length() > 4) {\n"
				+ "		         key = value;\n"
				+ "		         break;\n"
				+ "		    }\n"
				+ "		}";

		String expected = ""
				+ "		List<String> values = Arrays.asList(\"a\", \"b\");\n"
				+ "		String key = values.stream().filter(value -> value.length() > 4).findFirst().orElse(\"b\");";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
