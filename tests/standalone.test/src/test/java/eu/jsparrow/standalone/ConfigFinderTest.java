package eu.jsparrow.standalone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;

import eu.jsparrow.standalone.ConfigFinder.ConfigType;

/**
 * The {@link ConfigFinderTest} uses mocking, as well as parameterized and
 * non-parameterized tests to test the {@link ConfigFinder} class.
 * 
 * @since 2.6.0
 */
public class ConfigFinderTest {

	Path tempDirectory;
	ConfigFinder configFinder;
	Logger mockLogger;

	@BeforeEach
	public void setUp() throws IOException {
		mockLogger = mock(Logger.class);
		tempDirectory = Files.createTempDirectory("jsparrow-standalone-test-").toAbsolutePath();
		assertTrue(tempDirectory.toFile().getName().startsWith("jsparrow-standalone-test-"));
		assertEquals("tmp", tempDirectory.toFile().getParentFile().getName());
		configFinder = new ConfigFinder();
		configFinder.setLogger(mockLogger);
	}

	@AfterEach
	public void tearDown() throws IOException {
		assertTrue(Files.isDirectory(tempDirectory));
		File[] childFiles = tempDirectory.toFile().listFiles();
		for (File childFile : childFiles) {
			assertTrue(childFile.isFile());
			Files.delete(childFile.toPath());
		}
		Files.delete(tempDirectory);

	}

	@ParameterizedTest
	@ValueSource(strings = { "config.yml", "config.yaml", "Config.YAML", "CONFIG.YML" })
	public void getYAMLFilePath_validConfigFile_isPresentTrue(String configFileName) throws IOException {
		Path newFile = new File(tempDirectory.toFile(), configFileName).toPath();

		Files.createFile(newFile);

		Optional<String> configFile = configFinder.getYAMLFilePath(tempDirectory, ConfigType.CONFIG_FILE);

		assertTrue(configFile.isPresent(), String.format("Valid config file '%s' should be found", configFileName));
	}

	@ParameterizedTest
	@ValueSource(strings = { "_config.yml", "config.yamll", "Config.YAL", "CONFIGYML", "c_onfig.yml", "config.xml",
			"random.yml", "config:yml" })
	public void getYAMLFilePath_invalidConfigFile_isPresentFalse(String configFileName) throws IOException {
		Path newFile = new File(tempDirectory.toFile(), configFileName).toPath();

		Files.createFile(newFile);

		Optional<String> configFile = configFinder.getYAMLFilePath(tempDirectory, ConfigType.CONFIG_FILE);

		assertFalse(configFile.isPresent(),
				String.format("Invalid config file '%s' should not be found", configFileName));
	}

	@Test
	public void getYAMLFilePath_noConfigFile_isPresentFalse() {
		Optional<String> configFile = configFinder.getYAMLFilePath(tempDirectory, ConfigType.CONFIG_FILE);

		assertFalse(configFile.isPresent(), "Config file does not exist and should not be present");
	}

	@Test
	public void getYAMLFilePath_invalidPath_isPresentFalse() {
		String invalidFolderPath = "/thatdoesnotexist/forreal";
		Path path = Paths.get(invalidFolderPath);

		Optional<String> configFile = configFinder.getYAMLFilePath(path, ConfigType.CONFIG_FILE);

		assertFalse(configFile.isPresent(),
				String.format("Invalid path '%s' should not return a config file", invalidFolderPath));
	}

	@Test
	public void getYAMLFilePath_invalidPath_logDebug() {
		Path path = Paths.get("/thatdoesnotexist/forreal");

		configFinder.getYAMLFilePath(path, ConfigType.CONFIG_FILE);

		verify(mockLogger, times(1)).debug(anyString(), eq(path));
	}

	@Test
	public void getYAMLFilePath_multipleMatches_firstReturned() throws IOException {
		String expectedFirstFileName = "CONFIG.YAML";

		List<String> fileNames = Arrays.asList(expectedFirstFileName, "config.yaml", "config.yml", "Config.yaml",
				"Config.yml", "COnfig.yml");

		for (String fileName : fileNames) {
			Path nextFile = new File(tempDirectory.toFile(), fileName).toPath();
			Files.createFile(nextFile);
		}

		Optional<String> configFile = configFinder.getYAMLFilePath(tempDirectory, ConfigType.CONFIG_FILE);

		String configFileName = StringUtils.substringAfterLast(configFile.get(), "/");
		assertEquals(expectedFirstFileName, configFileName);

	}

	@Test
	public void getYAMLFilePath_multipleMatches_lastReturned() throws IOException {
		String expectedFirstFileName = "CONFIG.YAML";

		List<String> fileNames = Arrays.asList("config.yml", "Config.yaml", "Config.yml", "COnfig.yml",
				expectedFirstFileName);

		for (String fileName : fileNames) {
			Path file = new File(tempDirectory.toFile(), fileName).toPath();
			Files.createFile(file);
		}

		Optional<String> configFile = configFinder.getYAMLFilePath(tempDirectory, ConfigType.CONFIG_FILE);

		String configFileName = StringUtils.substringAfterLast(configFile.get(), "/");
		assertEquals(expectedFirstFileName, configFileName);
	}

	@Test
	public void getYAMLFilePath_multipleMatches_3rdReturned() throws IOException {
		String expectedFirstFileName = "CONFIG.YAML";

		List<String> fileNames = Arrays.asList("config.yaml", "Config.yaml", expectedFirstFileName, "Config.yml",
				"COnfig.yml");

		for (String fileName : fileNames) {
			Path file = new File(tempDirectory.toFile(), fileName).toPath();
			Files.createFile(file);
		}

		Optional<String> configFile = configFinder.getYAMLFilePath(tempDirectory, ConfigType.CONFIG_FILE);

		String configFileName = StringUtils.substringAfterLast(configFile.get(), "/");
		assertEquals(expectedFirstFileName, configFileName);
	}

}
