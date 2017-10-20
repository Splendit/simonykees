package eu.jsparrow.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.i18n.Messages;

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
			 * the TypeDescription specifies the type of the configuration class
			 * and of the containing list of profiles because generics are a
			 * compile time thing. see exportConfig method.
			 */
			TypeDescription rootTypeDescription = new TypeDescription(YAMLConfig.class, CONFIG_TAG);
			rootTypeDescription.putListPropertyType("profiles", YAMLProfile.class); //$NON-NLS-1$

			/*
			 * the constructor is used for the configuration of snakeyaml
			 */
			Constructor constructor = new Constructor(YAMLConfig.class);
			constructor.addTypeDescription(rootTypeDescription);

			Yaml yaml = new Yaml(constructor);

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

			logger.info("config file exported to " + file.getAbsolutePath()); //$NON-NLS-1$
		} catch (IOException e) {
			throw new YAMLConfigException(e.getMessage(), e);
		}
	}

	/**
	 * this method selects the rules to be applied. for all rules it will be
	 * checked if they are available in general and for the current project. if
	 * a rule does not meet the criteria, it will be filtered.
	 * 
	 * <ul>
	 * <li>the defaultProfile is checked and if it exists its rules will be
	 * used</li>
	 * <li>if no defaultProfile is set the rules in the rules-section of the
	 * configuration file will be used</li>
	 * <li>if the given defaultProfile is not specified or a selected rule does
	 * not exist a {@link YAMLConfigException} will be thrown</li>
	 * </ul>
	 * 
	 * @param config
	 *            configuration
	 * @param javaProject
	 *            the current {@link IJavaElement}
	 * @return a list of rules to be applied on the project
	 * @throws YAMLConfigException
	 */
	public static List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getSelectedRulesFromConfig(
			YAMLConfig config, IJavaProject javaProject) throws YAMLConfigException {
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> result;

		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> projectRules = RulesContainer
			.getRulesForProject(javaProject, true);

		String selectedProfile = config.getSelectedProfile();
		if (selectedProfile != null && !selectedProfile.isEmpty()) {
			if (checkProfileExistence(config, selectedProfile)) {
				Optional<YAMLProfile> configProfile = config.getProfiles()
					.stream()
					.filter(profile -> profile.getName()
						.equals(selectedProfile))
					.findFirst();

				if (configProfile.isPresent()) {
					List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> profileRules = getConfigRules(
							configProfile.get()
								.getRules());

					result = projectRules.stream()
						.filter(RefactoringRule::isEnabled)
						.filter(profileRules::contains)
						.collect(Collectors.toList());
				} else {
					String exceptionMessage = NLS.bind(Messages.Activator_standalone_DefaultProfileDoesNotExist,
							selectedProfile);
					throw new YAMLConfigException(exceptionMessage);
				}
			} else {
				String exceptionMessage = NLS.bind(Messages.Activator_standalone_DefaultProfileDoesNotExist,
						selectedProfile);
				throw new YAMLConfigException(exceptionMessage);
			}
		} else { // use all rules from config file
			List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> configSelectedRules = getConfigRules(
					config.getRules());

			result = projectRules.stream()
				.filter(RefactoringRule::isEnabled)
				.filter(configSelectedRules::contains)
				.collect(Collectors.toList());
		}

		return result;
	}

	/**
	 * this method takes a list of rule IDs and produces a list of rules
	 * 
	 * @param configRules
	 *            rule IDs
	 * @return list of rules ({@link RefactoringRule})
	 * @throws YAMLConfigException
	 *             is thrown if a given rule ID does not exist
	 */
	private static List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getConfigRules(
			List<String> configRules) throws YAMLConfigException {
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = RulesContainer.getAllRules(true);
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> configSelectedRules = new LinkedList<>();
		List<String> nonExistentRules = new LinkedList<>();

		for (String configRule : configRules) {
			Optional<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> currentRule = rules.stream()
				.filter(rule -> rule.getId()
					.equals(configRule))
				.findFirst();
			if (currentRule.isPresent()) {
				configSelectedRules.add(currentRule.get());
			} else {
				nonExistentRules.add(configRule);
			}
		}

		if (!nonExistentRules.isEmpty()) {
			String exceptionMessage = NLS.bind(Messages.Activator_standalone_RulesDoNotExist,
					nonExistentRules.toString());
			throw new YAMLConfigException(exceptionMessage);
		}

		return configSelectedRules;
	}

	/**
	 * checks if a given rule id exists
	 * 
	 * @param ruleId
	 * @return
	 */
	public static boolean isRuleExistent(String ruleId) {
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = RulesContainer.getAllRules(true);
		for (RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule : rules) {
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
	public static List<String> getNonExistentRules(List<String> ruleIds) {
		return ruleIds.stream()
			.filter(ruleId -> !isRuleExistent(ruleId))
			.collect(Collectors.toList());
	}

	/**
	 * reads the configuration file and modifies it according to maven flags. if
	 * no configuration file is specified the default configuration will be
	 * used. if a profile is chosen via maven flags there is a check if the
	 * profile exists.
	 * 
	 * @param configFilePath
	 *            path to the configuration file
	 * @param profile
	 *            selected profile
	 * @return jsparrow configuration
	 * @throws YAMLConfigException
	 *             if an error occurs during loading of the file or if the
	 *             profile does not exist
	 */
	public static YAMLConfig readConfig(String configFilePath, String profile) throws YAMLConfigException {
		YAMLConfig config = null;
		if (configFilePath != null && !configFilePath.isEmpty()) {
			File configFile = new File(configFilePath);
			if (configFile.exists() && !configFile.isDirectory()) {
				config = YAMLConfigUtil.loadConfiguration(configFile);
				String loggerInfo = NLS.bind(Messages.Activator_standalone_ConfigFileReadSuccessfully, configFilePath);
				logger.info(loggerInfo);
				String debugInfo = config.toString();
				logger.debug(debugInfo);
			}
		}

		if (config == null) {
			config = YAMLConfig.getDefaultConfig();
			logger.warn(Messages.Activator_standalone_UsingDefaultConfiguration);
		}

		if (profile != null && !profile.isEmpty()) {
			if (checkProfileExistence(config, profile)) {
				config.setSelectedProfile(profile);
			} else {
				String exceptionMessage = NLS.bind(Messages.Activator_standalone_DefaultProfileDoesNotExist, profile);
				throw new YAMLConfigException(exceptionMessage);
			}
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
	private static boolean checkProfileExistence(YAMLConfig config, String profile) {
		return config.getProfiles()
			.stream()
			.anyMatch(configProfile -> configProfile.getName()
				.equals(profile));
	}
}
