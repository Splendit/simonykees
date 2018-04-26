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
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

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
	 * @throws YAMLConfigException
	 * @throws MavenInvocationException
	 * @throws CoreException
	 * @throws IOException
	 */
	public void startRefactoring(BundleContext context, RefactoringPipeline refactoringPipeline)
			throws YAMLConfigException, CoreException, MavenInvocationException, IOException {

		List<StandaloneConfig> configs = loadStandaloneConfig(context);
		setStandaloneConfigurations(configs);
		for (StandaloneConfig config : configs) {
			startRefactoring(context, refactoringPipeline, config);
		}
	}

	public void startRefactoring(BundleContext context, RefactoringPipeline refactoringPipeline,
			StandaloneConfig standaloneConfig) throws YAMLConfigException {
		String loggerInfo;

		YAMLConfig config = getConfiguration(context, standaloneConfig.getProjectId());

		List<RefactoringRule> projectRules = getProjectRules(standaloneConfig);
		List<RefactoringRule> selectedRules = getSelectedRules(config,
				projectRules);
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

			refactoringPipeline.createRefactoringStates(compUnits);

			loggerInfo = NLS.bind(Messages.Activator_debug_numRefactoringStates,
					refactoringPipeline.getRefactoringStates()
						.size());
			logger.debug(loggerInfo);

			// Do refactoring
			try {
				logger.info(Messages.Activator_debug_startRefactoring);
				refactoringPipeline.doRefactoring(new NullProgressMonitor());
			} catch (RefactoringException | RuleException e) {
				logger.debug(e.getMessage(), e);
				logger.error(e.getMessage());
				return;
			}

			loggerInfo = NLS.bind(Messages.SelectRulesWizard_rules_with_changes,
					getJavaProject(standaloneConfig).getElementName(),
					refactoringPipeline.getRulesWithChangesAsString());
			logger.info(loggerInfo);

			// Commit refactoring
			try {
				logger.info(Messages.Activator_debug_commitRefactoring);
				refactoringPipeline.commitRefactoring();
			} catch (RefactoringException | ReconcileException e) {
				logger.debug(e.getMessage(), e);
				logger.error(e.getMessage());
				return;
			}
		} else {
			logger.info(Messages.Activator_standalone_noRulesSelected);
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
	 * @throws YAMLConfigException
	 */
	private YAMLConfig getConfiguration(BundleContext context, String projectId) throws YAMLConfigException {

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
	 * @throws CoreException
	 * @throws MavenInvocationException
	 * @throws IOException
	 */
	protected List<StandaloneConfig> loadStandaloneConfig(BundleContext context)
			throws CoreException, MavenInvocationException, IOException {

		Map<String, String> projectPaths = findAllProjectPaths(context);
		String mavenHome = context.getProperty(MAVEN_HOME_KEY);

		List<StandaloneConfig> configs = new ArrayList<>();
		for (Map.Entry<String, String> entry : projectPaths.entrySet()) {
			String id = entry.getKey();
			String path = entry.getValue();
			String compilerCompliance = context.getProperty(PROJECT_JAVA_VERSION + DOT + id);
			StandaloneConfig standaloneConfig = new StandaloneConfig(id, path, compilerCompliance, mavenHome);
			configs.add(standaloneConfig);
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

	protected List<RefactoringRule> getProjectRules(
			StandaloneConfig standaloneConfig) {
		logger.debug(Messages.RefactoringInvoker_GetEnabledRulesForProject);
		return RulesContainer.getRulesForProject(standaloneConfig.getJavaProject(), true);
	}

	protected List<RefactoringRule> getSelectedRules(YAMLConfig config,
			List<RefactoringRule> projectRules) throws YAMLConfigException {
		logger.debug(Messages.RefactoringInvoker_GetSelectedRules);
		return YAMLConfigUtil.getSelectedRulesFromConfig(config, projectRules);
	}

	protected YAMLConfig getYamlConfig(String configFilePath, String profile) throws YAMLConfigException {
		return YAMLConfigUtil.readConfig(configFilePath, profile);
	}

	protected IJavaProject getJavaProject(StandaloneConfig standaloneConfig) {
		return standaloneConfig.getJavaProject();
	}

	private void setStandaloneConfigurations(List<StandaloneConfig> configs) {
		this.standaloneConfigs = configs;
	}

}
