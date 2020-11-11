package eu.jsparrow.standalone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.slf4j.Logger;

import eu.jsparrow.standalone.ConfigFinder.ConfigType;

/**
 * The {@link ConfigFinderTest} uses mocking, as well as parameterized and
 * non-parameterized tests to test the {@link ConfigFinder} class.
 * 
 * @since 2.6.0
 */
@RunWith(Enclosed.class)
public class ConfigFinderTest {

	/**
	 * Base setup used for all other inner classes.
	 */
	public abstract static class ConfigFinderBaseTest {
		ConfigFinder configFinder;

		@Mock
		Logger mockLogger;

		@Rule
		public TemporaryFolder folder = new TemporaryFolder();

		@Before
		public void setUp() {
			initMocks(this);
			configFinder = new ConfigFinder();
			configFinder.setLogger(mockLogger);
		}
	}

	/**
	 * Parameterized tests for valid config file names.
	 */
	@RunWith(Parameterized.class)
	public static class ParameterizedValidTests extends ConfigFinderBaseTest {

		@Parameters(name = "{index}: Using valid input ({0})")
		public static String[] data() {
			return new String[] { "config.yml", "config.yaml", "Config.YAML", "CONFIG.YML" };
		}

		@Parameter
		public String configFileName;

		@Test
		public void getYAMLFilePath_validConfigFile_isPresentTrue() throws IOException {
			folder.newFile(configFileName);

			Path path = Paths.get(folder.getRoot()
				.getAbsolutePath());

			Optional<String> configFile = configFinder.getYAMLFilePath(path, ConfigType.CONFIG_FILE);

			assertTrue(String.format("Valid config file '%s' should be found", configFileName), configFile.isPresent());
		}
	}

	/**
	 * Parameterized tests for invalid config file names.
	 */
	@RunWith(Parameterized.class)
	public static class ParameterizedInvalidTests extends ConfigFinderBaseTest {

		@Parameters(name = "{index}: Using invalid input ({0})")
		public static String[] data() {
			return new String[] { "_config.yml", "config.yamll", "Config.YAL", "CONFIGYML", "c_onfig.yml", "config.xml",
					"random.yml", "config:yml" };
		}

		@Parameter
		public String configFileName;

		@Test
		public void getYAMLFilePath_invalidConfigFile_isPresentFalse() throws IOException {
			folder.newFile(configFileName);

			Path path = Paths.get(folder.getRoot()
				.getAbsolutePath());

			Optional<String> configFile = configFinder.getYAMLFilePath(path, ConfigType.CONFIG_FILE);

			assertFalse(String.format("Invalid config file '%s' should not be found", configFileName),
					configFile.isPresent());
		}
	}

	/**
	 * One-time non-parameterized tests.
	 */
	public static class NonParameterizedTests extends ConfigFinderBaseTest {

		@Test
		public void getYAMLFilePath_noConfigFile_isPresentFalse() {
			Path path = Paths.get(folder.getRoot()
				.getAbsolutePath());

			Optional<String> configFile = configFinder.getYAMLFilePath(path, ConfigType.CONFIG_FILE);

			assertFalse("Config file does not exist and should not be present", configFile.isPresent());
		}

		@Test
		public void getYAMLFilePath_invalidPath_isPresentFalse() {
			String invalidFolderPath = "/thatdoesnotexist/forreal";
			Path path = Paths.get(invalidFolderPath);

			Optional<String> configFile = configFinder.getYAMLFilePath(path, ConfigType.CONFIG_FILE);

			assertFalse(String.format("Invalid path '%s' should not return a config file", invalidFolderPath),
					configFile.isPresent());
		}

		@Test
		public void getYAMLFilePath_invalidPath_logDebug() {
			Path path = Paths.get("/thatdoesnotexist/forreal");

			configFinder.getYAMLFilePath(path, ConfigType.CONFIG_FILE);

			verify(mockLogger, times(1)).debug(anyString(), eq(path));
		}

		@Test
		public void getYAMLFilePath_multipleMatches_firstMatchReturned() throws IOException {
			String expectedFirstFileName = "CONFIG.YAML";

			File expectedFirstFile = folder.newFile(expectedFirstFileName);
			folder.newFile("config.yaml");
			folder.newFile("config.yml");
			folder.newFile("Config.yaml");
			folder.newFile("Config.yml");
			folder.newFile("COnfig.yml");

			Path path = Paths.get(folder.getRoot()
				.getAbsolutePath());

			Optional<String> configFile = configFinder.getYAMLFilePath(path, ConfigType.CONFIG_FILE);

			// configFileName is used for a nicer assert message exclusively
			String configFileName = "<empty>";
			if (configFile.isPresent()) {
				configFileName = StringUtils.substringAfterLast(configFile.get(), "/");
			}

			assertEquals(String.format("Expected the first valid config file to be '%s' and not '%s'",
					expectedFirstFileName, configFileName), expectedFirstFile.getAbsolutePath(), configFile.get());
		}
	}
}
