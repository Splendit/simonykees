package eu.jsparrow.core.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class YAMLExcludesTest {

	private static final String MODULE_NAME = "module";
	private static final String PACKAGE_NAME = "package";
	private static final String CLASS_NAME = "class";

	@Test
	public void setExcludeModules_notNullValue_ShouldReturnListWithModules() throws YAMLConfigException {
		YAMLExcludes excludes = new YAMLExcludes();
		excludes.setExcludeModules(Collections.singletonList(MODULE_NAME));

		List<String> excludedModules = excludes.getExcludeModules();

		assertEquals(MODULE_NAME, excludedModules.get(0));
	}

	@Test
	public void setExcludePackages_notNullValue_ShouldReturnListWithPackages() throws YAMLConfigException {
		YAMLExcludes excludes = new YAMLExcludes();
		excludes.setExcludePackages(Collections.singletonList(PACKAGE_NAME));

		List<String> excludedPackages = excludes.getExcludePackages();

		assertEquals(PACKAGE_NAME, excludedPackages.get(0));
	}

	@Test
	public void setExcludeClasses_notNullValue_ShouldReturnListWithPackages() throws YAMLConfigException {
		YAMLExcludes excludes = new YAMLExcludes();
		excludes.setExcludeClasses(Collections.singletonList(CLASS_NAME));

		List<String> excludedClasses = excludes.getExcludeClasses();

		assertEquals(CLASS_NAME, excludedClasses.get(0));
	}

	@Test
	public void setExcludeModules_nullValue_ShouldReturnEmptyList() throws YAMLConfigException {
		YAMLExcludes excludes = new YAMLExcludes();
		excludes.setExcludeModules(null);

		List<String> excludedModules = excludes.getExcludeModules();
		assertTrue(excludedModules.isEmpty());
	}

	@Test
	public void setExcludePackages_nullValue_ShouldReturnEmptyList() throws YAMLConfigException {
		YAMLExcludes excludes = new YAMLExcludes();
		excludes.setExcludePackages(null);

		List<String> excludedPackages = excludes.getExcludePackages();
		assertTrue(excludedPackages.isEmpty());
	}

	@Test
	public void setExcludeClasses_nullValue_ShouldReturnEmptyList() throws YAMLConfigException {
		YAMLExcludes excludes = new YAMLExcludes();
		excludes.setExcludeClasses(null);

		List<String> excludedClasses = excludes.getExcludeClasses();
		assertTrue(excludedClasses.isEmpty());
	}
}
