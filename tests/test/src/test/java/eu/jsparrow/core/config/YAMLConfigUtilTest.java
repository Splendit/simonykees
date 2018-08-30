package eu.jsparrow.core.config;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.osgi.util.NLS;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;

@SuppressWarnings("nls")
public class YAMLConfigUtilTest {

	private static final Logger logger = LoggerFactory.getLogger(YAMLConfigUtilTest.class);

	// For testing file operations refer to file in the resources directory
	private static final String RESOURCE_DIRECTORY = "src/test/resources/eu/jsparrow/core/config";
	private static final String PROFILE_NAME = "profile-name";

	File exportFile;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws IOException {
		exportFile = File.createTempFile("export", "yaml");
	}

	@After
	public void tearDown() throws IOException {
		if (!Files.deleteIfExists(exportFile.toPath())) {
			String loggerError = NLS.bind(Messages.Activator_couldNotDeleteFileWithPath, exportFile.getAbsolutePath());
			logger.error(loggerError);
		}
	}

	@Test
	public void loadConfiguration_LoadValidYAML_ShouldReturnYAMLConfig() throws YAMLConfigException {
		YAMLConfig config = YAMLConfigUtil.loadConfiguration(loadResource("valid.yaml"));
		assertNotNull(config);
	}

	@Test
	public void loadConfiguration_LoadInvalidYAML_ShouldThrowException() throws YAMLConfigException {
		expectedException.expect(YAMLConfigException.class);

		YAMLConfig config = YAMLConfigUtil.loadConfiguration(loadResource("invalid.yaml"));
		assertNotNull(config);
	}

	@Test
	public void exportConfig_ToFile_ShouldWriteToFile() throws YAMLConfigException {
		YAMLConfig config = new YAMLConfig();
		YAMLConfigUtil.exportConfig(config, exportFile);

		assertNotEquals(0, exportFile.length());
	}

	@Test
	public void exportConfig_ToNonWritableFile_ShouldThrowException() throws YAMLConfigException {
		YAMLConfig config = new YAMLConfig();
		assertTrue(exportFile.setWritable(false));
		expectedException.expect(YAMLConfigException.class);
		YAMLConfigUtil.exportConfig(config, exportFile);
	}

	@Test
	public void readConfig_NonExistentFile_ShouldThrowException() throws YAMLConfigException {
		expectedException.expect(YAMLConfigException.class);
		expectedException.expectMessage(
				"The provided path (file) does not lead to a YAML configuration file! (File extension must be *.yml or *.yaml)");
		YAMLConfig config = YAMLConfigUtil.readConfig("file");
		assertEquals("default", config.getSelectedProfile());
	}

	@Test
	public void readConfig_ExistingFile_ShouldUseDefaultProfile() throws YAMLConfigException {
		YAMLConfig config = YAMLConfigUtil.readConfig(String.join("/", RESOURCE_DIRECTORY, "valid.yaml"));

		assertEquals("aaa", config.getProfiles()
			.get(0)
			.getName());
	}

	@Test
	public void updateProfile_shouldSetSelectedProfile() throws YAMLConfigException {
		String profileName2 = "profile-name-2"; //$NON-NLS-1$
		YAMLConfig yamlConfig = new YAMLConfig();
		yamlConfig.setSelectedProfile(PROFILE_NAME);
		yamlConfig.setProfiles(
				Arrays.asList(new YAMLProfile(PROFILE_NAME, emptyList(), new YAMLRenamingRule(), new YAMLLoggerRule()),
						new YAMLProfile(profileName2, emptyList(), new YAMLRenamingRule(), new YAMLLoggerRule())));

		YAMLConfigUtil.updateSelectedProfile(yamlConfig, profileName2);

		assertEquals(profileName2, yamlConfig.getSelectedProfile());
	}

	@Test
	public void updateProfile_NonExistingProflie_shouldThrowException() throws YAMLConfigException {
		YAMLConfig yamlConfig = new YAMLConfig();
		yamlConfig.setProfiles(singletonList(
				new YAMLProfile(PROFILE_NAME, emptyList(), new YAMLRenamingRule(), new YAMLLoggerRule())));

		expectedException.expect(YAMLConfigException.class);
		expectedException.expectMessage("Profile [INVALID] does not exist"); //$NON-NLS-1$

		YAMLConfigUtil.updateSelectedProfile(yamlConfig, "INVALID"); //$NON-NLS-1$
	}

	@Test
	public void updateProfile_EmptyProfile_shouldNotUpdate() throws YAMLConfigException {
		YAMLConfig yamlConfig = new YAMLConfig();
		yamlConfig.setSelectedProfile(PROFILE_NAME);

		YAMLConfigUtil.updateSelectedProfile(yamlConfig, ""); //$NON-NLS-1$

		assertEquals(PROFILE_NAME, yamlConfig.getSelectedProfile());
	}

	@Test
	public void updateProfile_NullProfile_shouldNotUpdate() throws YAMLConfigException {
		YAMLConfig yamlConfig = new YAMLConfig();
		yamlConfig.setSelectedProfile(PROFILE_NAME);

		YAMLConfigUtil.updateSelectedProfile(yamlConfig, null); // $NON-NLS-1$

		assertEquals(PROFILE_NAME, yamlConfig.getSelectedProfile());
	}

	private File loadResource(String resource) {
		return Paths.get(String.join("/", RESOURCE_DIRECTORY, resource))
			.toFile();
	}

}
