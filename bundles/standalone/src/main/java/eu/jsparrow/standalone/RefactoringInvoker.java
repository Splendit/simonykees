package eu.jsparrow.standalone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
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
	private static final String HAS_PARENT = "HAS.PARENT"; //$NON-NLS-1$
	private static final String PARENT_PROJECT_PATH = "PARENT.PROJECT.PATH"; //$NON-NLS-1$
	private static final String ROOT_PROJECT_BASE_PATH = "ROOT.PROJECT.BASE.PATH"; //$NON-NLS-1$
	private static final String DEFAULT_GROUP_ID = "DEFAULT.GROUP.ID"; //$NON-NLS-1$

	private boolean abort = false;
	private YAMLConfigurationWrapper yamlConfigurationWrapper = new YAMLConfigurationWrapper();
	private EclipseProjectFileManager eclipseProjectFileManager = new EclipseProjectFileManager();

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
		Map<String, IJavaProject> importedProjects = importAllProjects(context);
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
				eclipseProjectFileManager.revertEclipseProjectFiles();
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
	private YAMLConfig getConfiguration(BundleContext context, String projectId) throws StandaloneException {

		boolean useDefaultConfig = parseUseDefaultConfiguration(context);

		if (useDefaultConfig) {
			return yamlConfigurationWrapper.getDefaultYamlConfig();
		}

		String configFilePath = context.getProperty(CONFIG_FILE_PATH + DOT + projectId);
		String profile = context.getProperty(SELECTED_PROFILE);

		if(configFilePath == null || configFilePath.isEmpty()) {
			configFilePath = context.getProperty(ROOT_CONFIG_PATH);
		}
		
		return yamlConfigurationWrapper.readConfiguration(configFilePath, profile);
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

	protected Map<String, IJavaProject> importAllProjects(BundleContext context) throws StandaloneException {
		MavenProjectImporter importer = new MavenProjectImporter();

		Map<String, String> projectPaths = findAllProjectPaths(context);
		List<String> folders = new ArrayList<>(projectPaths.values());
		
		eclipseProjectFileManager.backupExistingEclipseFiles();
		
		File workspaceRoot = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
		String folder = context.getProperty(ROOT_PROJECT_BASE_PATH);
		String defaultGroupId = context.getProperty(DEFAULT_GROUP_ID);
		
		Map<String, IJavaProject> imported;
		
		try {
			imported = importer.importProjectsUsingLocalProjectScanner(workspaceRoot, folder, defaultGroupId);
		} catch (MavenImportException e) {
			throw new StandaloneException(e.getMessage(), e);
		}
		
		return imported;
//		for (Map.Entry<String, String> entry : projectPaths.entrySet()) {
//			String id = entry.getKey();
//			String path = entry.getValue();
//			String compilerCompliance = context.getProperty(PROJECT_JAVA_VERSION + DOT + id);
//			String projectName = context.getProperty(PROJECT_NAME + DOT + id);
//			String parentPath = context.getProperty(PARENT_PROJECT_PATH + DOT + id);
//
//			try {
//				importer.addProjectInfo(path, projectName, compilerCompliance, parentPath);
//				eclipseProjectFileManager.addProject(path);
//			} catch (CoreException e) {
//				throw new StandaloneException(e.getMessage(), e);
//			}
//		}
//
//		try {
//			eclipseProjectFileManager.backupExistingEclipseFiles();
//			return importer.importMavenProjects();
//		} catch (MavenImportException e) {
//			throw new StandaloneException(e.getMessage(), e);
//		}
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
	protected void loadStandaloneConfig(Map<String, IJavaProject> importedProjects, BundleContext context)
			throws StandaloneException {

		Map<String, String> projectPaths = findAllProjectPaths(context);

		List<String> excludedModules = new ExcludedModules(parseUseDefaultConfiguration(context),
				context.getProperty(ROOT_CONFIG_PATH)).get();

		for (Map.Entry<String, IJavaProject> entry : importedProjects.entrySet()) {
			String abortMessage = "Abort detected while loading standalone configuration "; //$NON-NLS-1$
			verifyAbortFlag(abortMessage);
			
			String id = entry.getKey();
			IJavaProject javaProject = entry.getValue();
			
			String path = javaProject.getProject().getLocation().toFile().getAbsolutePath();
			String projectName = context.getProperty(PROJECT_NAME + DOT + id);
			
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
				continue;
			}
			try {
				YAMLConfig config = getConfiguration(context, id);
				StandaloneConfig standaloneConfig = new StandaloneConfig(javaProject, projectName, path, config);

				standaloneConfigs.add(standaloneConfig);

			} catch (CoreException | RuntimeException e) {
				String message = abort ? abortMessage : e.getMessage();
				throw new StandaloneException(message, e);
			}
		}
		
//		for (Map.Entry<String, String> entry : projectPaths.entrySet()) {
//			String abortMessage = "Abort detected while loading standalone configuration "; //$NON-NLS-1$
//			verifyAbortFlag(abortMessage);
//			String id = entry.getKey();
//			String path = entry.getValue();
//			String projectName = context.getProperty(PROJECT_NAME + DOT + id);
//			/*
//			 * Since the aggregate projects do not contain java sources and we
//			 * do not refactor them, given that the provided project has a
//			 * parent is enough to derive that we re dealing with a multimodule
//			 * project.
//			 */
//			if (excludedModules.contains(projectName)) {
//				/*
//				 * Skip adding StandaloneConfig for excluded module. Checks if
//				 * name matches and excludes only that package, but not possible
//				 * sub-packages / packages that start with the same string.
//				 */
//				continue;
//			}
//			try {
//				YAMLConfig config = getConfiguration(context, id);
//				Optional<IJavaProject> javaProject = importedProjects.stream()
//					.filter(p -> p.getProject()
//						.getName()
//						.equalsIgnoreCase(projectName))
//					.findFirst();
//				StandaloneConfig standaloneConfig = new StandaloneConfig(javaProject.get(), projectName, path, config);
//
//				standaloneConfigs.add(standaloneConfig);
//
//			} catch (CoreException | RuntimeException e) {
//				String message = abort ? abortMessage : e.getMessage();
//				throw new StandaloneException(message, e);
//			}
//		}

		if (standaloneConfigs.isEmpty()) {
			throw new StandaloneException(Messages.RefactoringInvoker_error_allModulesExcluded);
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
}