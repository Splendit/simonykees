package eu.jsparrow.standalone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import eu.jsparrow.standalone.exceptions.StandaloneException;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.4.0
 */
public class RefactoringInvoker {
	private static final Logger logger = LoggerFactory.getLogger(RefactoringInvoker.class);

	/**
	 * The following constants represent some keys in the BundleContext and they
	 * must match with the ones in {@link eu.jsparrow.adapter.MavenAdapter}
	 */
	private static final String USER_DIR = "user.dir"; //$NON-NLS-1$
	private static final String PROJECT_JAVA_VERSION = "PROJECT.JAVA.VERSION"; //$NON-NLS-1$
	private static final String JAVA_TMP = "java.io.tmpdir"; //$NON-NLS-1$
	private static final String PROJECT_PATH_CONSTANT = "PROJECT.PATH"; //$NON-NLS-1$
	private static final String JSPARROW_TEMP_FOLDER = "temp_jSparrow"; //$NON-NLS-1$
	private static final String CONFIG_FILE_PATH = "CONFIG.FILE.PATH"; //$NON-NLS-1$
	private static final String SELECTED_PROFILE = "PROFILE.SELECTED"; //$NON-NLS-1$
	private static final String USE_DEFAULT_CONFIGURATION = "DEFAULT.CONFIG"; //$NON-NLS-1$
	private static final String ROOT_CONFIG_PATH = "ROOT.CONFIG.PATH"; //$NON-NLS-1$
	private static final String ALL_PROJECT_IDENTIFIERS = "ALL.PROJECT.IDENTIFIERS"; //$NON-NLS-1$
	private static final String SOURCE_FOLDER = "SOURCE.FOLDER"; //$NON-NLS-1$
	private static final String NATURE_IDS = "NATURE.IDS"; //$NON-NLS-1$
	private static final String PROJECT_NAME = "PROJECT.NAME"; //$NON-NLS-1$

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
	 * @throws StandaloneException
	 *             if an exception occurs during refactoring. Reasons include:
	 *             <ul>
	 *             <li>The yaml configuration file cannot be found</li>
	 *             <li>The eclipse project cannot be created from the
	 *             sources</li>
	 *             <li>The list of refactoring states cannot be created in the
	 *             {@link RefactoringPipeline}</li>
	 *             <li>A {@link RefactoringException} is thrown while computing
	 *             refactoring
	 *             <li>
	 *             <li>All source files contain compilation errors</li>
	 *             <ul>
	 */
	public void startRefactoring(BundleContext context, RefactoringPipeline refactoringPipeline)
			throws StandaloneException {

		loadStandaloneConfig(context);
		computeRefactoring(context, refactoringPipeline, standaloneConfigs);
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
	 * @throws StandaloneException
	 */
	private void computeRefactoring(BundleContext context, RefactoringPipeline refactoringPipeline,
			List<StandaloneConfig> moduleConfigurations) throws StandaloneException {

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

				logger.debug(Messages.Activator_debug_createRefactoringStates);
				
				try {
					CompilationUnitProvider compilationUnitProvider = new CompilationUnitProvider(standaloneConfig, config.getExcludes());
					refactoringPipeline.createRefactoringStates(compilationUnitProvider.getFilteredCompilationUnits());
				} catch (JavaModelException e1) {
					throw new StandaloneException(e1.getMessage(), e1);
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
					throw new StandaloneException(e.getMessage(), e);
				}

				loggerInfo = NLS.bind(Messages.SelectRulesWizard_rules_with_changes, standaloneConfig.getJavaProject()
					.getElementName(), refactoringPipeline.getRulesWithChangesAsString());
				logger.info(loggerInfo);

			} else {
				logger.info(Messages.Activator_standalone_noRulesSelected);
			}
		}
	}

	protected void commitChanges(RefactoringPipeline refactoringPipeline) throws StandaloneException {
		// Commit refactoring
		try {
			logger.info(Messages.Activator_debug_commitRefactoring);
			refactoringPipeline.commitRefactoring();
		} catch (RefactoringException | ReconcileException e) {
			logger.debug(e.getMessage(), e);
			logger.error(e.getMessage());
			throw new StandaloneException("Can not commit refactoring", e); //$NON-NLS-1$
		}
	}

	/**
	 * Reverts eclipse files for all projects if they were previously existing
	 * 
	 * @throws IOException
	 *             if reverting eclipse project files fails for some reason
	 * @throws CoreException
	 *             if closing {@link IProject} fails
	 */
	public void cleanUp() throws IOException, CoreException {

		for (StandaloneConfig standaloneConfig : standaloneConfigs) {
			standaloneConfig.revertEclipseProjectFiles();
		}
	}

	/**
	 * gets the configuration from the given path in context
	 * 
	 * @param context
	 * @return the read configuration
	 * @throws StandaloneException
	 * @throws YAMLConfigException
	 */
	private YAMLConfig getConfiguration(BundleContext context, String projectId) throws StandaloneException {

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
	 *            the bundle context configuration
	 * @throws StandaloneException
	 *             if an instance of the {@link StandaloneConfig} cannot be
	 *             created.
	 */
	protected void loadStandaloneConfig(BundleContext context) throws StandaloneException {

		Map<String, String> projectPaths = findAllProjectPaths(context);

		List<String> excludedModules = new ArrayList<>();
		String rootProjectConfig = context.getProperty(ROOT_CONFIG_PATH);
		if (!rootProjectConfig.isEmpty()) {
			String profile = context.getProperty(SELECTED_PROFILE);
			try {
				YAMLConfig rootYamlConfig = YAMLConfigUtil.readConfig(rootProjectConfig, profile);
				excludedModules = rootYamlConfig.getExcludes()
					.getExcludeModules();
			} catch (YAMLConfigException e) {
				throw new StandaloneException("Error occured while reading root yaml configuration file", e); //$NON-NLS-1$
			}
		}

		for (Map.Entry<String, String> entry : projectPaths.entrySet()) {
			String id = entry.getKey();
			String path = entry.getValue();
			String compilerCompliance = context.getProperty(PROJECT_JAVA_VERSION + DOT + id);
			String projectName = context.getProperty(PROJECT_NAME + DOT + id);
			if (excludedModules.contains(projectName)) {
				// skip adding StandaloneConfig for excluded module
				continue;
			}
			String sourceFolder = context.getProperty(SOURCE_FOLDER);
			String[] natureIds = findNatureIds(context, id);
			try {
				StandaloneConfig standaloneConfig = new StandaloneConfig(id, projectName, path, compilerCompliance,
						sourceFolder, natureIds);
				standaloneConfigs.add(standaloneConfig);
			} catch (CoreException | IOException e) {
				throw new StandaloneException(e.getMessage(), e);
			}
		}
	}

	protected String[] findNatureIds(BundleContext context, String id) {
		return context.getProperty(NATURE_IDS + DOT + id)
			.split(","); //$NON-NLS-1$
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
			throws StandaloneException {
		logger.debug(Messages.RefactoringInvoker_GetSelectedRules);
		try {
			return YAMLConfigUtil.getSelectedRulesFromConfig(config, projectRules);
		} catch (YAMLConfigException e) {
			throw new StandaloneException(e.getMessage(), e);
		}
	}

	protected YAMLConfig getYamlConfig(String configFilePath, String profile) throws StandaloneException {
		try {
			return YAMLConfigUtil.readConfig(configFilePath, profile);
		} catch (YAMLConfigException e) {
			throw new StandaloneException(e.getMessage(), e);
		}
	}
}
