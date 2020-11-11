package eu.jsparrow.standalone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.standalone.exceptions.StandaloneException;

@RunWith(MockitoJUnitRunner.class)
public class ExcludedModulesTest {

	private static final String SOME_PROJECT = "someProject";

	private ExcludedModules excludedModules;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private YAMLConfig yamlConfigStub;
	
	@Test
	public void get_useDefaultConfig_shouldReturnEmptyExclusions() throws StandaloneException {
		excludedModules = new TestableExcludedModules(true, SOME_PROJECT);

		List<String> result = excludedModules.get();

		assertTrue("Exlusions should be empty", result.isEmpty());
	}

	@Test
	public void get_withEmptyRootConfig_shouldReturnEmptyExclusions() throws StandaloneException {
		excludedModules = new TestableExcludedModules(false, "");

		List<String> result = excludedModules.get();

		assertTrue("Exlusions should be empty", result.isEmpty());
	}

	@Test
	public void get_withValidYmlConfig_shouldReturnExclusionsFromConfig() throws StandaloneException {
		excludedModules = new TestableExcludedModules(false, SOME_PROJECT);
		List<String> exclusionsFromConfig = Collections.singletonList("exludedModule");
		when(yamlConfigStub.getExcludes()
			.getExcludeModules()).thenReturn(exclusionsFromConfig);

		List<String> result = excludedModules.get();

		assertEquals(exclusionsFromConfig, result);
	}

	@Test
	public void get_withValidYmlConfig_shouldReturnEmptyExclusionsList() throws StandaloneException {
		excludedModules = new TestableExcludedModules(false, SOME_PROJECT);
		List<String> exclusionsFromConfig = Collections.emptyList();
		when(yamlConfigStub.getExcludes()
			.getExcludeModules()).thenReturn(exclusionsFromConfig);

		List<String> result = excludedModules.get();

		assertTrue(result.isEmpty());
	}

	@Test
	public void get_WithEmptyExcludes_shouldReturnEmptyExclusionsList() {
		excludedModules = new TestableExcludedModules(false, SOME_PROJECT);
		when(yamlConfigStub.getExcludes()).thenReturn(null);

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
