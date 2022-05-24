package eu.jsparrow.core.visitor.spring;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

	private static Stream<Arguments> mapToAnnotationReplacement() throws Exception {
		return Stream.of(
				Arguments.of("GET", "GetMapping"),
				Arguments.of("PUT", "PutMapping"),
				Arguments.of("POST", "PostMapping"),
				Arguments.of("PATCH", "PatchMapping"),
				Arguments.of("DELETE", "DeleteMapping"));
	}

	@ParameterizedTest
	@MethodSource(value = "mapToAnnotationReplacement")
	public void visit_RequestMappingWithRequestMethodGet_shouldTransform(String requestMethod,
			String annotationReplacement) throws Exception {

		defaultFixture.addImport("org.springframework.web.bind.annotation.RequestMethod");
		String original = String.format("" +
				"	@RequestMapping(value = \"/hello\", method = RequestMethod.%s)\n"
				+ "	public String hello(@RequestParam String name) {\n"
				+ "		return \"Hello \" + name + \"!\";\n"
				+ "	}", requestMethod);

		String expected = String.format("" +
				"	@%s(value = \"/hello\")\n"
				+ "	public String hello(@RequestParam String name) {\n"
				+ "		return \"Hello \" + name + \"!\";\n"
				+ "	}", annotationReplacement);

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

	@Test
	public void visit_RequestMappingNotSpring_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.springframework.web.bind.annotation.RequestMethod");
		String original = "" +
				"	@RequestMapping(value = \"/hello\", method = RequestMethod.GET)\n"
				+ "	public String hello(@RequestParam String name) {\n"
				+ "		return \"Hello \" + name + \"!\";\n"
				+ "	}\n"
				+ "\n"
				+ "	@interface RequestMapping {\n"
				+ "		String[] value() default {};\n"
				+ "\n"
				+ "		RequestMethod[] method() default {};\n"
				+ "	}";
		assertNoChange(original);
	}
	
	
	@Test
	public void visit_NoRequestMethodFound_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.springframework.web.bind.annotation.RequestMethod");
		String original = "" +
				"	@RequestMapping(value = \"/hello\")\n"
				+ "	public String hello(@RequestParam String name) {\n"
				+ "		return \"Hello \" + name + \"!\";\n"
				+ "	}";

		assertNoChange(original);
	}
	
	
	@Test
	public void visit_MultipleRequestMethods_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.springframework.web.bind.annotation.RequestMethod");
		
		String original = "" +
				"	@RequestMapping(value = \"/hello\", method = {RequestMethod.GET, RequestMethod.POST})\n"
				+ "	public String hello(@RequestParam String name) {\n"
				+ "		return \"Hello \" + name + \"!\";\n"
				+ "	}";

		assertNoChange(original);
	}
	
	
	@Test
	public void visit_NotSupportedRequestMethod_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.springframework.web.bind.annotation.RequestMethod");
		
		String original = "" +
				"	@RequestMapping(value = \"/hello\", method = RequestMethod.OPTIONS)\n"
				+ "	public String hello(@RequestParam String name) {\n"
				+ "		return \"Hello \" + name + \"!\";\n"
				+ "	}";

		assertNoChange(original);
	}
}
