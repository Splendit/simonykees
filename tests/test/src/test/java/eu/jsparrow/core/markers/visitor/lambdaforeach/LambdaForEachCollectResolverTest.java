package eu.jsparrow.core.markers.visitor.lambdaforeach;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class LambdaForEachCollectResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
		fixture.addImport(java.util.Arrays.class.getName());
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		LambdaForEachCollectResolver visitor = new LambdaForEachCollectResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("LambdaForEachCollectResolver"));
		setVisitor(visitor);
		String original = "" + "List<String> objectList = Arrays.asList(\"foo\", \"bar\");\n"
				+ "List<String> oStrings = new ArrayList<>();\n" + "objectList.stream()\n"
				+ "	.map(o -> o.substring(0))\n" + "	.forEach(oString -> {\n" + "		oStrings.add(oString);\n"
				+ "	});";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		LambdaForEachCollectResolver visitor = new LambdaForEachCollectResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("LambdaForEachCollectResolver"));
		setVisitor(visitor);
		String original = "" + "List<String> objectList = Arrays.asList(\"foo\", \"bar\");\n"
				+ "List<String> oStrings = new ArrayList<>();\n" + "objectList.stream()\n"
				+ "	.map(o -> o.substring(0))\n" + "	.forEach(oString -> {\n" + "		oStrings.add(oString);\n"
				+ "	});";
		String expected = "" + "List<String> objectList = Arrays.asList(\"foo\", \"bar\");\n"
				+ "List<String> oStrings = new ArrayList<>();\n" + "oStrings.addAll(objectList.stream()\n"
				+ ".map(o -> o.substring(0)).collect(Collectors.toList()));";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Replaces 'Stream.forEach' with 'Stream.collect' if the argument of the forEach statement is only used for adding elements to a list.\n"
				+ "\n"
				+ "For example, 'stream.forEach(x -> list.add)' is transformed into 'stream.collect(Collectors.toList())'.\n"
				+ "\n" + "This simplifies adding elements to a list.";
		assertAll(() -> assertEquals("Use Stream::collect", event.getName()),
				() -> assertEquals(description, event.getMessage()),
				() -> assertEquals("LambdaForEachCollectResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()), () -> assertEquals(279, event.getOffset()),
				() -> assertEquals(111, event.getLength()), () -> assertEquals(11, event.getLineNumber()),
				() -> assertEquals(15, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		LambdaForEachCollectResolver visitor = new LambdaForEachCollectResolver(node -> node.getStartPosition() == 280);
		visitor.addMarkerListener(RefactoringMarkers.getFor("LambdaForEachCollectResolver"));
		setVisitor(visitor);
		String original = "" + "List<String> objectList = Arrays.asList(\"foo\", \"bar\");\n"
				+ "List<String> oStrings = new ArrayList<>();\n" + "objectList.stream()\n"
				+ "	.map(o -> o.substring(0))\n" + "	.forEach(oString -> {\n" + "		oStrings.add(oString);\n"
				+ "	});";
		String expected = "" + "List<String> objectList = Arrays.asList(\"foo\", \"bar\");\n"
				+ "List<String> oStrings = new ArrayList<>();\n" + "oStrings.addAll(objectList.stream()\n"
				+ ".map(o -> o.substring(0)).collect(Collectors.toList()));";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
