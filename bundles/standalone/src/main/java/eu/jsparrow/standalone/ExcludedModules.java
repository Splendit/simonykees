package eu.jsparrow.standalone;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.config.YAMLConfigException;
import eu.jsparrow.core.config.YAMLConfigUtil;
import eu.jsparrow.core.config.YAMLExcludes;
import eu.jsparrow.standalone.exceptions.StandaloneException;

public class ExcludedModules {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private boolean useDefaultConfig;

	private String rootProjectConfig;

	private String selectedProfile;

	public ExcludedModules(Boolean useDefaultConfig, String rootProjectConfig, String selectedProfile) {
		this.useDefaultConfig = useDefaultConfig;
		this.rootProjectConfig = rootProjectConfig;
		this.selectedProfile = selectedProfile;
	}

	public List<String> get() throws StandaloneException {

		String logInfo;
		if (useDefaultConfig) {
			/*
			 * No modules are excluded with the default configuration
			 */
			logInfo = "No excluded modules. Using the default configuration."; //$NON-NLS-1$
			logger.debug(logInfo);
			return Collections.emptyList();
		}

		if (rootProjectConfig.isEmpty()) {
			logInfo = "Cannot find excluded modules. The root yml file path is not provided"; //$NON-NLS-1$
			logger.debug(logInfo);
			return Collections.emptyList();
		}

		YAMLConfig rootYamlConfig = getRootYamlConfig(rootProjectConfig, selectedProfile);
		YAMLExcludes excludes = rootYamlConfig.getExcludes();
		List<String> excludedModules = excludes.getExcludeModules();
		if (!excludedModules.isEmpty()) {
			logInfo = String.format("Excluded modules: %s ", excludedModules.stream() //$NON-NLS-1$
				.collect(Collectors.joining(","))); //$NON-NLS-1$
		} else {
			logInfo = "No excluded modules were found."; //$NON-NLS-1$
		}
		logger.debug(logInfo);
		return excludedModules;

	}

	protected YAMLConfig getRootYamlConfig(String rootProjectConfig)
			throws StandaloneException {
		YAMLConfig rootYamlConfig;
		try {
			rootYamlConfig = YAMLConfigUtil.readConfig(rootProjectConfig);
		} catch (YAMLConfigException e) {
			throw new StandaloneException(e.getMessage(), e);
		}
		return rootYamlConfig;
	}

}
