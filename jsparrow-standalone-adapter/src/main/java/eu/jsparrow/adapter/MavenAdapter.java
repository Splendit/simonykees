package eu.jsparrow.adapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Constants;

import eu.jsparrow.adapter.i18n.Messages;

/**
 * Sets up the configuration used for starting the equinox framework.
 * Distinguishes the properties of different projects.
 * 
 * @author Andreja Sambolec, Matthias Webhofer, Ardit Ymeri
 * @since 2.5.0
 *
 */
public class MavenAdapter {

	public static final String USER_DIR = "user.dir"; //$NON-NLS-1$
	public static final String DOT = "."; //$NON-NLS-1$
	private static final String MAVEN_COMPILER_PLUGIN_ARTIFACT_ID = "maven-compiler-plugin"; //$NON-NLS-1$
	private static final String MAVEN_COMPILER_PLUGIN_CONFIGURATIN_SOURCE_NAME = "source"; //$NON-NLS-1$
	private static final String MAVEN_COMPILER_PLUGIN_DEFAULT_JAVA_VERSION = "1.5"; //$NON-NLS-1$

	private static final String SELECTED_PROFILE = "PROFILE.SELECTED"; //$NON-NLS-1$
	private static final String USE_DEFAULT_CONFIGURATION = "DEFAULT.CONFIG"; //$NON-NLS-1$
	private static final String STANDALONE_MODE_KEY = "STANDALONE.MODE"; //$NON-NLS-1$
	private static final String PROJECT_JAVA_VERSION = "PROJECT.JAVA.VERSION"; //$NON-NLS-1$

	private static final String JAVA_TMP = "java.io.tmpdir"; //$NON-NLS-1$
	private static final String INSTANCE_DATA_LOCATION_CONSTANT = "osgi.instance.area.default"; //$NON-NLS-1$
	private static final String FRAMEWORK_STORAGE_VALUE = "target/bundlecache"; //$NON-NLS-1$
	private static final String PROJECT_PATH_CONSTANT = "PROJECT.PATH"; //$NON-NLS-1$
	private static final String ALL_PROJECT_IDENTIFIERS = "ALL.PROJECT.IDENTIFIERS"; //$NON-NLS-1$
	private static final String PROJECT_NAME_CONSTANT = "PROJECT.NAME"; //$NON-NLS-1$
	private static final String JSPARROW_TEMP_FOLDER = "temp_jSparrow"; //$NON-NLS-1$
	private static final String OSGI_INSTANCE_AREA_CONSTANT = "osgi.instance.area"; //$NON-NLS-1$
	private static final String MAVEN_HOME_KEY = "MAVEN.HOME"; //$NON-NLS-1$
	private static final String DEBUG_ENABLED = "debug.enabled"; //$NON-NLS-1$
	private static final String CONFIG_FILE_PATH = "CONFIG.FILE.PATH"; //$NON-NLS-1$
	private static final String LIST_RULES_SELECTED_ID = "LIST.RULES.SELECTED.ID"; //$NON-NLS-1$
	private static final String LOCK_FILE_NAME = "lock"; //$NON-NLS-1$
	private static final String LICENSE_KEY = "LICENSE"; //$NON-NLS-1$

	private Log log;

	private Map<String, String> configuration = new HashMap<>();
	private MavenProject rootProject;
	private File directory;

	private Map<String, Boolean> sessionProjects = new HashMap<>();

	private boolean jsparrowAlreadyRunningError = false;
	private File defaultYamlFile;

	public MavenAdapter(MavenProject rootProject, Log log, File defaultYamlFile) {
		this(rootProject, log);
		this.defaultYamlFile = defaultYamlFile;
	}

	public MavenAdapter(MavenProject rootProject, Log log) {
		setRootProject(rootProject);
		this.log = log;
		this.sessionProjects = new HashMap<>();
	}

