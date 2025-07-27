package eu.jsparrow.standalone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.config.YAMLExcludes;
import eu.jsparrow.standalone.exceptions.StandaloneException;

public class ExcludedModulesTest {

	private static final String SOME_PROJECT = "someProject";

	private ExcludedModules excludedModules;


	private YAMLConfig yamlConfigStub;
	
	@Test
	public void get_useDefaultConfig_shouldReturnEmptyExclusions() throws StandaloneException {
		yamlConfigStub = new YAMLConfig();
		excludedModules = new TestableExcludedModules(true, SOME_PROJECT);

		List<String> result = excludedModules.get();

		assertTrue(result.isEmpty(), "Exlusions should be empty");
	}

	@Test
	public void get_withEmptyRootConfig_shouldReturnEmptyExclusions() throws StandaloneException {
		yamlConfigStub = new YAMLConfig();
		excludedModules = new TestableExcludedModules(false, "");

		List<String> result = excludedModules.get();

		assertTrue(result.isEmpty(), "Exlusions should be empty");
	}

	@Test
	public void get_withValidYmlConfig_shouldReturnExclusionsFromConfig() throws StandaloneException {
		YAMLExcludes yamlExcludes = new YAMLExcludes();
		List<String> exclusionsFromConfig = Collections.singletonList("exludedModule");
		yamlExcludes.setExcludeModules(exclusionsFromConfig);
		yamlConfigStub = new YAMLConfig(null, null, null, yamlExcludes, null, null);
		excludedModules = new TestableExcludedModules(false, SOME_PROJECT);

		List<String> result = excludedModules.get();
		assertEquals(exclusionsFromConfig, result);
	}

	@Test
	public void get_withValidYmlConfig_shouldReturnEmptyExclusionsList() throws StandaloneException {
		YAMLExcludes yamlExcludes = new YAMLExcludes();
		List<String> exclusionsFromConfig = Collections.emptyList();
		yamlExcludes.setExcludeModules(exclusionsFromConfig);
		yamlConfigStub = new YAMLConfig(null, null, null, yamlExcludes, null, null);
		excludedModules = new TestableExcludedModules(false, SOME_PROJECT);		

		List<String> result = excludedModules.get();
		assertTrue(result.isEmpty());
	}

	@Test
	public void get_WithEmptyExcludes_shouldReturnEmptyExclusionsList() {
		excludedModules = new TestableExcludedModules(false, SOME_PROJECT);
		yamlConfigStub = new YAMLConfig(null, null, null, null, null, null);
		assertNull(yamlConfigStub.getExcludes());
		List<String> result = excludedModules.get();

		assertTrue(result.isEmpty());
	}

	private class TestableExcludedModules extends ExcludedModules {

		public TestableExcludedModules(Boolean useDefaultConfig, String rootProjectConfig) {
			super(useDefaultConfig, rootProjectConfig);
		}

		@Override
		protected YAMLConfig getRootYamlConfig(String rootProjectConfig) {
			return yamlConfigStub;
		}
	}

}
