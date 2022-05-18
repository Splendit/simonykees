package eu.jsparrow.core.visitor.spring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;

public class ReplaceRequestMappingAnnotationASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		addDependency("org.springframework", "spring-web", "5.3.19");
		defaultFixture.addImport("org.springframework.web.bind.annotation.RequestMapping");
		defaultFixture.addImport("org.springframework.web.bind.annotation.RequestParam");
		setDefaultVisitor(new ReplaceRequestMappingAnnotationASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_RequestMappingWithRequestMethodGet_shouldTransform() throws Exception {

		defaultFixture.addImport("org.springframework.web.bind.annotation.RequestMethod");
		String original = "" +
				"	@RequestMapping(value = \"/hello\", method = RequestMethod.GET)\n"
				+ "	public String hello(@RequestParam String name) {\n"
				+ "		return String.format(\"Hello %s!\", name);\n"
				+ "	}";

		String expected = "" +
				"	@GetMapping(value = \"/hello\")\n"
				+ "	public String hello(@RequestParam String name) {\n"
				+ "		return String.format(\"Hello %s!\", name);\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_RequestMethodGetInArrayInitializer_shouldTransform() throws Exception {

		defaultFixture.addImport("org.springframework.web.bind.annotation.RequestMethod");
		String original = "" +
				"	@RequestMapping(value = \"/hello\", method = {RequestMethod.GET})\n"
				+ "	public String hello(@RequestParam String name) {\n"
				+ "		return String.format(\"Hello %s!\", name);\n"
				+ "	}";

		String expected = "" +
				"	@GetMapping(value = \"/hello\")\n"
				+ "	public String hello(@RequestParam String name) {\n"
				+ "		return String.format(\"Hello %s!\", name);\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_StaticImportOfRequestMethodGet_shouldTransform() throws Exception {
		defaultFixture.addImport("org.springframework.web.bind.annotation.RequestMethod.GET", true, false);
		String original = "" +
				"	@RequestMapping(value = \"/hello\", method = GET)\n"
				+ "	public String hello(@RequestParam String name) {\n"
				+ "		return String.format(\"Hello %s!\", name);\n"
				+ "	}";

		String expected = "" +
				"	@GetMapping(value = \"/hello\")\n"
				+ "	public String hello(@RequestParam String name) {\n"
				+ "		return String.format(\"Hello %s!\", name);\n"
				+ "	}";

		assertChange(original, expected);
	}
}
