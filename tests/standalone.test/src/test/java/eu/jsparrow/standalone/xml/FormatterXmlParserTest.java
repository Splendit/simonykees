package eu.jsparrow.standalone.xml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThrows;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;

@SuppressWarnings("nls")
public class FormatterXmlParserTest {

	private static final String RESOURCE_DIRECTORY = "src/test/resources/xml";

	private static final String NO_FORMATTER_SETTINGS_FOUND = "No formatter settings found";
	private static final String NO_CODE_FORMATTER_PROFILE_FOUND = "No CodeFormatterProfile found";
	private static final String UNEXPECTED_XML_STRUCTURE = "Unexpected XML structure";
	private static final String PATH_UNAVAILABLE = "Path unavailable";

	@Test
	public void readSettings_validPath_shouldReturnValidSettings() throws FormatterXmlParserException {
		Map<String, String> expected = new HashMap<String, String>();
		expected.put("org.eclipse.jdt.core.formatter.insert_space_after_ellipsis", "insert");
		expected.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_enum_declarations", "insert");

		Map<String, String> settings = FormatterXmlParser.getFormatterSettings(loadResource("simple.xml"));
		assertThat(settings.entrySet(), equalTo(expected.entrySet()));
	}

	@Test
	public void readSettings_twoProfiles_shouldReturnFirstProfile() throws FormatterXmlParserException {
		Map<String, String> expected = new HashMap<String, String>();
		expected.put("1_org.eclipse.jdt.core.formatter.insert_space_after_ellipsis", "1_insert");
		expected.put("1_org.eclipse.jdt.core.formatter.insert_space_after_comma_in_enum_declarations", "1_insert");

		Map<String, String> settings = FormatterXmlParser.getFormatterSettings(loadResource("two-profiles.xml"));
		assertThat(settings.entrySet(), equalTo(expected.entrySet()));
	}

	@Test
	public void readSettings_secondProfileIsFormatter_shouldReturnSecondProfile() throws FormatterXmlParserException {
		Map<String, String> expected = new HashMap<String, String>();
		expected.put("2_org.eclipse.jdt.core.formatter.insert_space_after_ellipsis", "2_insert");
		expected.put("2_org.eclipse.jdt.core.formatter.insert_space_after_comma_in_enum_declarations", "2_insert");

		Map<String, String> settings = FormatterXmlParser
			.getFormatterSettings(loadResource("second-profile-is-formatter.xml"));
		assertThat(settings.entrySet(), equalTo(expected.entrySet()));
	}

	@Test
	public void readSettings_invalidPath_shouldThrowException() throws FormatterXmlParserException {
		ThrowingRunnable tr = () -> FormatterXmlParser.getFormatterSettings(loadResource("unavailable.xml"));
		FormatterXmlParserException e = assertThrows(FormatterXmlParserException.class, tr);

		assertThat(e.getMessage(), startsWith(PATH_UNAVAILABLE));
	}

	@Test
	public void readSettings_missingProfiles_shouldThrowException() throws FormatterXmlParserException {
		ThrowingRunnable tr = () -> FormatterXmlParser.getFormatterSettings(loadResource("missing-profiles.xml"));
		FormatterXmlParserException e = assertThrows(FormatterXmlParserException.class, tr);

		assertThat(e.getMessage(), startsWith(UNEXPECTED_XML_STRUCTURE));
	}

	@Test
	public void readSettings_missingProfile_shouldThrowException() throws FormatterXmlParserException {
		ThrowingRunnable tr = () -> FormatterXmlParser.getFormatterSettings(loadResource("missing-profile.xml"));
		FormatterXmlParserException e = assertThrows(FormatterXmlParserException.class, tr);

		assertThat(e.getMessage(), startsWith(NO_CODE_FORMATTER_PROFILE_FOUND));
	}

	@Test
	public void readSettings_missingSettings_shouldThrowException() throws FormatterXmlParserException {
		ThrowingRunnable tr = () -> FormatterXmlParser.getFormatterSettings(loadResource("missing-settings.xml"));
		FormatterXmlParserException e = assertThrows(FormatterXmlParserException.class, tr);

		assertThat(e.getMessage(), startsWith(NO_FORMATTER_SETTINGS_FOUND));
	}

	@Test
	public void readSettings_noFormatterKind_shouldThrowException() throws FormatterXmlParserException {
		ThrowingRunnable tr = () -> FormatterXmlParser.getFormatterSettings(loadResource("no-formatter-kind.xml"));
		FormatterXmlParserException e = assertThrows(FormatterXmlParserException.class, tr);

		assertThat(e.getMessage(), startsWith(NO_CODE_FORMATTER_PROFILE_FOUND));
	}

	private File loadResource(String resource) {
		return Paths.get(String.join("/", RESOURCE_DIRECTORY, resource))
			.toFile();
	}
}
