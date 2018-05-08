package eu.jsparrow.standalone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.config.YAMLConfigException;
import eu.jsparrow.core.config.YAMLConfigUtil;
import eu.jsparrow.core.exception.ReconcileException;
import eu.jsparrow.core.exception.RuleException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.standalone.exceptions.StandAloneException;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.4.0
 */
public class RefactoringInvoker {
	private static final Logger logger = LoggerFactory.getLogger(RefactoringInvoker.class);

	// CONSTANTS
	private static final String USER_DIR = "user.dir"; //$NON-NLS-1$
	private static final String PROJECT_JAVA_VERSION = "PROJECT.JAVA.VERSION"; //$NON-NLS-1$
	private static final String JAVA_TMP = "java.io.tmpdir"; //$NON-NLS-1$
	private static final String PROJECT_PATH_CONSTANT = "PROJECT.PATH"; //$NON-NLS-1$
	private static final String JSPARROW_TEMP_FOLDER = "temp_jSparrow"; //$NON-NLS-1$
	private static final String CONFIG_FILE_PATH = "CONFIG.FILE.PATH"; //$NON-NLS-1$
	private static final String SELECTED_PROFILE = "PROFILE.SELECTED"; //$NON-NLS-1$
	private static final String MAVEN_HOME_KEY = "MAVEN.HOME"; //$NON-NLS-1$
	private static final String USE_DEFAULT_CONFIGURATION = "DEFAULT.CONFIG"; //$NON-NLS-1$
	private static final String ALL_PROJECT_IDENTIFIERS = "ALL.PROJECT.IDENTIFIERS"; //$NON-NLS-1$
	private static final String DOT = "."; //$NON-NLS-1$

	protected List<StandaloneConfig> standaloneConfigs = new ArrayList<>();

	public RefactoringInvoker() {
		prepareWorkingDirectory();
	}

	/**
	 * Prepare and start the refactoring process
	 * 
	 * @param context
	 *            the bundle context configuration
	 * @param refactoringPipeline
	 *            an instance of the {@link RefactoringPipeline}
	 * @throws YAMLConfigException
	 *             if the yaml configuration file cannot be loaded
	 * @throws MavenInvocationException
	 *             if the maven invoker for the {@link StandaloneConfig} cannot
	 *             be created.
	 * @throws CoreException
	 *             if an exception occurs when creating an eclipse java project.
	 * @throws IOException
	 *             if the standalone configuration cannot be loaded
	 * @throws StandAloneException
	 * @throws RefactoringException
	 *             if there are no sources to apply the refactoring to
	 */
	public void startRefactoring(BundleContext context, RefactoringPipeline refactoringPipeline)
			throws StandAloneException {

		List<StandaloneConfig> configs = loadStandaloneConfig(context);
		setStandaloneConfigurations(configs);
		computeRefactoring(context, refactoringPipeline, configs);
		commitChanges(refactoringPipeline);
	}

	/**
	 * Prepares the refactoring states and computes the refacotring for the
	 * projects contained in the provided list of
	 * {@link StandaloneConfig}uratios. Does NOT commit the refactoring.
	 * 
	 * @param context
	 *            the bundle context configuration
	 * @param refactoringPipeline
	 *            an instance of the {@link RefactoringPipeline}
	 * @param moduleConfigurations
	 *            the list of the {@link StandaloneConfig} for the projects to
	 *            be refactored.
	 *            
	 * @throws StandAloneException
	 */
	private void computeRefactoring(BundleContext context, RefactoringPipeline refactoringPipeline,
			List<StandaloneConfig> moduleConfigurations) throws StandAloneException {

		for (StandaloneConfig standaloneConfig : moduleConfigurations) {
			String loggerInfo;
			YAMLConfig config = getConfiguration(context, standaloneConfig.getProjectId());

			List<RefactoringRule> projectRules = getProjectRules(standaloneConfig);
			List<RefactoringRule> selectedRules = getSelectedRules(config, projectRules);
			if (selectedRules != null && !selectedRules.isEmpty()) {
				// Create refactoring pipeline and set rules
				refactoringPipeline.setRules(selectedRules);

				loggerInfo = NLS.bind(Messages.Activator_standalone_SelectedRules, selectedRules.size(),
						selectedRules.toString());
				logger.info(loggerInfo);

				logger.info(Messages.Activator_debug_collectCompilationUnits);

				List<ICompilationUnit> compUnits = standaloneConfig.getCompUnits();

				loggerInfo = NLS.bind(Messages.Activator_debug_numCompilationUnits, compUnits.size());
				logger.debug(loggerInfo);

				logger.debug(Messages.Activator_debug_createRefactoringStates);
				try {
					refactoringPipeline.createRefactoringStates(compUnits);
				} catch (JavaModelException e1) {
					throw new StandAloneException(e1.getMessage(), e1);
				}

				loggerInfo = NLS.bind(Messages.Activator_debug_numRefactoringStates,
						refactoringPipeline.getRefactoringStates()
							.size());
				logger.debug(loggerInfo);

				// Do refactoring
				logger.info(Messages.Activator_debug_startRefactoring);
				try {
					refactoringPipeline.doRefactoring(new NullProgressMonitor());
				} catch (RuleException e) {
					logger.debug(e.getMessage(), e);
					logger.error(e.getMessage());
				} catch (RefactoringException e) {
					throw new StandAloneException(e.getMessage(), e);
				}

				loggerInfo = NLS.bind(Messages.SelectRulesWizard_rules_with_changes,
						getJavaProject(standaloneConfig).getElementName(),
						refactoringPipeline.getRulesWithChangesAsString());
				logger.info(loggerInfo);

			} else {
				logger.info(Messages.Activator_standalone_noRulesSelected);
			}
		}
	}

