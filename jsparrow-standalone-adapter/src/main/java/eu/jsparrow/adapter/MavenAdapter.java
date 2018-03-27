package eu.jsparrow.adapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Constants;

public class MavenAdapter {

	protected static final String OUTPUT_DIRECTORY_CONSTANT = "outputDirectory"; //$NON-NLS-1$
	private static final String MAVEN_COMPILER_PLUGIN_ARTIFACT_ID = "maven-compiler-plugin"; //$NON-NLS-1$
	private static final String MAVEN_COMPILER_PLUGIN_CONFIGURATIN_SOURCE_NAME = "source"; //$NON-NLS-1$
	private static final String MAVEN_COMPILER_PLUGIN_DEFAULT_JAVA_VERSION = "1.5"; //$NON-NLS-1$

	private static final String SELECTED_PROFILE = "PROFILE.SELECTED"; //$NON-NLS-1$
	private static final String USE_DEFAULT_CONFIGURATION = "DEFAULT.CONFIG"; //$NON-NLS-1$
	private static final String STANDALONE_MODE_KEY = "STANDALONE.MODE"; //$NON-NLS-1$
	private static final String PROJECT_JAVA_VERSION = "PROJECT.JAVA.VERSION"; //$NON-NLS-1$

	private static final String USER_DIR = "user.dir"; //$NON-NLS-1$
	private static final String DEPENDENCIES_FOLDER_CONSTANT = "deps"; //$NON-NLS-1$
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
	private static final String DOT = "."; //$NON-NLS-1$
	private static final String POM = "pom"; //$NON-NLS-1$
	private static final String CONFIG_FILE_PATH = "CONFIG.FILE.PATH"; //$NON-NLS-1$
	private static final String LOCK_FILE_NAME = "lock.txt"; //$NON-NLS-1$
	private static final String LICENSE_KEY = "LICENSE"; //$NON-NLS-1$

	private Log log;

	private Map<String, String> configuration = new HashMap<>();
	private MavenProject rootProject;
	private File directory;

	private Map<String, Boolean> sessionProjects = new HashMap<>();

	private boolean jsparrowAlreadyRunningError = false;
	private File defaultYamlFile;

	public MavenAdapter(MavenProject rootProject, Log log, File defaultYamlFile) {
		this.rootProject = rootProject;
		this.log = log;
		this.sessionProjects = new HashMap<>();
		this.defaultYamlFile = defaultYamlFile;
	}

	public void addProjectConfiguration(MavenProject project, File configFile, String mavenHome) {
		log.info(String.format("Adding configuration for project %s ...", project.getName())); //$NON-NLS-1$

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
		String yamlFilePath = findYamlFilePath(configFile);
		addConfigurationKeyValue(CONFIG_FILE_PATH + DOT + projectIdentifier, yamlFilePath);
		extractAndCopyDependencies(project, mavenHome);
		configuration.put(PROJECT_JAVA_VERSION + DOT + projectIdentifier, getCompilerCompliance(project));
	}

	private String findYamlFilePath(File yamlFile) {
		if (yamlFile.exists()) {
			return yamlFile.getAbsolutePath();
		}

		return defaultYamlFile.getAbsolutePath();
	}

	private boolean isAggregateProject(MavenProject project) {
		List<String> modules = project.getModules();
		String packaging = project.getPackaging();
		return POM.equalsIgnoreCase(packaging) || !modules.isEmpty();
	}

	private String joinWithComma(String left, String right) {
		if (left.isEmpty()) {
			return right;
		}
		return left + "," + right; //$NON-NLS-1$
	}

	private String getAllProjectIdentifiers() {
		return configuration.getOrDefault(ALL_PROJECT_IDENTIFIERS, ""); //$NON-NLS-1$
	}

	public void addInitialConfiguration(String mavenHome, String profile, String mode, boolean useDefaultConfig, String licenseKey) {
		configuration.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
		configuration.put(Constants.FRAMEWORK_STORAGE, FRAMEWORK_STORAGE_VALUE);
		configuration.put(INSTANCE_DATA_LOCATION_CONSTANT, System.getProperty(USER_DIR));
		configuration.put(MAVEN_HOME_KEY, mavenHome);
		configuration.put(DEBUG_ENABLED, Boolean.toString(log.isDebugEnabled()));
		
		
		configuration.put(STANDALONE_MODE_KEY, mode);
		configuration.put(SELECTED_PROFILE, (profile == null) ? "" : profile); //$NON-NLS-1$
		configuration.put(USE_DEFAULT_CONFIGURATION, Boolean.toString(useDefaultConfig));
		configuration.put(LICENSE_KEY, licenseKey);
	}

	private void addConfigurationKeyValue(String key, String value) {
		this.configuration.put(key, value);
	}

	private void markProjectConfigurationCompleted(MavenProject project) {
		String projectIdentifier = findProjectIdentifier(project);
		sessionProjects.put(projectIdentifier, true);
	}

