package eu.jsparrow.independent;

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

/**
 * Contains functionality for finding the excluded modules from the yaml
 * configuration file in the project root.
 * 
 * @since 2.6.0
 *
 */
public class ExcludedModules {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private boolean useDefaultConfig;
	private String rootProjectConfig;

	public ExcludedModules(Boolean useDefaultConfig, String rootProjectConfig) {
		this.useDefaultConfig = useDefaultConfig;
		this.rootProjectConfig = rootProjectConfig;
	}

	public List<String> get() {

		String logInfo;
		if (useDefaultConfig) {
			/*
			 * No modules are excluded with the default configuration
			 */
			logInfo = "No excluded modules. Using the default configuration."; //$NON-NLS-1$
			logger.debug(logInfo);
			return Collections.emptyList();
		}

		if (rootProjectConfig == null || rootProjectConfig.isEmpty()) {
			logInfo = "Cannot find excluded modules. The root yml file path is not provided"; //$NON-NLS-1$
			logger.debug(logInfo);
			return Collections.emptyList();
		}

		YAMLConfig rootYamlConfig;
		try {
			rootYamlConfig = getRootYamlConfig(rootProjectConfig);
		} catch (YAMLConfigException e) {
			logger.warn("Cannot find excluded modules. The provided file {} cannot be read", rootProjectConfig); //$NON-NLS-1$
			return Collections.emptyList();
		}

		YAMLExcludes excludes = rootYamlConfig.getExcludes();
		if (null == excludes) {
			return Collections.emptyList();
		}
		List<String> excludedModules = excludes.getExcludeModules();
		if (!excludedModules.isEmpty()) {
			logInfo = String.format("Excluded modules: %s ", excludedModules.stream() //$NON-NLS-1$
				.collect(Collectors.joining(","))); //$NON-NLS-1$
		} else {
			logInfo = "No excluded modules were found in the configuration file."; //$NON-NLS-1$
		}
		logger.debug(logInfo);
		return excludedModules;

	}

	protected YAMLConfig getRootYamlConfig(String rootProjectConfig) throws YAMLConfigException {
		return YAMLConfigUtil.readConfig(rootProjectConfig);
	}

}
