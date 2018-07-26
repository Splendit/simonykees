package eu.jsparrow.standalone;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.config.YAMLProfile;
import eu.jsparrow.standalone.exceptions.StandaloneException;

@SuppressWarnings("nls")
public class YAMLConfigurationWrapperTest {

	private static final String RESOURCE_DIRECTORY = "src/test/resources";

	private YAMLConfigurationWrapper yamlConfigurationWrapper;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() {
		yamlConfigurationWrapper = new YAMLConfigurationWrapper();
	}

	@Test
	public void readConfiguration_invalidPath_shouldReturnDefaultConfiguration() throws StandaloneException {
		YAMLConfig ymlConfig = yamlConfigurationWrapper.readConfiguration("i/dont/exist", "profile");

		assertThat(ymlConfig, hasProperty("selectedProfile", equalTo("default")));
	}

	@Test
	public void readConfiguration_invalidExistingYamlFile_shouldReturnDefaultConfiguration()
			throws StandaloneException {
		YAMLConfig ymlConfig = yamlConfigurationWrapper.readConfiguration(loadResource("invalid.yaml").getPath(),
				"profile");

		assertThat(ymlConfig, hasProperty("selectedProfile", equalTo("default")));
	}

	@Test
	public void readConfiguration_validYamlFileInvalidProfile_shouldReturnDefaultConfiguration()
			throws StandaloneException {
		expectedException.expect(StandaloneException.class);
		yamlConfigurationWrapper.readConfiguration(loadResource("valid.yaml").getPath(), "invalid-profile");
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
		yamlConfig.setProfiles(Collections.singletonList(new YAMLProfile(profileName, Collections.emptyList())));

		yamlConfigurationWrapper.updateSelectedProfile(yamlConfig, profileName);

		assertEquals(profileName, yamlConfig.getSelectedProfile());
	}

	@Test
	public void updateProfile_NonExistingProflie_shouldThrowException() throws StandaloneException {
		String profileName = "profile-name"; //$NON-NLS-1$
		YAMLConfig yamlConfig = new YAMLConfig();
		yamlConfig.setProfiles(Collections.singletonList(new YAMLProfile(profileName, Collections.emptyList())));

		expectedException.expect(StandaloneException.class);
		expectedException.expectMessage("Profile [INVALID] does not exist");
		yamlConfigurationWrapper.updateSelectedProfile(yamlConfig, "INVALID");

		assertTrue(false);
	}

	private File loadResource(String resource) {
		return Paths.get(String.join("/", RESOURCE_DIRECTORY, resource))
			.toFile();
	}

}