	/**
	 * Adds the following values to the configuration
	 * <ul>
	 * <li>project identifier computed by
	 * {@link #joinWithComma(String, String)}</li>
	 * <li>project path</li>
	 * <li>project name</li>
	 * <li>yml file path</li>
	 * <li>compiler compliance java version of the project</li>
	 * </ul>
	 * 
	 * Updates the {@link #sessionProjects} to indicate that the related
	 * configurations for the given project are stored.
	 * 
	 * <b>Note:</b> if the project represents and aggregate project, then no
	 * configuration is stored. Only the cofiguration of child projects need to
	 * be stored.
	 * 
	 * @param project
	 *            the maven project to store the configuration for
	 * @param configFile
	 *            the expected jsparrow.yml file
	 */
	public void addProjectConfiguration(MavenProject project, File configFile) {
		log.info(String.format(Messages.MavenAdapter_addingProjectConfiguration, project.getName()));

		markProjectConfigurationCompleted(project);

		if (isAggregateProject(project)) {
			return;
		}

		File baseDir = project.getBasedir();
		String projectPath = baseDir.getAbsolutePath();
		String projcetName = project.getName();
		String projectIdentifier = findProjectIdentifier(project);

		String allIdentifiers = getAllProjectIdentifiers();
		addConfigurationKeyValue(ALL_PROJECT_IDENTIFIERS, joinWithComma(allIdentifiers, projectIdentifier));
		addConfigurationKeyValue(PROJECT_PATH_CONSTANT + DOT + projectIdentifier, projectPath);
		addConfigurationKeyValue(PROJECT_NAME_CONSTANT + DOT + projectIdentifier, projcetName);
		String yamlFilePath = findYamlFilePath(project, configFile);
		log.info(Messages.MavenAdapter_jSparrowConfigurationFile + yamlFilePath);
		addConfigurationKeyValue(CONFIG_FILE_PATH + DOT + projectIdentifier, yamlFilePath);
		addConfigurationKeyValue(PROJECT_JAVA_VERSION + DOT + projectIdentifier, getCompilerCompliance(project));
	}

	/**
	 * Finds the path of the corresponding yaml configuration file of the
	 * project. If the provided yamlFile exists, its path is immediately
	 * returned. Otherwise, finds the yaml file in the closest ancestor until
	 * reaching the {@link rootProject}.
	 * 
	 * @param project
	 *            the project to find the configuration file for.
	 * @param yamlFile
	 *            expected yaml file
	 * @return the path of the corresponding yaml file
	 */
	protected String findYamlFilePath(MavenProject project, File yamlFile) {
		if (yamlFile.exists()) {
			return yamlFile.getAbsolutePath();
		}
		MavenProject parent = project;
		while ((parent = parent.getParent()) != null) {
			if (parent == getRootProject()) {
				break;
			}
			File parentBaseDir = parent.getBasedir();
			Path parentYamlPath = joinPaths(yamlFile, parentBaseDir);
			if (parentYamlPath.toFile()
				.exists()) {
				return parentYamlPath.toString();
			}
		}
		return getDefaultYamlFile().getAbsolutePath();
	}

	protected Path joinPaths(File yamlFile, File parentBaseDir) {
		return Paths.get(parentBaseDir.getAbsolutePath(), yamlFile.getPath());
	}

	/**
	 * Checks whether the given projects represents an aggregation of projects.
	 * 
	 * @param project
	 *            the maven project to be checked.
	 * @return if the packaging of the project is {@code pom} or the list of
	 *         modules is not empty
	 */
	protected boolean isAggregateProject(MavenProject project) {
		List<String> modules = project.getModules();
		String packaging = project.getPackaging();
		return "pom".equalsIgnoreCase(packaging) || !modules.isEmpty(); //$NON-NLS-1$
	}

	protected String joinWithComma(String left, String right) {
		if (left.isEmpty()) {
			return right;
		}
		return left + "," + right; //$NON-NLS-1$
	}

	private String getAllProjectIdentifiers() {
		return configuration.getOrDefault(ALL_PROJECT_IDENTIFIERS, ""); //$NON-NLS-1$
	}

	public void addInitialConfiguration(MavenParameters config, String mavenHome) {
		boolean useDefaultConfig = config.getUseDefaultConfig()
			.orElse(false);
		configuration.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
		configuration.put(Constants.FRAMEWORK_STORAGE, FRAMEWORK_STORAGE_VALUE);
		configuration.put(INSTANCE_DATA_LOCATION_CONSTANT, System.getProperty(USER_DIR));
		configuration.put(MAVEN_HOME_KEY, mavenHome);
		configuration.put(DEBUG_ENABLED, Boolean.toString(log.isDebugEnabled()));
		configuration.put(STANDALONE_MODE_KEY, config.getMode());
		configuration.put(SELECTED_PROFILE, config.getProfile()
			.orElse("")); //$NON-NLS-1$
		configuration.put(USE_DEFAULT_CONFIGURATION, Boolean.toString(useDefaultConfig));
		configuration.put(LICENSE_KEY, config.getLicense());
		config.getRuleId()
			.ifPresent(ruleId -> configuration.put(LIST_RULES_SELECTED_ID, ruleId));
	}

