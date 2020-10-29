package eu.jsparrow.standalone;

import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertEquals;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;

import org.junit.Test;

@SuppressWarnings("nls")
public class YAMLStandaloneConfigTest {

	private YAMLStandaloneConfig config;

	@Test
	public void load_whenFileNotExists_shouldReturnDefaultConfig() throws Exception {
		config = YAMLStandaloneConfig.load(new File("src/test/resources/doesnotexist"));

		assertThat(config.getKey(), isEmptyString());
	}

	@Test
	public void load_withValidConfigFile_shouldLoadConfig() throws Exception {
		config = YAMLStandaloneConfig.load(new File("src/test/resources/standalone-config.yaml"));

		assertEquals("Test123", config.getKey());
		assertEquals("http://localhost:8080", config.getUrl());
	}

}
