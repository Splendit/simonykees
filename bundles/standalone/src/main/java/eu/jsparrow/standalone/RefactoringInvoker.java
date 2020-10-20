package eu.jsparrow.standalone;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.http.JsonUtil;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.StandaloneStatisticsData;
import eu.jsparrow.core.refactorer.StandaloneStatisticsMetadata;
import eu.jsparrow.core.statistic.entity.JsparrowData;
import eu.jsparrow.core.statistic.entity.JsparrowMetric;
import eu.jsparrow.core.statistic.entity.JsparrowRuleData;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.standalone.ConfigFinder.ConfigType;
import eu.jsparrow.standalone.exceptions.MavenImportException;
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
	 * must match with the ones in {@link eu.jsparrow.adapter.ConfigurationKeys}
	 */
	private static final String USER_DIR = "user.dir"; //$NON-NLS-1$
	private static final String JAVA_TMP = "java.io.tmpdir"; //$NON-NLS-1$
	private static final String JSPARROW_TEMP_FOLDER = "temp_jSparrow"; //$NON-NLS-1$
	private static final String SELECTED_PROFILE = "PROFILE.SELECTED"; //$NON-NLS-1$
	private static final String USE_DEFAULT_CONFIGURATION = "DEFAULT.CONFIG"; //$NON-NLS-1$
	private static final String ROOT_CONFIG_PATH = "ROOT.CONFIG.PATH"; //$NON-NLS-1$
	private static final String ROOT_PROJECT_BASE_PATH = "ROOT.PROJECT.BASE.PATH"; //$NON-NLS-1$
	private static final String CONFIG_FILE_OVERRIDE = "CONFIG.FILE.OVERRIDE"; //$NON-NLS-1$
	public static final String STATISTICS_START_TIME = "STATISTICS_START_TIME"; //$NON-NLS-1$
	public static final String STATISTICS_REPO_OWNER = "STATISTICS_REPO_OWNER"; //$NON-NLS-1$
	public static final String STATISTICS_REPO_NAME = "STATISTICS_REPO_NAME"; //$NON-NLS-1$
	public static final String STATISTICS_SEND = "STATISTICS_SEND"; //$NON-NLS-1$

	private boolean abort = false;
	private YAMLConfigurationWrapper yamlConfigurationWrapper = new YAMLConfigurationWrapper();
	private MavenProjectImporter importer = new MavenProjectImporter();

	protected List<StandaloneConfig> standaloneConfigs = new ArrayList<>();

	public RefactoringInvoker() {
		prepareWorkingDirectory();
	}

	/**
	 * Prepare and start the refactoring process
	 * 
	 * @param context
	 *            the bundle context configuration
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
	public void startRefactoring(BundleContext context) throws StandaloneException {
		List<IJavaProject> importedProjects = importAllProjects(context);
		loadStandaloneConfig(importedProjects, context);
		prepareRefactoring();
		computeRefactoring();
		commitRefactoring();
		collectAndSendStatisticData(context);
	}

	public void runInDemoMode(BundleContext context) throws StandaloneException {
		List<IJavaProject> importedProjects = importAllProjects(context);
		loadStandaloneConfig(importedProjects, context);
		prepareRefactoring();
		computeRefactoring();
		collectAndSendStatisticData(context);
	}

	/**
	 * Prepares the refactoring states for each {@link StandaloneConfig} on the
	 * {@link #standaloneConfigs}.
	 * 
	 * @throws StandaloneException
	 *             reasons include:
	 *             <ul>
	 *             <li>A {@link JavaModelException} is thrown while creating a
	 *             refactoring state</li>
	 *             <li>A user abort was detected</li>
	 *             <li>A {@link ConcurrentModificationException} was thrown
	 *             while canceling the execution</li>
	 *             </ul>
	 */
	private void prepareRefactoring() throws StandaloneException {
		for (StandaloneConfig standaloneConfig : standaloneConfigs) {
			String abortMessage = String.format("Abort detected while preparing refactoring on %s ", //$NON-NLS-1$
					standaloneConfig.getProjectName());
			verifyAbortFlag(abortMessage);
			try {
				standaloneConfig.createRefactoringStates();
			} catch (ConcurrentModificationException e) {
				String message = abort ? abortMessage : e.getMessage();
				throw new StandaloneException(message);
			}
		}
	}

	private void computeRefactoring() throws StandaloneException {

		for (StandaloneConfig standaloneConfig : standaloneConfigs) {
			String abortMessage = String.format("abort detected while computing refactoring on %s ", //$NON-NLS-1$
					standaloneConfig.getProjectName());
			verifyAbortFlag(abortMessage);
			try {
				standaloneConfig.computeRefactoring();
			} catch (ConcurrentModificationException e) {
				String message = abort ? abortMessage : e.getMessage();
				throw new StandaloneException(message);
			}

		}
	}

	private void commitRefactoring() throws StandaloneException {
		String loggInfo = "Abort detected before commiting refactoring "; //$NON-NLS-1$
		verifyAbortFlag(loggInfo);

		for (StandaloneConfig config : standaloneConfigs) {
			config.commitRefactoring();
		}
	}

	private void verifyAbortFlag(String logInfo) throws StandaloneException {
		if (abort) {
			logger.info(logInfo);
			throw new StandaloneException(logInfo);
		}
	}

	private void collectAndSendStatisticData(BundleContext context) {
		boolean sendStatistics = Boolean.parseBoolean(context.getProperty(STATISTICS_SEND));
		if (!sendStatistics) {
			return;
		}

		boolean computedStatistics = standaloneConfigs.stream()
			.map(StandaloneConfig::getStatisticsData)
			.filter(Objects::nonNull)
			.map(StandaloneStatisticsData::getMetricData)
			.anyMatch(Optional::isPresent);

		if (!computedStatistics) {
			return;
		}

		JsparrowMetric metricData = new JsparrowMetric();
		JsparrowData projectData = new JsparrowData();
		Map<String, JsparrowRuleData> rulesData = new HashMap<>();

		for (StandaloneConfig config : standaloneConfigs) {
			JsparrowMetric metrics = config.getStatisticsData()
				.getMetricData()
				.orElse(null);
			if (metrics == null) {
				continue;
			}
			metricData.setuuid(metrics.getuuid());
			metricData.setTimestamp(metrics.getTimestamp());
			metricData.setRepoName(metrics.getRepoName());
			metricData.setRepoOwner(metrics.getRepoOwner());

			JsparrowData currentProjectData = metrics.getData();
			projectData.setTimestampGitHubStart(currentProjectData.getTimestampGitHubStart());
			projectData.setTimestampJSparrowFinish(currentProjectData.getTimestampJSparrowFinish());
			projectData.setProjectName(metrics.getRepoName());

			projectData
				.setTotalFilesChanged(projectData.getTotalFilesChanged() + currentProjectData.getTotalFilesChanged());
			projectData.setTotalFilesCount(projectData.getTotalFilesCount() + currentProjectData.getTotalFilesCount());
			projectData
				.setTotalIssuesFixed(projectData.getTotalIssuesFixed() + currentProjectData.getTotalIssuesFixed());
			projectData.setTotalTimeSaved(projectData.getTotalTimeSaved() + currentProjectData.getTotalTimeSaved());

			String logInfo = String.format("Project  : %s, totalIssues: %s, totalTime: %s, totalFiles: %s ", //$NON-NLS-1$
					currentProjectData.getProjectName(), currentProjectData.getTotalIssuesFixed(),
					currentProjectData.getTotalTimeSaved(), currentProjectData.getTotalFilesChanged());
			logger.debug(logInfo);

			for (JsparrowRuleData ruleData : currentProjectData.getRules()) {
				if (rulesData.containsKey(ruleData.getRuleId())) {
					JsparrowRuleData currentRule = rulesData.get(ruleData.getRuleId());
					currentRule.setFilesChanged(currentRule.getFilesChanged() + ruleData.getFilesChanged());
					currentRule.setIssuesFixed(currentRule.getIssuesFixed() + ruleData.getIssuesFixed());
					rulesData.put(ruleData.getRuleId(), currentRule);
				} else {
					rulesData.put(ruleData.getRuleId(), ruleData);
				}
			}
		}
		projectData.setRules(new ArrayList<>(rulesData.values()));
		metricData.setData(projectData);

		String logInfo = String.format("FinalData: %s, totalIssues: %s, totalTime: %s, totalFiles: %s ", //$NON-NLS-1$
				projectData.getProjectName(), projectData.getTotalIssuesFixed(), projectData.getTotalTimeSaved(),
				projectData.getTotalFilesChanged());
		logger.debug(logInfo);

		String json = JsonUtil.generateJSON(metricData);
		JsonUtil.sendJsonToAwsStatisticsService(json);
	}

	/**
	 * Reverts eclipse files for all projects if they were previously existing
	 */
	public void cleanUp() {
		abort = true;
		for (StandaloneConfig standaloneConfig : standaloneConfigs) {
			standaloneConfig.setAbortFlag();
			try {
				standaloneConfig.clearPipeline();
			} catch (RuntimeException e) {
				/*
				 * Unpredicted runtime exceptions may be thrown while cleaning
				 * the pipeline. But the eclipse files must be reverted anyway.
				 * 
				 */
				logger.debug("Cannot clear refactoring states on {} ", standaloneConfig.getProjectName(), e); //$NON-NLS-1$
			}
		}

		importer.cleanUp();
	}

	/**
	 * Gets the configuration for the provided project.
	 * 
	 * @param context
	 *            the {@link BundleContext} within the equinox framework
	 * @param projectId
	 *            the project to find the configuration for
	 * @return the {@link YAMLConfig} corresponding to the project with the
	 *         given projectId or the default configuration if the yml file
	 *         cannot be found.
	 * @throws StandaloneException
	 *             if the yaml configuration is inconsistent
	 * 
	 * @see YAMLConfigurationWrapper#readConfiguration(String, String)
	 */
	private YAMLConfig getConfiguration(BundleContext context, File projectRootDir) throws StandaloneException {

		boolean useDefaultConfig = parseUseDefaultConfiguration(context);
		String profile = context.getProperty(SELECTED_PROFILE);
		String configFileOverride = context.getProperty(CONFIG_FILE_OVERRIDE);

		if (useDefaultConfig) {
			logger.debug(Messages.RefactoringInvoker_usingDefaultConfiguration);
			return yamlConfigurationWrapper.getDefaultYamlConfig();
		}

		if (configFileOverride != null && !configFileOverride.isEmpty()) {
			String logMsg = NLS.bind(Messages.RefactoringInvoker_usingOverriddenConfiguration, configFileOverride);
			logger.debug(logMsg);
			return yamlConfigurationWrapper.readConfiguration(configFileOverride, profile);
		}

		String configFilePath = findConfigFilePath(context, projectRootDir);
		String logMsg = NLS.bind(Messages.RefactoringInvoker_usingConfiguration, configFilePath);
		logger.debug(logMsg);

		return yamlConfigurationWrapper.readConfiguration(configFilePath, profile);
	}

	private String findConfigFilePath(BundleContext context, File projectRootDir) {
		ConfigFinder configFinder = new ConfigFinder();

		return configFinder.getYAMLFilePath(projectRootDir.toPath(), ConfigType.JSPARROW_FILE)
			.orElse(context.getProperty(ROOT_CONFIG_PATH));
	}

	private boolean parseUseDefaultConfiguration(BundleContext context) {
		String useDefaultConfigValue = context.getProperty(USE_DEFAULT_CONFIGURATION);
		return Boolean.parseBoolean(useDefaultConfigValue);
	}

	private void prepareWorkingDirectory() {
		String file = System.getProperty(JAVA_TMP);
		File directory = new File(file + File.separator + JSPARROW_TEMP_FOLDER).getAbsoluteFile();

		if (directory.exists() || directory.mkdirs()) {
			System.setProperty(USER_DIR, directory.getAbsolutePath());
		}
	}

	protected List<IJavaProject> importAllProjects(BundleContext context) throws StandaloneException {
		logger.info(Messages.RefactoringInvoker_importingMavenProjects);

		File workspaceRoot = ResourcesPlugin.getWorkspace()
			.getRoot()
			.getLocation()
			.toFile();
		String folder = context.getProperty(ROOT_PROJECT_BASE_PATH);

		List<IJavaProject> imported;

		try {
			imported = importer.importProjects(workspaceRoot, folder);
		} catch (MavenImportException e) {
			throw new StandaloneException(e.getMessage(), e);
		}

		logger.info(Messages.RefactoringInvoker_mavenProjectsImported);

		return imported;
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
	protected void loadStandaloneConfig(List<IJavaProject> importedProjects, BundleContext context)
			throws StandaloneException {

		logger.info(Messages.RefactoringInvoker_loadingConfiguration);

		String configFileOverride = context.getProperty(CONFIG_FILE_OVERRIDE);
		boolean useDefaultFallBack = configFileOverride == null || configFileOverride.isEmpty();
		String excludedModulesFilePath = useDefaultFallBack ? context.getProperty(ROOT_CONFIG_PATH)
				: configFileOverride;
		List<String> excludedModules = new ExcludedModules(parseUseDefaultConfiguration(context),
				excludedModulesFilePath).get();

		StandaloneStatisticsMetadata metadata = extractStatisticsMetadata(context);

		for (IJavaProject javaProject : importedProjects) {
			String abortMessage = "Abort detected while loading standalone configuration "; //$NON-NLS-1$
			verifyAbortFlag(abortMessage);

			String path = javaProject.getProject()
				.getLocation()
				.toFile()
				.getAbsolutePath();
			String projectName = javaProject.getProject()
				.getName();

			/*
			 * Since the aggregate projects do not contain java sources and we
			 * do not refactor them, given that the provided project has a
			 * parent is enough to derive that we re dealing with a multimodule
			 * project.
			 */
			if (excludedModules.contains(projectName)) {
				/*
				 * Skip adding StandaloneConfig for excluded module. Checks if
				 * name matches and excludes only that package, but not possible
				 * sub-packages / packages that start with the same string.
				 */
				String skippedLog = NLS.bind(Messages.RefactoringInvoker_projectExcludedFromRefactoring, projectName);
				logger.debug(skippedLog);
				continue;
			}

			String logMsg = NLS.bind(Messages.RefactoringInvoker_loadingConfigurationForProject, projectName);
			logger.debug(logMsg);
			try {
				YAMLConfig config = getConfiguration(context, javaProject.getProject()
					.getLocation()
					.toFile());
				StandaloneConfig standaloneConfig = new StandaloneConfig(javaProject, path, config, metadata);

				standaloneConfigs.add(standaloneConfig);

			} catch (CoreException | RuntimeException e) {
				String message = abort ? abortMessage : e.getMessage();
				throw new StandaloneException(message, e);
			}
		}

		if (standaloneConfigs.isEmpty()) {
			throw new StandaloneException(Messages.RefactoringInvoker_error_allModulesExcluded);
		}

		logger.info(Messages.RefactoringInvoker_configurationLoaded);
	}

	public MavenProjectImporter getImporter() {
		return importer;
	}

	private StandaloneStatisticsMetadata extractStatisticsMetadata(BundleContext context) {
		String repoOwner = context.getProperty(STATISTICS_REPO_OWNER);
		String repoName = context.getProperty(STATISTICS_REPO_NAME);

		String gitHubStartTimeString = context.getProperty(STATISTICS_START_TIME);

		long timestampGitHubStart = -1;
		if (gitHubStartTimeString != null && !gitHubStartTimeString.isEmpty()) {
			Instant gitHubStartTime = Instant.parse(context.getProperty(STATISTICS_START_TIME));
			timestampGitHubStart = gitHubStartTime.getEpochSecond();
		}

		return new StandaloneStatisticsMetadata(timestampGitHubStart, repoOwner, repoName);
	}

	public void setImporter(MavenProjectImporter importer) {
		this.importer = importer;
	}
}