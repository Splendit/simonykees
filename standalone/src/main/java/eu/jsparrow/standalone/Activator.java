package eu.jsparrow.standalone;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.config.YAMLConfigException;
import eu.jsparrow.core.config.YAMLConfigUtil;
import eu.jsparrow.core.config.YAMLProfile;
import eu.jsparrow.core.exception.ReconcileException;
import eu.jsparrow.core.exception.RefactoringException;
import eu.jsparrow.core.exception.RuleException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.AbstractRefactoringRule;
import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Andreja Sambolec, Matthias Webhofer
 * @since 2.1.1
 */
// @SuppressWarnings("restriction")
public class Activator implements BundleActivator {

	private static final Logger logger = LoggerFactory.getLogger(Activator.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "eu.jsparrow.standalone"; //$NON-NLS-1$

	public static final String USER_DIR = "user.dir"; //$NON-NLS-1$
	public static final String JAVA_TMP = "java.io.tmpdir"; //$NON-NLS-1$
	public static final String PROJECT_PATH_CONSTANT = "PROJECT.PATH"; //$NON-NLS-1$
	public static final String PROJECT_NAME_CONSTANT = "PROJECT.NAME"; //$NON-NLS-1$
	public static final String JSPARROW_TEMP_FOLDER = "temp_jSparrow"; //$NON-NLS-1$
	public static final String DEPENDENCIES_FOLDER_CONSTANT = "deps"; //$NON-NLS-1$
	public static final String MAVEN_NATURE_CONSTANT = "org.eclipse.m2e.core.maven2Nature"; //$NON-NLS-1$
	public static final String PROJECT_DESCRIPTION_CONSTANT = ".project"; //$NON-NLS-1$
	public static final String CONFIG_FILE_PATH = "CONFIG.FILE.PATH"; //$NON-NLS-1$
	public static final String SELECTED_PROFILE = "PROFILE.SELECTED"; //$NON-NLS-1$

	private StandaloneConfig standaloneConfig;

	@Override
	public void start(BundleContext context) throws Exception {
		logger.info(Messages.Activator_start);

		String configFilePath = context.getProperty(CONFIG_FILE_PATH);
		
		String loggerInfo = NLS.bind(Messages.Activator_standalone_LoadingConfiguration, configFilePath);
		logger.info(loggerInfo);

		String profile = context.getProperty(SELECTED_PROFILE);
		loggerInfo = NLS.bind(Messages.Activator_standalone_SelectedProfile, profile);
		logger.info(loggerInfo);

		YAMLConfig config = readConfig(configFilePath, profile);

		// get project path and name from context
		String projectPath = context.getProperty(PROJECT_PATH_CONSTANT);
		String projectName = context.getProperty(PROJECT_NAME_CONSTANT);

		// Set working directory to temp_jSparrow in java tmp folder
		String file = System.getProperty(JAVA_TMP);
		File directory = new File(file + File.separator + JSPARROW_TEMP_FOLDER).getAbsoluteFile();
		if (directory.exists() || directory.mkdirs()) {
			System.setProperty(USER_DIR, directory.getAbsolutePath());
		}

		standaloneConfig = new StandaloneConfig(projectName, projectPath);

		List<AbstractRefactoringRule<? extends AbstractASTRewriteASTVisitor>> selectedRules = getSelectedRulesFromConfig(config,
				standaloneConfig.getJavaProject());

		// Create refactoring pipeline and set rules
		RefactoringPipeline refactoringPipeline = new RefactoringPipeline();
		refactoringPipeline.setRules(selectedRules);
		loggerInfo = NLS.bind(Messages.Activator_standalone_SelectedRules, selectedRules.size(), selectedRules.toString());
		logger.info(loggerInfo);

		logger.info(Messages.Activator_debug_collectCompilationUnits);
		List<ICompilationUnit> compUnits = standaloneConfig.getCompUnits();
		loggerInfo = NLS.bind(Messages.Activator_debug_numCompilationUnits, compUnits.size());
		logger.debug(loggerInfo);

		logger.debug(Messages.Activator_debug_createRefactoringStates);
		refactoringPipeline.createRefactoringStates(compUnits);
		loggerInfo = NLS.bind(Messages.Activator_debug_numRefactoringStates, refactoringPipeline.getRefactoringStates().size());
		logger.debug(loggerInfo);

		// Do refactoring
		try {
			logger.info(Messages.Activator_debug_startRefactoring);
			refactoringPipeline.doRefactoring(new NullProgressMonitor());
		} catch (RefactoringException | RuleException e) {
			logger.error(e.getMessage(), e);
			return;
		}

		// Commit refactoring
		try {
			logger.info(Messages.Activator_debug_commitRefactoring);
			refactoringPipeline.commitRefactoring();
		} catch (RefactoringException | ReconcileException e) {
			logger.error(e.getMessage(), e);
			return;
		}
	}

	@Override
	public void stop(BundleContext context) {
		try {
			/* Unregister as a save participant */
			if (ResourcesPlugin.getWorkspace() != null) {
				ResourcesPlugin.getWorkspace().forgetSavedTree(PLUGIN_ID);
				ResourcesPlugin.getWorkspace().removeSaveParticipant(PLUGIN_ID);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			standaloneConfig.cleanUp();
		}

		logger.info(Messages.Activator_stop);
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
	private List<AbstractRefactoringRule<? extends AbstractASTRewriteASTVisitor>> getSelectedRulesFromConfig(YAMLConfig config,
			IJavaProject javaProject) throws YAMLConfigException {
		List<AbstractRefactoringRule<? extends AbstractASTRewriteASTVisitor>> result  = new LinkedList<>();

		List<AbstractRefactoringRule<? extends AbstractASTRewriteASTVisitor>> projectRules = RulesContainer
				.getRulesForProject(javaProject, true);

		String defaultProfile = config.getDefaultProfile();
		if (defaultProfile != null && !defaultProfile.isEmpty()) {
			if (checkProfileExistence(config, defaultProfile)) {
				Optional<YAMLProfile> configProfile = config.getProfiles().stream()
						.filter(profile -> profile.getName().equals(defaultProfile)).findFirst();

				if (configProfile.isPresent()) {
					List<AbstractRefactoringRule<? extends AbstractASTRewriteASTVisitor>> profileRules = getConfigRules(
							configProfile.get().getRules());

					result = projectRules.stream().filter(rule -> rule.isEnabled())
							.filter(profileRules::contains).collect(Collectors.toList());
				} else {
					String exceptionMessage = NLS.bind(Messages.Activator_standalone_DefaultProfileDoesNotExist, defaultProfile)
;					throw new YAMLConfigException(exceptionMessage);
				}
			} else {
				String exceptionMessage = NLS.bind(Messages.Activator_standalone_DefaultProfileDoesNotExist, defaultProfile);
				throw new YAMLConfigException(exceptionMessage);
			}
		} else { // use all rules from config file
			List<AbstractRefactoringRule<? extends AbstractASTRewriteASTVisitor>> configSelectedRules = getConfigRules(
					config.getRules());

			result = projectRules.stream().filter(AbstractRefactoringRule::isEnabled)
					.filter(configSelectedRules::contains).collect(Collectors.toList());
		}

		return result;
	}

	/**
	 * this method takes a list of rule IDs and produces a list of rules
	 * 
	 * @param configRules
	 *            rule IDs
	 * @return list of rules ({@link AbstractRefactoringRule})
	 * @throws YAMLConfigException
	 *             is thrown if a given rule ID does not exist
	 */
	private List<AbstractRefactoringRule<? extends AbstractASTRewriteASTVisitor>> getConfigRules(List<String> configRules)
			throws YAMLConfigException {
		List<AbstractRefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = RulesContainer.getAllRules(true);
		List<AbstractRefactoringRule<? extends AbstractASTRewriteASTVisitor>> configSelectedRules = new LinkedList<>();
		List<String> nonExistentRules = new LinkedList<>();

		for (String configRule : configRules) {
			Optional<AbstractRefactoringRule<? extends AbstractASTRewriteASTVisitor>> currentRule = rules.stream()
					.filter(rule -> rule.getId().equals(configRule)).findFirst();
			if (currentRule.isPresent()) {
				configSelectedRules.add(currentRule.get());
			} else {
				nonExistentRules.add(configRule);
			}
		}

		if (!nonExistentRules.isEmpty()) {
			String exceptionMessage = NLS.bind(Messages.Activator_standalone_RulesDoNotExist, nonExistentRules.toString());
			throw new YAMLConfigException(exceptionMessage);
		}

		return configSelectedRules;
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
	private YAMLConfig readConfig(String configFilePath, String profile) throws YAMLConfigException {
		YAMLConfig config = null;
		if (configFilePath != null && !configFilePath.isEmpty()) {
			File configFile = new File(configFilePath);
			if (configFile.exists() && !configFile.isDirectory()) {
				config = YAMLConfigUtil.loadConfiguration(configFile);
				String loggerInfo = NLS.bind(Messages.Activator_standalone_ConfigFileReadSuccessfully, configFilePath);
				logger.info(loggerInfo);
				logger.debug(config.toString());
			}
		}

		if (config == null) {
			config = YAMLConfig.getDefaultConfig();
			logger.warn(Messages.Activator_standalone_UsingDefaultConfiguration);
		}

		if (profile != null && !profile.isEmpty()) {
			if (checkProfileExistence(config, profile)) {
				config.setDefaultProfile(profile);
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
	private boolean checkProfileExistence(YAMLConfig config, String profile) {
		return config.getProfiles().stream().anyMatch(configProfile -> configProfile.getName().equals(profile));
	}
}
