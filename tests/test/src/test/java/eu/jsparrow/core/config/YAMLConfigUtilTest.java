package eu.jsparrow.core.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.eclipse.osgi.util.NLS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;

@SuppressWarnings("nls")
public class YAMLConfigUtilTest {

	private static final Logger logger = LoggerFactory.getLogger(YAMLConfigUtilTest.class);

	// For testing file operations refer to file in the resources directory
	private static final String RESOURCE_DIRECTORY = "src/test/resources/eu/jsparrow/core/config";

	File exportFile;

	@Before
	public void setUp() throws IOException {
		exportFile = File.createTempFile("export", "yaml");
	}

	@After
	public void tearDown() {
		if (!exportFile.delete()) {
			String loggerError = NLS.bind(Messages.Activator_couldNotDeleteFileWithPath, exportFile.getAbsolutePath());
			logger.error(loggerError);
		}
	}

	@Test
	public void loadConfiguration_LoadValidYAML_ShouldReturnYAMLConfig() throws YAMLConfigException {
		YAMLConfig config = YAMLConfigUtil.loadConfiguration(loadResource("valid.yaml"));
		assertNotNull(config);
	}

	@Test(expected = YAMLConfigException.class)
	public void loadConfiguration_LoadInvalidYAML_ShouldThrowException() throws YAMLConfigException {
		YAMLConfig config = YAMLConfigUtil.loadConfiguration(loadResource("invalid.yaml"));
		assertNotNull(config);
	}

	@Test
	public void exportConfig_ToFile_ShouldWriteToFile() throws YAMLConfigException {
		YAMLConfig config = new YAMLConfig();
		YAMLConfigUtil.exportConfig(config, exportFile);

		assertNotEquals(0, exportFile.length());
	}

	@Test(expected = YAMLConfigException.class)
	public void exportConfig_ToNonWritableFile_ShouldThrowException() throws YAMLConfigException {
		YAMLConfig config = new YAMLConfig();
		assertTrue(exportFile.setWritable(false));
		YAMLConfigUtil.exportConfig(config, exportFile);
	}

	@Test(expected = YAMLConfigException.class)
	public void getSelectedRulesFromConfig_InvalidSelectedProfile_ShouldThrowException() throws YAMLConfigException {
		YAMLConfig config = new YAMLConfig();
		config.setSelectedProfile("INVALID");

		YAMLConfigUtil.getSelectedRulesFromConfig(config, new ArrayList<>());
	}

	@Test
	public void getSelectedRulesFromConfig_WithoutProfileWithValidRules_ShouldReturnAllRules()
			throws YAMLConfigException {
		YAMLConfig config = new YAMLConfig();
		config.getRules()
			.add("TryWithResource");

		YAMLConfigUtil.getSelectedRulesFromConfig(config, new ArrayList<>());
	}

	@Test(expected = YAMLConfigException.class)
	public void readConfig_InvalidProfile_ShouldThrowException() throws YAMLConfigException {
		YAMLConfigUtil.readConfig("file");
	}

	@Test(expected = YAMLConfigException.class)
	public void readConfig_NonExistentFileWithoutProfile_ShouldThrowException() throws YAMLConfigException {
		YAMLConfig config = YAMLConfigUtil.readConfig("file");

		assertEquals("default", config.getSelectedProfile());
	}

	@Test
	public void readConfig_ExistingFileWithoutProfile_ShouldUseDefaultProfile() throws YAMLConfigException {
		YAMLConfig config = YAMLConfigUtil.readConfig(String.join("/", RESOURCE_DIRECTORY, "valid.yaml"));

		assertEquals("aaa", config.getProfiles()
			.get(0)
			.getName());
	}

	private File loadResource(String resource) {
		return Paths.get(String.join("/", RESOURCE_DIRECTORY, resource))
			.toFile();
	}

}
