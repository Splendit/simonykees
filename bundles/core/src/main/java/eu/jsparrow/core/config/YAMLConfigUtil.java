package eu.jsparrow.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;

/**
 * This class provides useful methods to deal with YAML configuration. the used
 * library for YAML parsing is snakeyaml.
 * (https://bitbucket.org/asomov/snakeyaml)
 * 
 * @author Matthias Webhofer
 * @since 2.2.2
 */
public class YAMLConfigUtil {

	private static final Logger logger = LoggerFactory.getLogger(YAMLConfigUtil.class);

	private static final String CONFIG_TAG = "!jsparrow.config"; //$NON-NLS-1$

	private YAMLConfigUtil() {
		// private constructor to hide public default constructor
	}

	/**
	 * loads a jsparrow configuration file and returns a {@link YAMLConfig}
	 * 
	 * @param file
	 *            configuration file (e.g. jsparrow.yml)
	 * @return the configuration stored in the configuration file
	 * @throws YAMLConfigException
	 */
	public static YAMLConfig loadConfiguration(File file) throws YAMLConfigException {
		YAMLConfig config = null;
		try (FileInputStream fis = new FileInputStream(file)) {

			/*
			 * The TypeDescription specifies the type of the configuration class
			 * and of the containing list of profiles because generics are a
			 * compile time thing. see exportConfig method.
			 */
			TypeDescription rootTypeDescription = new TypeDescription(YAMLConfig.class, CONFIG_TAG);
			rootTypeDescription.addPropertyParameters("profiles", YAMLProfile.class); //$NON-NLS-1$

			/*
			 * the constructor is used for the configuration of snakeyaml
			 */
			Constructor constructor = new Constructor(YAMLConfig.class);
			constructor.addTypeDescription(rootTypeDescription);

			LoaderOptions loaderOptions = new LoaderOptions();
			loaderOptions.setAllowDuplicateKeys(false);

			Yaml yaml = new Yaml(constructor, new Representer(), new DumperOptions(), loaderOptions);

			config = yaml.loadAs(fis, YAMLConfig.class);
		} catch (YAMLException | IOException e) {
			throw new YAMLConfigException(e.getLocalizedMessage(), e);
		}
		return config;
	}

	/**
	 * exports configuration in form of {@link YAMLConfig} to the given file.
	 * this is also used for exporting profiles from jsparrow eclipse version.
	 * 
	 * @param config
	 *            configuration for export
	 * @param file
	 *            configuratin file
	 * @throws YAMLConfigException
	 */
	public static void exportConfig(YAMLConfig config, File file) throws YAMLConfigException {
		try (FileWriter fw = new FileWriter(file)) {
			/*
			 * the Representer is used to put an alias type into the YAML file.
			 * Otherwise the fully qualified class name would be used and we
			 * could run into troubles with obfuscation.
			 */
			Representer representer = new Representer();
			representer.addClassTag(YAMLConfig.class, new Tag(CONFIG_TAG));

			DumperOptions options = new DumperOptions();
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

			Yaml yaml = new Yaml(representer, options);
			yaml.dump(config, fw);

			logger.info("config file exported to {}", file.getAbsolutePath()); //$NON-NLS-1$
		} catch (IOException e) {
			throw new YAMLConfigException(e.getMessage(), e);
		}
	}

	private static boolean isRuleExistent(String ruleId, boolean isStandalone) {
		List<RefactoringRule> rules = RulesContainer.getAllRules(isStandalone);
		for (RefactoringRule rule : rules) {
			if (rule.getId()
				.equals(ruleId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * searches the given list of ruleIds for non-existent rules and returns
	 * them
	 * 
	 * @param ruleIds
	 * @return
	 */
	public static List<String> getNonExistentRules(List<String> ruleIds, boolean isStandalone) {
		return ruleIds.stream()
			.filter(ruleId -> !isRuleExistent(ruleId, isStandalone))
			.collect(Collectors.toList());
	}

	/**
	 * reads the configuration file with the provided path. if no configuration
	 * file is specified the default configuration will be used.
	 * 
	 * @param configFilePath
	 *            path to the configuration file
	 * @return jSparrow configuration
	 * @throws YAMLConfigException
	 *             if an error occurs while loading of the file
	 */
	public static YAMLConfig readConfig(String configFilePath) throws YAMLConfigException {
		YAMLConfig config = null;
		if (configFilePath != null && !configFilePath.isEmpty()) {
			File configFile = new File(configFilePath);
			if (configFile.exists() && !configFile.isDirectory()) {
				String configFileExtension = StringUtils.substringAfterLast(configFile.getAbsolutePath(), "."); //$NON-NLS-1$
				if ("yml".equalsIgnoreCase(configFileExtension) //$NON-NLS-1$
						|| "yaml".equalsIgnoreCase(configFileExtension)) { //$NON-NLS-1$
					config = YAMLConfigUtil.loadConfiguration(configFile);
				}
			}
		}

		if (config == null) {
			String exceptionMessage = NLS.bind(Messages.YAMLConfigUtil_providedPathNotLeadingToYAMLConfig,
					configFilePath);
			throw new YAMLConfigException(exceptionMessage);
		}

		return config;
	}

	/**
	 * checks if the given profile exists in the configuration
	 * 
	 * @param config
	 *            jsparrow configuration
	 * @param profile
	 *            selected profile
	 * @return true, if the profile exists, false otherwise
	 */
	public static boolean checkProfileExistence(YAMLConfig config, String profile) {
		return config.getProfiles()
			.stream()
			.anyMatch(configProfile -> configProfile.getName()
				.equals(profile));
	}

	/**
	 * Updates the selected profile of the configuration.
	 * 
	 * @param config
	 *            the {@link YAMLConfig} to be updated
	 * @param profile
	 *            the selected profile name
	 * 
	 * @throws YAMLConfigException
	 *             if the provided profile does not exist.
	 */
	public static void updateSelectedProfile(YAMLConfig config, String profile) throws YAMLConfigException {
		if (profile == null || profile.isEmpty()) {
			return;
		}

		if (YAMLConfigUtil.checkProfileExistence(config, profile)) {
			config.setSelectedProfile(profile);
		} else {
			String exceptionMessage = NLS.bind(Messages.Activator_standalone_DefaultProfileDoesNotExist, profile);
			throw new YAMLConfigException(exceptionMessage);
		}
	}
}
