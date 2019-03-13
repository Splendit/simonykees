package eu.jsparrow.standalone;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.exception.RefactoringException;
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
		List<IJavaProject> importedProjects = importAllProjects(context);
		loadStandaloneConfig(importedProjects, context);
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

	/**
	 * Reverts eclipse files for all projects if they were previously existing
	 * 
	 * @throws IOException
	 *             if reverting eclipse project files fails for some reason
	 * @throws CoreException
	 *             if closing {@link IProject} fails
	 */
	public void cleanUp() throws IOException, CoreException {
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
				throw e;
			} finally {
				importer.cleanUp();
			}
		}
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

		if (useDefaultConfig) {
			return yamlConfigurationWrapper.getDefaultYamlConfig();
		}

		Optional<String> configFilePath = findConfigFilePath(projectRootDir);
		String profile = context.getProperty(SELECTED_PROFILE);
		String rootConfigFilePath = context.getProperty(ROOT_CONFIG_PATH);

		return yamlConfigurationWrapper.readConfiguration(configFilePath.orElse(rootConfigFilePath), profile);
	}

	private Optional<String> findConfigFilePath(File projectRootDir) {
		Path configFilePath = Paths.get(projectRootDir.getAbsolutePath(), "jsparrow.yml"); //$NON-NLS-1$

		if (Files.exists(configFilePath)) {
			return Optional.ofNullable(configFilePath.toString());
		}

		return Optional.empty();
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

		List<String> excludedModules = new ExcludedModules(parseUseDefaultConfiguration(context), context.getProperty(ROOT_CONFIG_PATH)).get();

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
				StandaloneConfig standaloneConfig = new StandaloneConfig(javaProject, projectName, path, config);

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

	public void setImporter(MavenProjectImporter importer) {
		this.importer = importer;
	}
}