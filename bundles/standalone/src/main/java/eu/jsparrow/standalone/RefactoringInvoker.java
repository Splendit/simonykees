package eu.jsparrow.standalone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.config.YAMLConfigException;
import eu.jsparrow.core.config.YAMLConfigUtil;
import eu.jsparrow.core.config.YAMLExcludes;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.i18n.Messages;
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
	 * must match with the ones in {@link eu.jsparrow.adapter.ConfigurationKeys}
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
	private boolean aboard = false;

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
	public void startRefactoring(BundleContext context) throws StandaloneException {
		loadStandaloneConfig(context);
		prepareRefactoring();
		computeRefactoring();
		commitRefactoring();
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
	 *             <li>A user aboard was detected</li>
	 *             <li>A {@link ConcurrentModificationException} was thrown
	 *             while canceling the execution</li>
	 *             </ul>
	 */
	private void prepareRefactoring() throws StandaloneException {
		for (StandaloneConfig standaloneConfig : standaloneConfigs) {
			String aboardMessage = String.format("Aboard detected while preparing refactoring on %s ", //$NON-NLS-1$
					standaloneConfig.getProjectName());
			verifyAboardFlag(aboardMessage);
			try {
				standaloneConfig.createRefactoringStates();
			} catch (ConcurrentModificationException e) {
				String message = aboard ? aboardMessage : e.getMessage();
				throw new StandaloneException(message);
			}
		}
	}

	private void computeRefactoring() throws StandaloneException {

		for (StandaloneConfig standaloneConfig : standaloneConfigs) {
			String aboardMessage = String.format("Aboard detected while computing refactoring on %s ", //$NON-NLS-1$
					standaloneConfig.getProjectName());
			verifyAboardFlag(aboardMessage);
			try {
				standaloneConfig.computeRefactoring();
			} catch (ConcurrentModificationException e) {
				String message = aboard ? aboardMessage : e.getMessage();
				throw new StandaloneException(message);
			}

		}
	}

	private void commitRefactoring() throws StandaloneException {
		String loggInfo = "Aboard detected before commiting refactoring "; //$NON-NLS-1$
		verifyAboardFlag(loggInfo);

		for (StandaloneConfig config : standaloneConfigs) {
			config.commitRefactoring();
		}
	}

	private void verifyAboardFlag(String logInfo) throws StandaloneException {
		if (aboard) {
			logger.info(logInfo);
			throw new StandaloneException(logInfo);
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
		aboard = true;
		for (StandaloneConfig standaloneConfig : standaloneConfigs) {
			standaloneConfig.setAboardFlag();
			try {
				standaloneConfig.clearPipeline();
			} catch (RuntimeException e) {
				/*
				 * Unpredicted runtime exceptions may be thrown while cleaning
				 * the pipeline. But the eclipse files must be reverted anyway.
				 * 
				 */
				logger.debug("Cannot clear refactoring states on {} ", standaloneConfig.getProjectName(), e); //$NON-NLS-1$
				throw e;
			} finally {
				standaloneConfig.revertEclipseProjectFiles();
			}
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

		boolean useDefaultConfig = parseUseDefaultConfiguration(context);

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
		List<String> excludedModules = findExcluededModules(context);

		for (Map.Entry<String, String> entry : projectPaths.entrySet()) {
			String aboardMessage = "Aboard detected while loading standalone configuration "; //$NON-NLS-1$
			verifyAboardFlag(aboardMessage);
			String id = entry.getKey();
			String path = entry.getValue();
			String compilerCompliance = context.getProperty(PROJECT_JAVA_VERSION + DOT + id);
			String projectName = context.getProperty(PROJECT_NAME + DOT + id);
			if (excludedModules.contains(projectName)) {
				/*
				 * Skip adding StandaloneConfig for excluded module. Checks if
				 * name matches and excludes only that package, but not possible
				 * sub-packages / packages that start with the same string.
				 */
				continue;
			}
			String sourceFolder = context.getProperty(SOURCE_FOLDER);
			String[] natureIds = findNatureIds(context, id);
			try {
				YAMLConfig config = getConfiguration(context, id);
				StandaloneConfig standaloneConfig = new StandaloneConfig(projectName, path, compilerCompliance,
						sourceFolder, natureIds, config);
				standaloneConfigs.add(standaloneConfig);

			} catch (CoreException | RuntimeException e) {
				String message = aboard ? aboardMessage : e.getMessage();
				throw new StandaloneException(message, e);
			}
		}

		if (standaloneConfigs.isEmpty()) {
			throw new StandaloneException(Messages.RefactoringInvoker_error_allModulesExcluded);
		}
	}

	private List<String> findExcluededModules(BundleContext context) throws StandaloneException {
		boolean useDefaultConfig = parseUseDefaultConfiguration(context);
		String logInfo;
		if (useDefaultConfig) {
			/*
			 * No modules are excluded with the default configuration
			 */
			logInfo = "No excluded modules. Using the default configuration."; //$NON-NLS-1$
			logger.debug(logInfo);
			return Collections.emptyList();
		}

		String rootProjectConfig = context.getProperty(ROOT_CONFIG_PATH);
		if (rootProjectConfig.isEmpty()) {
			logInfo = "Cannot find excluded modules. The root yml file path is not provided"; //$NON-NLS-1$
			logger.debug(logInfo);
			return Collections.emptyList();
		}
		String profile = context.getProperty(SELECTED_PROFILE);
		try {
			YAMLConfig rootYamlConfig = YAMLConfigUtil.readConfig(rootProjectConfig, profile);
			YAMLExcludes excludes = rootYamlConfig.getExcludes();
			List<String> excludedModules = excludes.getExcludeModules();
			if (!excludedModules.isEmpty()) {
				logInfo = excludedModules.stream()
					.collect(Collectors.joining("\n", ",\n", ".")); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			} else {
				logInfo = "No excluded modules were found."; //$NON-NLS-1$
			}
			logger.debug(logInfo);
			return excludedModules;
		} catch (YAMLConfigException e) {
			throw new StandaloneException("Error occured while reading the root yaml configuration file", e); //$NON-NLS-1$
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

	protected YAMLConfig getYamlConfig(String configFilePath, String profile) throws StandaloneException {
		try {
			return YAMLConfigUtil.readConfig(configFilePath, profile);
		} catch (YAMLConfigException e) {
			throw new StandaloneException(e.getMessage(), e);
		}
	}
}
