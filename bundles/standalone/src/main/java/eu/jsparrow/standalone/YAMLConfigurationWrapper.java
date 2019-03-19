package eu.jsparrow.standalone;

import java.io.File;

import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.config.YAMLConfigException;
import eu.jsparrow.core.config.YAMLConfigUtil;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.standalone.exceptions.StandaloneException;

/**
 * A wrapper for {@link YAMLConfig}. Contains functionality for reading a yaml
 * configuration file and for creating a default {@link YAMLConfig}.
 * 
 * @since 2.6.0
 *
 */
public class YAMLConfigurationWrapper {

	private static final Logger logger = LoggerFactory.getLogger(YAMLConfigurationWrapper.class);

	/**
	 * Reads the yaml configuration with the given path
	 * 
	 * @param configFilePath
	 *            path to the yaml file
	 * @param profile
	 *            the preferred jSparrow profile
	 * @return the {@link YAMLConfig} corresponding to the given path or the
	 *         default configuration if the path does not represent an existing
	 *         yaml file.
	 * @throws StandaloneException
	 *             if the configuration is inconsistent. Reasons include:
	 *             <ul>
	 *             <li>The selected profile in the {@link BundleContext} does
	 *             not match any of the declared profiles in the configuration
	 *             file.</li>
	 *             </ul>
	 */
	public YAMLConfig readConfiguration(String configFilePath, String profile) throws StandaloneException {

		YAMLConfig config;

		if (isYamlFilePresent(configFilePath)) {
			try {
				config = getYamlConfig(configFilePath);
			} catch (YAMLConfigException e) {
				throw new StandaloneException(e.getMessage(), e);
			}
		} else {
			String exceptionMessage = NLS.bind(Messages.YAMLConfigurationWrapper_configurationFileNotFoundAtPath,
					configFilePath);
			throw new StandaloneException(exceptionMessage);
		}

		updateSelectedProfile(config, profile);
		String selectedProfile = config.getSelectedProfile();
		String loggerInfo = NLS.bind(Messages.Activator_standalone_SelectedProfile,
				(selectedProfile == null) ? Messages.Activator_standalone_None : selectedProfile);
		logger.info(loggerInfo);
		return config;
	}

	/**
	 * Reads the yml configuration file in the provided path.
	 * 
	 * @param configFilePath
	 *            path to the yml/yaml file
	 * @return the parsed {@link YAMLConfig} file.
	 * @throws StandaloneException
	 *             if the configuration file could not be read
	 */
	private YAMLConfig getYamlConfig(String configFilePath) throws YAMLConfigException {
		return YAMLConfigUtil.readConfig(configFilePath);
	}

	/**
	 * Updates the selected profile of the configuration.
	 * 
	 * @param config
	 *            the {@link YAMLConfig} to be updated
	 * @param profile
	 *            the selected profile name
	 * 
	 * @throws StandaloneException
	 *             if the provided profile does not exist.
	 */
	protected void updateSelectedProfile(YAMLConfig config, String profile) throws StandaloneException {
		try {
			YAMLConfigUtil.updateSelectedProfile(config, profile);
		} catch (YAMLConfigException e) {
			throw new StandaloneException(e.getMessage(), e);
		}

	}

	public YAMLConfig getDefaultYamlConfig() {
		return YAMLConfig.getDefaultConfig();
	}

	private boolean isYamlFilePresent(String configFilePath) {
		if (configFilePath == null || configFilePath.isEmpty()) {
			return false;
		}

		return new File(configFilePath).exists();
	}

}
