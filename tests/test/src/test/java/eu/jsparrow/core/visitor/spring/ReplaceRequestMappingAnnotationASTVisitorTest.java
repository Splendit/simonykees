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
		defaultFixture.addImport("org.springframework.web.bind.annotation.RequestMethod");
		defaultFixture.addImport("org.springframework.web.bind.annotation.RequestParam");
		setDefaultVisitor(new ReplaceRequestMappingAnnotationASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	/**
	 * This test is expected to fail as soon as
	 * ReplaceRequestMappingAnnotationASTVisitor is implemented.
	 */
	@Test
	public void visit_RequestMappingWithRequestMethodGet_shouldTransform() throws Exception {

		String original = "" +
				"	@RequestMapping(value = \"/hello\", method = RequestMethod.GET)\n"
				+ "	public String hello(@RequestParam String name) {\n"
				+ "		return String.format(\"Hello %s!\", name);\n"
				+ "	}";

		String expected = "" +
				"	@RequestMapping(value = \"/hello\", method = RequestMethod.GET)\n"
				+ "	public String hello(@RequestParam String name) {\n"
				+ "		return String.format(\"Hello %s!\", name);\n"
				+ "	}";

		assertChange(original, expected);
	}

}
