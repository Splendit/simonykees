package eu.jsparrow.standalone;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * @since 2.6.0
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("nls")
public class ConfigFinderTest {

	private ConfigFinder configFinder;

	@Mock
	private Logger mockLogger;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void setUp() {
		configFinder = new ConfigFinder();
		configFinder.setLogger(mockLogger);
	}

	@Test
	public void getYAMLFilePath_validConfigFile_isPresentTrue() throws IOException {
		String configFileName = "config.yml";
		folder.newFile(configFileName);

		Path path = Paths.get(folder.getRoot()
			.getAbsolutePath());

		Optional<String> configFile = configFinder.getYAMLFilePath(path);

		assertTrue(String.format("Valid config file '%s' should be found", configFileName), configFile.isPresent());
	}

	@Test
	public void getYAMLFilePath_invalidConfigFile_isPresentFalse() throws IOException {
		String configFileName = "_config.yml";
		folder.newFile(configFileName);

		Path path = Paths.get(folder.getRoot()
			.getAbsolutePath());

		Optional<String> configFile = configFinder.getYAMLFilePath(path);

		assertFalse(String.format("Invalid config file '%s' should not be found", configFileName),
				configFile.isPresent());
	}

	@Test
	public void getYAMLFilePath_noConfigFile_isPresentFalse() throws IOException {
		Path path = Paths.get(folder.getRoot()
			.getAbsolutePath());

		Optional<String> configFile = configFinder.getYAMLFilePath(path);

		assertFalse("Config file does not exist and should not be present", configFile.isPresent());
	}

	@Test
	public void getYAMLFilePath_invalidPath_isPresentFalse() throws IOException {
		String invalidFolderPath = "/thatdoesnotexist/forreal";
		Path path = Paths.get(invalidFolderPath);

		Optional<String> configFile = configFinder.getYAMLFilePath(path);

		assertFalse(String.format("Invalid path '%s' should not return a config file", invalidFolderPath),
				configFile.isPresent());
	}

	@Test
	public void getYAMLFilePath_invalidPath_logDebug() throws IOException {
		String invalidFolderPath = "/thatdoesnotexist/forreal";
		Path path = Paths.get(invalidFolderPath);

		configFinder.getYAMLFilePath(path);

		verify(mockLogger, times(1)).debug(anyString(), eq(invalidFolderPath));
	}
}