	private void addConfigurationKeyValue(String key, String value) {
		this.configuration.put(key, value);
	}

	private void markProjectConfigurationCompleted(MavenProject project) {
		String projectIdentifier = findProjectIdentifier(project);
		sessionProjects.put(projectIdentifier, true);
	}

	/**
	 * Concatenates the groupId and the artifactId of the project.
	 * 
	 * @param mavenProject
	 *            project to generate identifier for.
	 * @return the computed identifier.
	 */
	public String findProjectIdentifier(MavenProject mavenProject) {
		String groupId = mavenProject.getGroupId();
		String artifactId = mavenProject.getArtifactId();
		return groupId + DOT + artifactId;
	}

	/**
	 * Creates and prepares the temporary working directory and sets its path in
	 * system properties and equinox configuration
	 * 
	 * @param configuration
	 * 
	 * @throws InterruptedException
	 */
	public void prepareWorkingDirectory() throws InterruptedException {

		File workingDirectory = createWorkingDirectory();
		setWorkingDirectory(workingDirectory);
		if (workingDirectory.exists() || workingDirectory.mkdirs()) {
			String directoryAbsolutePath = workingDirectory.getAbsolutePath();
			setSystemProperty(USER_DIR, directoryAbsolutePath);
			addConfigurationKeyValue(OSGI_INSTANCE_AREA_CONSTANT, directoryAbsolutePath);

			String loggerInfo = NLS.bind(Messages.MavenAdapter_setUserDir, directoryAbsolutePath);
			log.info(loggerInfo);
		} else {
			throw new InterruptedException(Messages.MavenAdapter_couldnotCreateTempFolder);
		}
	}

	protected void setSystemProperty(String key, String directoryAbsolutePath) {
		System.setProperty(key, directoryAbsolutePath);
	}

	protected File createWorkingDirectory() {
		return new File(calculateJsparrowTempFolderPath()).getAbsoluteFile();
	}

	private void setWorkingDirectory(File directory2) {
		this.directory = directory2;
	}

	/**
	 * Cleans classpath and temp directory
	 * 
	 * @throws IOException
	 */
	public void cleanUp() {

		// CLEAN
		if (directory == null || !directory.exists()) {
			return;
		}

		try {
			deleteSessionRelatedFiles(directory);
			boolean emptyLockFile = cleanLockFile();
			if (emptyLockFile) {
				deleteChildren(directory);
				Files.deleteIfExists(directory.toPath());
			}
		} catch (IOException e) {
			log.debug(e.getMessage(), e);
			log.error(e.getMessage());
		}
	}

	/**
	 * Deletes the children files related to the projects on the current
	 * session.
	 * 
	 * @param parentDirectory
	 *            the file representing the parent directory containing session
	 *            related files.
	 */
	private void deleteSessionRelatedFiles(File parentDirectory) {
		String[] children = parentDirectory.list();
		if (children == null) {
			return;
		}

		for (String file : children) {
			if (isSessionRelated(file)) {
				File currentFile = new File(parentDirectory.getAbsolutePath(), file);
				deleteChildren(currentFile);
				deleteIfExists(currentFile);
			}
		}
	}

	protected void deleteIfExists(File currentFile) {
		try {
			Files.deleteIfExists(currentFile.toPath());
		} catch (IOException e) {
			log.debug(e.getMessage(), e);
			log.error(e.getMessage());
		}
	}

	/**
	 * Removes the lines in the lock file that are related to the projects of
	 * the given session.
	 * 
	 * @return if the resulting content of the lock file is empty.
	 */
	private boolean cleanLockFile() {
		Set<String> sessionIds = sessionProjects.keySet();
		Path path = Paths.get(calculateJsparrowLockFilePath());

		if (!path.toFile()
			.exists()) {
			return true;
		}

		String remainingContent = ""; //$NON-NLS-1$
		try (Stream<String> linesStream = Files.lines(path)) {
			remainingContent = linesStream.filter(id -> !sessionIds.contains(id))
				.collect(Collectors.joining("\n")) //$NON-NLS-1$
				.trim();

		} catch (IOException e) {
			log.warn(Messages.MavenAdapter_cannotReadJSparrowLockFile, e);
		}

		try {
			Files.write(path, remainingContent.getBytes());
			return remainingContent.isEmpty();
		} catch (IOException e) {
			log.warn(Messages.MavenAdapter_cannotWriteToJSparrowLockFile, e);
		}

		return false;
	}

