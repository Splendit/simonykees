package eu.jsparrow.standalone.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.osgi.framework.Bundle;

import eu.jsparrow.standalone.Activator;

@SuppressWarnings("nls")
public class FormatterXmlParserTest {

	private static final String RESOURCE_DIRECTORY = "src/test/resources/xml";

	private static final String NO_FORMATTER_SETTINGS_FOUND = "No formatter settings found";
	private static final String NO_CODE_FORMATTER_PROFILE_FOUND = "No CodeFormatterProfile found";
	private static final String UNEXPECTED_XML_STRUCTURE = "Unexpected XML structure";
	private static final String PATH_UNAVAILABLE = "Path unavailable";
	private static final String FILE_PATH_IS_NULL = "File path is null";

	@Test
	public void readSettings_validPath_shouldReturnValidSettings() throws Exception {
		Map<String, String> expected = new HashMap<>();
		expected.put("org.eclipse.jdt.core.formatter.insert_space_after_ellipsis", "insert");
		expected.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_enum_declarations", "insert");

		Map<String, String> settings = FormatterXmlParser.getFormatterSettings(loadResource("simple.xml"));
		assertEquals(expected.entrySet(), settings.entrySet());
	}

	@Test
	public void readSettings_twoProfiles_shouldReturnFirstProfile() throws Exception {
		Map<String, String> expected = new HashMap<>();
		expected.put("1_org.eclipse.jdt.core.formatter.insert_space_after_ellipsis", "1_insert");
		expected.put("1_org.eclipse.jdt.core.formatter.insert_space_after_comma_in_enum_declarations", "1_insert");

		Map<String, String> settings = FormatterXmlParser.getFormatterSettings(loadResource("two-profiles.xml"));
		assertEquals(expected.entrySet(), settings.entrySet());
	}

	@Test
	public void readSettings_secondProfileIsFormatter_shouldReturnSecondProfile() throws Exception {
		Map<String, String> expected = new HashMap<>();
		expected.put("2_org.eclipse.jdt.core.formatter.insert_space_after_ellipsis", "2_insert");
		expected.put("2_org.eclipse.jdt.core.formatter.insert_space_after_comma_in_enum_declarations", "2_insert");

		Map<String, String> settings = FormatterXmlParser
			.getFormatterSettings(loadResource("second-profile-is-formatter.xml"));
		assertEquals(expected.entrySet(), settings.entrySet());
	}

	@Test
	public void readSettings_5000EntriesLargeFile_shouldReturn5000Settings() throws Exception {
		int expectedSize = 5000;

		Map<String, String> settings = FormatterXmlParser.getFormatterSettings(loadResource("5000-entries-large.xml"));
		assertEquals(expectedSize, settings.size());
	}

	@Test
	public void readSettings_invalidPath_shouldThrowException() throws Exception {
		String invalidPath = String.join("/", "/xyz", "unavailable.xml");
		Executable tr = () -> FormatterXmlParser.getFormatterSettings(new File(invalidPath));
		FormatterXmlParserException e = assertThrows(FormatterXmlParserException.class, tr);

		assertTrue(e.getMessage()
			.startsWith(PATH_UNAVAILABLE));
	}

	@Test
	public void readSettings_nullPath_shouldThrowException() throws Exception {
		Executable tr = () -> FormatterXmlParser.getFormatterSettings(null);
		FormatterXmlParserException e = assertThrows(FormatterXmlParserException.class, tr);

		assertTrue(e.getMessage()
			.startsWith(FILE_PATH_IS_NULL));
	}

	@Test
	public void readSettings_missingProfiles_shouldThrowException() throws Exception {
		Executable tr = () -> FormatterXmlParser.getFormatterSettings(loadResource("missing-profiles.xml"));
		FormatterXmlParserException e = assertThrows(FormatterXmlParserException.class, tr);

		assertTrue(e.getMessage()
			.startsWith(UNEXPECTED_XML_STRUCTURE));
	}

	@Test
	public void readSettings_missingProfile_shouldThrowException() throws Exception {
		Executable tr = () -> FormatterXmlParser.getFormatterSettings(loadResource("missing-profile.xml"));
		FormatterXmlParserException e = assertThrows(FormatterXmlParserException.class, tr);

		assertTrue(e.getMessage()
			.startsWith(UNEXPECTED_XML_STRUCTURE));
	}

	@Test
	public void readSettings_missingSettings_shouldThrowException() throws Exception {
		Executable tr = () -> FormatterXmlParser.getFormatterSettings(loadResource("missing-settings.xml"));
		FormatterXmlParserException e = assertThrows(FormatterXmlParserException.class, tr);

		assertTrue(e.getMessage()
			.startsWith(NO_FORMATTER_SETTINGS_FOUND));
	}

	@Test
	public void readSettings_noFormatterKind_shouldThrowException() throws Exception {
		Executable tr = () -> FormatterXmlParser.getFormatterSettings(loadResource("no-formatter-kind.xml"));
		FormatterXmlParserException e = assertThrows(FormatterXmlParserException.class, tr);

		assertTrue(e.getMessage()
			.startsWith(NO_CODE_FORMATTER_PROFILE_FOUND));
	}

	/**
	 * Gets a file name, adds a relative path and uses OSGi mechanisms to
	 * retrieve the actual path of the file. Note: This method does not work
	 * with invalid paths.
	 * 
	 * @param resource
	 *            a valid file name
	 * @return a file instance with the resolved path
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private File loadResource(String resource) throws IOException, URISyntaxException {
		String filePath = String.join("/", RESOURCE_DIRECTORY, resource);

		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		IPath iPathReport = new Path(filePath);
		URL url = FileLocator.find(bundle, iPathReport, new HashMap<>());
		URL templateDirecotryUrl = null;

		templateDirecotryUrl = FileLocator.toFileURL(url);
		return new File(templateDirecotryUrl.toURI());
	}
}
