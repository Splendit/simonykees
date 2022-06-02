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

class ReplaceRequestMappingAnnotationResolverTest extends UsesJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		addDependency("org.springframework", "spring-web", "5.3.19");
		RefactoringMarkers.clear();
		defaultFixture.addImport("org.springframework.web.bind.annotation.RequestMapping");
		defaultFixture.addImport("org.springframework.web.bind.annotation.RequestParam");
		defaultFixture.addImport("org.springframework.web.bind.annotation.RequestMethod");

	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		ReplaceRequestMappingAnnotationResolver visitor = new ReplaceRequestMappingAnnotationResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceRequestMappingAnnotationEventResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "	@RequestMapping(value = \"/hello\", method = RequestMethod.GET)\n"
				+ "	public String hello(@RequestParam String name) {\n"
				+ "		return \"Hello \" + name + \"!\";\n"
				+ "	}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		ReplaceRequestMappingAnnotationResolver visitor = new ReplaceRequestMappingAnnotationResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceRequestMappingAnnotationEventResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "	@RequestMapping(value = \"/hello\", method = RequestMethod.GET)\n"
				+ "	public String hello(@RequestParam String name) {\n"
				+ "		return \"Hello \" + name + \"!\";\n"
				+ "	}";
		String expected = ""
				+ "	@GetMapping(value = \"/hello\")\n"
				+ "	public String hello(@RequestParam String name) {\n"
				+ "		return \"Hello \" + name + \"!\";\n"
				+ "	}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "The Spring Framework 4.3 introduced some composed annotations like '@GetMapping', '@PostMapping', etc, "
				+ "as an alternative of '@RequestMapping(method=...)' for annotating HTTP request handlers. Accordingly, "
				+ "this rule replaces the '@RequestMapping' annotations with their equivalent dedicated alternatives, "
				+ "for example, '@RequestMapping(value = \"/hello\", method = RequestMethod.GET)' is replaced by "
				+ "'@GetMapping(value = \"/hello\")'.";
		assertAll(
				() -> assertEquals("Replace Request Mapping Annotation", event.getName()),
				() -> assertEquals(description, event.getMessage()),
				() -> assertEquals("ReplaceRequestMappingAnnotationEventResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(232, event.getOffset()),
				() -> assertEquals(61, event.getLength()),
				() -> assertEquals(9, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		ReplaceRequestMappingAnnotationResolver visitor = new ReplaceRequestMappingAnnotationResolver(
				node -> node.getStartPosition() == 232);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceRequestMappingAnnotationEventResolver"));
		setDefaultVisitor(visitor);

		String original = ""
				+ "	@RequestMapping(value = \"/hello\", method = RequestMethod.GET)\n"
				+ "	public String hello(@RequestParam String name) {\n"
				+ "		return \"Hello \" + name + \"!\";\n"
				+ "	}";
		String expected = ""
				+ "	@GetMapping(value = \"/hello\")\n"
				+ "	public String hello(@RequestParam String name) {\n"
				+ "		return \"Hello \" + name + \"!\";\n"
				+ "	}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

	@Test
	void test_resolveOneSingleResourcePerMarker_shouldResolveFirst() throws Exception {
		ReplaceRequestMappingAnnotationResolver visitor = new ReplaceRequestMappingAnnotationResolver(
				node -> node.getStartPosition() == 232);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceRequestMappingAnnotationEventResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "	@RequestMapping(value = \"/hello/first\", method = RequestMethod.GET)\n"
				+ "	public String hello1(@RequestParam String name) {\n"
				+ "		return \"Hello \" + name + \"!\";\n"
				+ "	}\n"
				+ "	\n"
				+ "	@RequestMapping(value = \"/hello/second\", method = RequestMethod.GET)\n"
				+ "	public String hello2(@RequestParam String name) {\n"
				+ "		return \"Hello \" + name + \"!\";\n"
				+ "	}";
		String expected = ""
				+ "	@GetMapping(value = \"/hello/first\")\n"
				+ "	public String hello1(@RequestParam String name) {\n"
				+ "		return \"Hello \" + name + \"!\";\n"
				+ "	}\n"
				+ "	\n"
				+ "	@RequestMapping(value = \"/hello/second\", method = RequestMethod.GET)\n"
				+ "	public String hello2(@RequestParam String name) {\n"
				+ "		return \"Hello \" + name + \"!\";\n"
				+ "	}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

	@Test
	void test_resolveOneSingleResourcePerMarker_shouldResolveSecond() throws Exception {
		ReplaceRequestMappingAnnotationResolver visitor = new ReplaceRequestMappingAnnotationResolver(
				node -> node.getStartPosition() == 402);

		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceRequestMappingAnnotationEventResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "	@RequestMapping(value = \"/hello/first\", method = RequestMethod.GET)\n"
				+ "	public String hello1(@RequestParam String name) {\n"
				+ "		return \"Hello \" + name + \"!\";\n"
				+ "	}\n"
				+ "	\n"
				+ "	@RequestMapping(value = \"/hello/second\", method = RequestMethod.GET)\n"
				+ "	public String hello2(@RequestParam String name) {\n"
				+ "		return \"Hello \" + name + \"!\";\n"
				+ "	}";
		String expected = ""
				+ "	@RequestMapping(value = \"/hello/first\", method = RequestMethod.GET)\n"
				+ "	public String hello1(@RequestParam String name) {\n"
				+ "		return \"Hello \" + name + \"!\";\n"
				+ "	}\n"
				+ "	\n"
				+ "	@GetMapping(value = \"/hello/second\")\n"
				+ "	public String hello2(@RequestParam String name) {\n"
				+ "		return \"Hello \" + name + \"!\";\n"
				+ "	}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
