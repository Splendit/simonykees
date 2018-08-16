package eu.jsparrow.core.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

public class YAMLExcludesTest {

	private static final String RESOURCE_DIRECTORY = "src/test/resources/eu/jsparrow/core/config";
	private static final String MODULE_NAME = "module";
	private static final String PACKAGE_NAME = "package";
	private static final String CLASS_NAME = "class";

	@Test
	public void loadConfiguration_LoadFileWithSpecifiedExcludes_ShouldReturnListWithModules()
			throws YAMLConfigException {
		YAMLConfig config = YAMLConfigUtil.loadConfiguration(loadResource("specified_modules.yaml"));

		assertNotNull(config);

		List<String> excludedModules = config.getExcludes().getExcludeModules();
		List<String> excludedPackages = config.getExcludes().getExcludePackages();
		List<String> excludedClasses = config.getExcludes().getExcludeClasses();
		assertTrue(!excludedModules.isEmpty());
		assertEquals(MODULE_NAME, excludedModules.get(0));
		assertTrue(!excludedPackages.isEmpty());
		assertEquals(PACKAGE_NAME, excludedPackages.get(0));
		assertTrue(!excludedClasses.isEmpty());
		assertEquals(CLASS_NAME, excludedClasses.get(0));
	}

	@Test
	public void loadConfiguration_LoadFileWithEmptyExcludes_ShouldReturnEmptyListForEach() throws YAMLConfigException {
		YAMLConfig config = YAMLConfigUtil.loadConfiguration(loadResource("empty_modules.yaml"));

		assertNotNull(config);

		List<String> excludedModules = config.getExcludes().getExcludeModules();
		List<String> excludedPackages = config.getExcludes().getExcludePackages();
		List<String> excludedClasses = config.getExcludes().getExcludeClasses();
		assertTrue(excludedModules.isEmpty());
		assertTrue(excludedPackages.isEmpty());
		assertTrue(excludedClasses.isEmpty());
}

	private File loadResource(String resource) {
		return Paths.get(String.join("/", RESOURCE_DIRECTORY, resource)).toFile();
	}

}