	protected void commitChanges(RefactoringPipeline refactoringPipeline) {
		// Commit refactoring
		try {
			logger.info(Messages.Activator_debug_commitRefactoring);
			refactoringPipeline.commitRefactoring();
		} catch (RefactoringException | ReconcileException e) {
			logger.debug(e.getMessage(), e);
			logger.error(e.getMessage());
		}
	}

	/**
	 * cleans classpath and temp directory
	 * 
	 * @throws IOException
	 */
	public void cleanUp() throws IOException {
		try {
			for (StandaloneConfig standaloneConfig : standaloneConfigs) {
				standaloneConfig.cleanUp();
			}
		} catch (JavaModelException | MavenInvocationException e) {
			logger.debug(e.getMessage(), e);
			logger.error(e.getMessage());
		}
	}

	/**
	 * gets the configuration from the given path in context
	 * 
	 * @param context
	 * @return the read configuration
	 * @throws StandAloneException
	 * @throws YAMLConfigException
	 */
	private YAMLConfig getConfiguration(BundleContext context, String projectId) throws StandAloneException {

		boolean useDefaultConfig = Boolean.parseBoolean(context.getProperty(USE_DEFAULT_CONFIGURATION));

		if (!useDefaultConfig) {
			String configFilePath = context.getProperty(CONFIG_FILE_PATH + DOT + projectId);
			String profile = context.getProperty(SELECTED_PROFILE);

			String loggerInfo = NLS.bind(Messages.Activator_standalone_LoadingConfiguration, configFilePath);
			logger.info(loggerInfo);

			YAMLConfig config = getYamlConfig(configFilePath, profile);

			String selectedProfile = config.getSelectedProfile();

			loggerInfo = NLS.bind(Messages.Activator_standalone_SelectedProfile,
					(selectedProfile == null) ? Messages.Activator_standalone_None : selectedProfile);
			logger.info(loggerInfo);

			return config;
		} else {
			logger.info(Messages.Activator_standalone_UsingDefaultConfiguration);

			return YAMLConfig.getDefaultConfig();
		}
	}

	private void prepareWorkingDirectory() {
		String file = System.getProperty(JAVA_TMP);
		File directory = new File(file + File.separator + JSPARROW_TEMP_FOLDER).getAbsoluteFile();

		if (directory.exists() || directory.mkdirs()) {
			System.setProperty(USER_DIR, directory.getAbsolutePath());
		}
	}

	/**
	 * Loads a new {@link StandaloneConfig} with the properties found in
	 * {@link BundleContext}
	 * 
	 * @param context
	 * @throws StandAloneException 
	 */
	protected List<StandaloneConfig> loadStandaloneConfig(BundleContext context) throws StandAloneException {

		Map<String, String> projectPaths = findAllProjectPaths(context);
		String mavenHome = context.getProperty(MAVEN_HOME_KEY);

		List<StandaloneConfig> configs = new ArrayList<>();
		for (Map.Entry<String, String> entry : projectPaths.entrySet()) {
			String id = entry.getKey();
			String path = entry.getValue();
			String compilerCompliance = context.getProperty(PROJECT_JAVA_VERSION + DOT + id);
			try {
				StandaloneConfig standaloneConfig = new StandaloneConfig(id, path, compilerCompliance, mavenHome);
				configs.add(standaloneConfig);
			} catch (CoreException | MavenInvocationException | IOException e) {
				throw new StandAloneException(e.getMessage(), e);
			}
		}
		return configs;

	}

	private Map<String, String> findAllProjectPaths(BundleContext context) {
		String concatenatedIds = context.getProperty(ALL_PROJECT_IDENTIFIERS);
		Map<String, String> paths = new HashMap<>();
		String[] allIds = concatenatedIds.split(","); //$NON-NLS-1$
		for (String id : allIds) {
			String propertyKey = PROJECT_PATH_CONSTANT + "." + id; //$NON-NLS-1$
			String path = context.getProperty(propertyKey);
			paths.put(id, path);
		}
		return paths;
	}

	protected List<RefactoringRule> getProjectRules(StandaloneConfig standaloneConfig) {
		logger.debug(Messages.RefactoringInvoker_GetEnabledRulesForProject);
		return RulesContainer.getRulesForProject(standaloneConfig.getJavaProject(), true);
	}

	protected List<RefactoringRule> getSelectedRules(YAMLConfig config, List<RefactoringRule> projectRules)
			throws StandAloneException {
		logger.debug(Messages.RefactoringInvoker_GetSelectedRules);
		try {
			return YAMLConfigUtil.getSelectedRulesFromConfig(config, projectRules);
		} catch (YAMLConfigException e) {
			throw new StandAloneException(e.getMessage(), e);
		}
	}

	protected YAMLConfig getYamlConfig(String configFilePath, String profile) throws StandAloneException {
		try {
			return YAMLConfigUtil.readConfig(configFilePath, profile);
		} catch (YAMLConfigException e) {
			throw new StandAloneException(e.getMessage(), e);
		}
	}

	protected IJavaProject getJavaProject(StandaloneConfig standaloneConfig) {
		return standaloneConfig.getJavaProject();
	}

	private void setStandaloneConfigurations(List<StandaloneConfig> configs) {
		this.standaloneConfigs = configs;
	}

}