	/**
	 * Recursively deletes all sub-folders from received folder.
	 * 
	 * @param parentDirectory
	 *            directory which content is to be deleted
	 * @throws IOException
	 */
	private void deleteChildren(File parentDirectory) {
		String[] children = parentDirectory.list();
		if (children != null) {
			for (String file : children) {
				File currentFile = new File(parentDirectory.getAbsolutePath(), file);
				if (currentFile.isDirectory()) {
					deleteChildren(currentFile);
				}

				deleteIfExists(currentFile);
			}
		}
	}

	private boolean isSessionRelated(String file) {
		return sessionProjects.keySet()
			.stream()
			.anyMatch(file::contains);
	}

	public boolean allProjectConfigurationLoaded() {
		return !this.sessionProjects.containsValue(Boolean.FALSE);
	}

	public void storeProjects(MavenSession mavenSession2) {
		List<MavenProject> allProjects = mavenSession2.getAllProjects();
		this.sessionProjects = allProjects.stream()
			.map(this::findProjectIdentifier)
			.collect(Collectors.toMap(Function.identity(), id -> false));
	}

	/**
	 * Appends the project id-s of the current session in the lock file. Creates
	 * the lock file it does not exist. Uses
	 * {@link #calculateJsparrowLockFilePath()} for computing the path of the
	 * lock file.
	 */
	public synchronized void lockProjects() {
		Set<String> projectIds = sessionProjects.keySet();
		String lockFilePath = calculateJsparrowLockFilePath();
		Path path = Paths.get(lockFilePath);
		String conntent = projectIds.stream()
			.collect(Collectors.joining("\n", "\n", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		try {
			Files.write(path, conntent.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			log.warn(Messages.MavenAdapter_cannotWriteToJSparrowLockFile, e);
		}
	}

	protected String calculateJsparrowLockFilePath() {
		return calculateJsparrowTempFolderPath() + File.separator + LOCK_FILE_NAME;
	}

	public boolean isJsparrowRunningFlag() {
		return jsparrowAlreadyRunningError;
	}

	public void setJsparrowRunningFlag() {
		this.jsparrowAlreadyRunningError = true;
	}

	public Map<String, String> getConfiguration() {
		return configuration;
	}

	public static String calculateJsparrowTempFolderPath() {
		String file = System.getProperty(JAVA_TMP);
		return file + File.separator + JSPARROW_TEMP_FOLDER;
	}

	protected MavenProject getRootProject() {
		return this.rootProject;
	}

	protected void setRootProject(MavenProject project) {
		this.rootProject = project;
	}

	protected File getDefaultYamlFile() {
		return this.defaultYamlFile;
	}

	protected void setDefaultYamlFile(File file) {
		this.defaultYamlFile = file;
	}

	/**
	 * Checks whether the lock file contains the id of the given project.
	 * 
	 * @param mavenProject
	 *            a maven project to be checked.
	 * @return {@code true}if the lock file contains the project id, or
	 *         {@code false} otherwise.
	 */
	public boolean isJsparrowStarted(MavenProject mavenProject) {
		String projectId = findProjectIdentifier(mavenProject);
		Path path = Paths.get(calculateJsparrowLockFilePath());

		if (!path.toFile()
			.exists()) {
			return false;
		}
		try (Stream<String> linesStream = Files.lines(path)) {
			return linesStream.anyMatch(projectId::equals);
		} catch (IOException e) {
			log.warn(Messages.MavenAdapter_cannotReadJSparrowLockFile, e);
		}

		return false;
	}

	/**
	 * Reads the current java source version from the maven-compiler-plugin
	 * configuration in the pom.xml. If no configuration is found, the java
	 * version is 1.5 by default (as stated in the documentation of
	 * maven-compiler-plugin:
	 * https://maven.apache.org/plugins/maven-compiler-plugin/).
	 * 
	 * @return the project's java version
	 */
	private String getCompilerCompliance(MavenProject project) {
		List<Plugin> buildPlugins = project.getBuildPlugins();

		for (Plugin plugin : buildPlugins) {
			if (MAVEN_COMPILER_PLUGIN_ARTIFACT_ID.equals(plugin.getArtifactId())) {
				Xpp3Dom pluginConfig = (Xpp3Dom) plugin.getConfiguration();
				if (pluginConfig != null) {
					for (Xpp3Dom child : pluginConfig.getChildren()) {
						if (MAVEN_COMPILER_PLUGIN_CONFIGURATIN_SOURCE_NAME.equals(child.getName())) {
							return child.getValue();
						}
					}
				}
				break;
			}
		}

		return MAVEN_COMPILER_PLUGIN_DEFAULT_JAVA_VERSION;
	}
}
