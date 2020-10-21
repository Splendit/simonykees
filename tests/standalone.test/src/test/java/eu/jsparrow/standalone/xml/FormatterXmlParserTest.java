package eu.jsparrow.standalone.xml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

@SuppressWarnings("nls")
public class FormatterXmlParserTest {

	private static final String RESOURCE_DIRECTORY = "src/test/resources/xml";

	@Test
	public void readSettings_invalidPath_shouldThrowException() throws Exception {
		Map<String, String> expected = new HashMap<String, String>();
		expected.put("org.eclipse.jdt.core.formatter.insert_space_after_ellipsis", "insert");
		expected.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_enum_declarations", "insert");
		
		Map<String, String> settings = FormatterXmlParser.getFormatterSettings(loadResource("simple.xml"));
		assertThat(settings.entrySet(), equalTo(expected.entrySet()));
	}

	private File loadResource(String resource) {
		return Paths.get(String.join("/", RESOURCE_DIRECTORY, resource))
			.toFile();
	}
}
