package eu.jsparrow.standalone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.standalone.exceptions.StandaloneException;

@RunWith(MockitoJUnitRunner.class)
public class ExcludedModulesTest {

	private ExcludedModules excludedModules;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private YAMLConfig yamlConfigStub;

	@Test
	public void get_useDefaultConfig_shouldReturnEmptyExclusions() throws StandaloneException {
		excludedModules = new TestableExcludedModules(true, "someProject", "selectedProfile");

		List<String> result = excludedModules.get();

		assertTrue("Exlusions should be empty", result.isEmpty());
	}

	@Test
	public void get_withEmptyRootConfig_shouldReturnEmptyExclusions() throws StandaloneException {
		excludedModules = new TestableExcludedModules(false, "", "selectedProfile");

		List<String> result = excludedModules.get();

		assertTrue("Exlusions should be empty", result.isEmpty());
	}

	@Test
	public void get_withValidProfile_shouldReturnExlusionsFromConfig() throws StandaloneException {
		excludedModules = new TestableExcludedModules(false, "someProject", "selectedProfile");
		List<String> exclusionsFromConfig = Collections.singletonList("exludedModule"); 
		when(yamlConfigStub.getExcludes()
			.getExcludeModules()).thenReturn(exclusionsFromConfig);

		List<String> result = excludedModules.get();

		assertEquals(exclusionsFromConfig, result);
	}
	
	private class TestableExcludedModules extends ExcludedModules {

		public TestableExcludedModules(Boolean useDefaultConfig, String rootProjectConfig, String selectedProfile) {
			super(useDefaultConfig, rootProjectConfig, selectedProfile);
		}

		@Override
		protected YAMLConfig getRootYamlConfig(String rootProjectConfig, String selectedProfile)
				throws StandaloneException {
			return yamlConfigStub;
		}
	}

}