	private String findProjectIdentifier(MavenProject mavenProject) {
		String groupId = mavenProject.getGroupId();
		String artifactId = mavenProject.getArtifactId();
		return groupId + DOT + artifactId;
	}

	/**
	 * Creates and prepares the temporary working directory and sets its path in
	 * system properties and equinox configuration
	 * 
	 * @param configuration
	 * @throws InterruptedException
	 */
	public void prepareWorkingDirectory(MavenProject mavenProject) throws InterruptedException {
		createWorkingDirectory(mavenProject);
		if (directory.exists() || directory.mkdirs()) {
			String directoryAbsolutePath = directory.getAbsolutePath();
			System.setProperty(USER_DIR, directoryAbsolutePath);
			addConfigurationKeyValue(OSGI_INSTANCE_AREA_CONSTANT, directoryAbsolutePath);

			String loggerInfo = NLS.bind("Set user.dir to {0}", directoryAbsolutePath);
			log.info(loggerInfo);
		} else {
			throw new InterruptedException("Could not create temp folder"); //$NON-NLS-1$
		}
	}

	/**
	 * Executes maven goal copy-dependencies on the project to copy all resolved
	 * needed dependencies to the temp folder for use from bundles.
	 */
	public void extractAndCopyDependencies(MavenProject project, String mavenHome) {
		log.debug("Extract and copy dependencies");

		final InvocationRequest request = new DefaultInvocationRequest();
		final Properties props = new Properties();
		prepareDefaultRequest(project, request, props);
		final Invoker invoker = new DefaultInvoker();
		invokeMaven(invoker, request, mavenHome);
	}

	protected void prepareDefaultRequest(MavenProject project, InvocationRequest request, Properties props) {
		File projectBaseDir = project.getBasedir();
		String projectPath = projectBaseDir.getAbsolutePath();
		request.setPomFile(new File(projectPath + File.separator + "pom.xml")); //$NON-NLS-1$
		request.setGoals(Collections.singletonList("dependency:copy-dependencies ")); //$NON-NLS-1$

		props.setProperty(OUTPUT_DIRECTORY_CONSTANT, System.getProperty(USER_DIR) + File.separator
				+ DEPENDENCIES_FOLDER_CONSTANT + DOT + findProjectIdentifier(project));
		request.setProperties(props);
	}

	protected void invokeMaven(Invoker invoker, InvocationRequest request, String preparedMavenHome) {
		invoker.setMavenHome(new File(preparedMavenHome));

		try {
			invoker.execute(request);
		} catch (final MavenInvocationException e) {
			log.debug(e.getMessage(), e);
			log.error(e.getMessage());
		}
	}

	protected File createWorkingDirectory(MavenProject mavenProject) {
		String projectIdentifier = findProjectIdentifier(mavenProject);
		File workingDirectory = new File(calculateJsparrowTempFolderPath() + DOT + projectIdentifier).getAbsoluteFile();
		setWorkingDirectory(workingDirectory);
		return workingDirectory;
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
		if (directory != null && directory.exists()) {
			try {
				deleteChildren(directory);
				Files.deleteIfExists(directory.toPath());
				clearLockFile();
			} catch (IOException e) {
				log.debug(e.getMessage(), e);
				log.error(e.getMessage());
			}
		}
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
			for (String file : Arrays.asList(children)) {
				File currentFile = new File(parentDirectory.getAbsolutePath(), file);
				if (currentFile.isDirectory()) {
					deleteChildren(currentFile);
				}

				try {
					Files.deleteIfExists(currentFile.toPath());
				} catch (IOException e) {
					log.debug(e.getMessage(), e);
					log.error(e.getMessage());
				}
			}
		}
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

	public synchronized void lockProjects() {
		Set<String> projectIds = sessionProjects.keySet();
		String lockFilePath = calculateJsparrowLockFilePath();
		Path path = Paths.get(lockFilePath);
		String conntent = projectIds.stream()
			.collect(Collectors.joining("\n")); //$NON-NLS-1$

		try {
			Files.write(path, conntent.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			log.warn("Cannot write to jsparrow lock file...", e);
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

	public boolean isJsparrowStarted(MavenProject mavenProject) {
		String projectId = findProjectIdentifier(mavenProject);
		try (Stream<String> linesStream = Files.lines(Paths.get(calculateJsparrowLockFilePath()))) {
			return linesStream.anyMatch(line -> line.equals(projectId));
		} catch (IOException e) {
			log.warn("Cannot read the jsparrow lock file...", e);
		}

		return false;
	}

	private void clearLockFile() {
		Path path = Paths.get(calculateJsparrowLockFilePath());
		try (Stream<String> linesStream = Files.lines(path)) {
			String newContent = linesStream.filter(line -> !sessionProjects.containsKey(line))
				.collect(Collectors.joining("\n"));
			Files.write(path, newContent.getBytes());
		} catch (IOException e) {
			log.warn("Cannot read the jsparrow lock file...", e);
		}
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
