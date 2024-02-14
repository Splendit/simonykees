package eu.jsparrow.standalone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.config.YAMLLoggerRule;
import eu.jsparrow.core.config.YAMLProfile;
import eu.jsparrow.core.config.YAMLRenamingRule;
import eu.jsparrow.standalone.exceptions.StandaloneException;

public class YAMLConfigurationWrapperTest {

	private static final String RESOURCE_DIRECTORY = "src/test/resources";

	private YAMLConfigurationWrapper yamlConfigurationWrapper;

	@BeforeEach
	public void setUp() {
		yamlConfigurationWrapper = new YAMLConfigurationWrapper();
	}

	@Test
	public void readConfiguration_invalidPath_shouldThrowException() {
		StandaloneException exception = assertThrows(StandaloneException.class,
				() -> yamlConfigurationWrapper.readConfiguration("i/dont/exist", "profile"));
		assertEquals("A configuration file has not been found at the given path [i/dont/exist]",
				exception.getMessage());
	}

	@Test
	public void readConfiguration_invalidExistingYamlFile_shouldReturnDefaultConfiguration() {
		assertThrows(StandaloneException.class,
				() -> yamlConfigurationWrapper.readConfiguration(loadResource("invalid.yaml").getPath(), "profile"));
	}

	@Test
	public void readConfiguration_validYamlFileInvalidProfile_shouldReturnDefaultConfiguration() {
		assertThrows(StandaloneException.class,
				() -> yamlConfigurationWrapper.readConfiguration(loadResource("valid.yaml").getPath(),
						"invalid-profile"));
	}

	@Test
	public void readConfiguration_validYamlFile_shouldReturnDefaultConfiguration() throws StandaloneException {
		YAMLConfig ymlConfig = yamlConfigurationWrapper.readConfiguration(loadResource("valid.yaml").getPath(), "aaa");

		assertThat(ymlConfig, hasProperty("selectedProfile", equalTo("aaa")));
	}

	@Test
	public void updateProfile_shouldSetSelectedProfile() throws StandaloneException {
		String profileName = "profile-name"; //$NON-NLS-1$
		YAMLConfig yamlConfig = new YAMLConfig();
		yamlConfig.setProfiles(Collections.singletonList(
				new YAMLProfile(profileName, Collections.emptyList(), new YAMLRenamingRule(), new YAMLLoggerRule())));

		yamlConfigurationWrapper.updateSelectedProfile(yamlConfig, profileName);

		assertEquals(profileName, yamlConfig.getSelectedProfile());
	}

	@Test
	public void updateProfile_NonExistingProfile_shouldThrowException() throws StandaloneException {
		String profileName = "profile-name"; //$NON-NLS-1$
		YAMLConfig yamlConfig = new YAMLConfig();
		yamlConfig.setProfiles(Collections.singletonList(
				new YAMLProfile(profileName, Collections.emptyList(), new YAMLRenamingRule(), new YAMLLoggerRule())));

		StandaloneException exception = assertThrows(StandaloneException.class,
				() -> yamlConfigurationWrapper.updateSelectedProfile(yamlConfig, "INVALID"));
		assertEquals("Profile [INVALID] does not exist", exception.getMessage());
	}

	private File loadResource(String resource) {
		return Paths.get(String.join("/", RESOURCE_DIRECTORY, resource))
			.toFile();
	}

}
