package eu.jsparrow.standalone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

public class YAMLStandaloneConfigTest {

	private YAMLStandaloneConfig config;

	@Test
	public void load_whenFileNotExists_shouldReturnDefaultConfig() throws Exception {
		config = YAMLStandaloneConfig.load(new File("src/test/resources/doesnotexist"));
		assertTrue(config.getKey().isEmpty());
	}

	@Test
	public void load_withValidConfigFile_shouldLoadConfig() throws Exception {
		config = YAMLStandaloneConfig.load(new File("src/test/resources/standalone-config.yaml"));

		assertEquals("Test123", config.getKey());
		assertEquals("http://localhost:8080", config.getUrl());
	}

}
