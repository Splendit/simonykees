package eu.jsparrow.adapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Constants;

import eu.jsparrow.adapter.i18n.Messages;

public class MavenAdapter {

	protected static final String OUTPUT_DIRECTORY_CONSTANT = "outputDirectory"; //$NON-NLS-1$

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

	private Log log;

	private Map<String, String> configuration = new HashMap<>();
	private MavenProject rootProject;
	private File directory;

	private Map<String, Boolean> sessionProjects = new HashMap<>();

	private boolean jsparrowAlreadyRunningError = false;

	public MavenAdapter(MavenProject rootProject, Log log) {
		this.rootProject = rootProject;
		this.log = log;
		this.sessionProjects = new HashMap<>();
	}

	public void addProjectConfiguration(MavenProject project, Map<String, String> config) {
		log.info(String.format("Adding configuration for project %s ...", project.getName())); //$NON-NLS-1$

		File baseDir = project.getBasedir();
		String projectPath = baseDir.getAbsolutePath();
		String projcetName = project.getName();
		String projectIdentifier = findProjectIdentifier(project);
		
		log.info("Project path is " + projectPath + ".");
		
		String allIdentifiers = getAllProjectIdentifiers();
		addConfigurationKeyValue(ALL_PROJECT_IDENTIFIERS, allIdentifiers + "," + projectIdentifier);
		log.info(String.format("Adding project identifier %s ...", allIdentifiers + "," + projectIdentifier)); //$NON-NLS-1$
		
		addConfigurationKeyValue(PROJECT_PATH_CONSTANT + DOT + projectIdentifier, projectPath);
		log.info(String.format("Adding project path: %s  -> %s ", PROJECT_PATH_CONSTANT + DOT + projectIdentifier , projectPath)); //$NON-NLS-1$
		addConfigurationKeyValue(PROJECT_NAME_CONSTANT + DOT + projectIdentifier, projcetName);

		markProjectConfigurationCompleted(project);
		addConfig(config);
	}

	private void addConfig(Map<String, String> config) {
		this.configuration.putAll(config);
		
	}

	private String getAllProjectIdentifiers() {
		return configuration.getOrDefault(ALL_PROJECT_IDENTIFIERS, "");
	}

	public void addInitialConfiguration(String mavenHome) {
		configuration.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
		configuration.put(Constants.FRAMEWORK_STORAGE, FRAMEWORK_STORAGE_VALUE);
		configuration.put(INSTANCE_DATA_LOCATION_CONSTANT, System.getProperty(USER_DIR));
		configuration.put(MAVEN_HOME_KEY, mavenHome);
		configuration.put(DEBUG_ENABLED, Boolean.toString(log.isDebugEnabled()));
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
	 * creates and prepares the temporary working directory and sets its path in
	 * system properties and equinox configuration
	 * 
	 * @param configuration
	 * @throws InterruptedException
	 */
	public void prepareWorkingDirectory(MavenProject mavenProject) throws InterruptedException {
		createWorkingDirectory(mavenProject);

		if (directory.exists()) {
			if (Arrays.asList(directory.list())
				.size() <= 1) {
				System.setProperty(USER_DIR, directory.getAbsolutePath());
				configuration.put(OSGI_INSTANCE_AREA_CONSTANT, directory.getAbsolutePath());

				String loggerInfo = NLS.bind(Messages.Adapter_setUserDirTo, directory.getAbsolutePath());
				log.info(loggerInfo);
			} else {
				log.error("Existing files in " + directory.getAbsolutePath() + ": \n " + convertToConcatAbsolutPaths(directory.listFiles()));
				jsparrowAlreadyRunningError = true;
				throw new InterruptedException("jSparrow is already running...");
			}
		} else if (directory.mkdirs()) {
			System.setProperty(USER_DIR, directory.getAbsolutePath());
			configuration.put(OSGI_INSTANCE_AREA_CONSTANT, directory.getAbsolutePath());

			String loggerInfo = NLS.bind(Messages.Adapter_setUserDirTo, directory.getAbsolutePath());
			log.info(loggerInfo);
		} else {
			throw new InterruptedException("Could not create temp folder"); //$NON-NLS-1$
		}
	}

	private String convertToConcatAbsolutPaths(File [] files) {
		StringBuilder sb = new StringBuilder();
		for(File file : files) {
			sb.append(file.getAbsolutePath());
			sb.append(",");
		}
		return sb.toString();
	}

	/**
	 * Executes maven goal copy-dependencies on the project to copy all resolved
	 * needed dependencies to the temp folder for use from bundles.
	 */
	public void extractAndCopyDependencies(String preparedMavenHome) {
		log.debug(Messages.Adapter_extractAndCopyDependencies);

		final InvocationRequest request = new DefaultInvocationRequest();
		final Properties props = new Properties();

		prepareDefaultRequest(request, props);

		final Invoker invoker = new DefaultInvoker();

		invokeMaven(invoker, request, preparedMavenHome);
	}

	protected void prepareDefaultRequest(InvocationRequest request, Properties props) {
		String projectPath = this.rootProject.getBasedir()
			.getAbsolutePath();
		request.setPomFile(new File(projectPath + File.separator + "pom.xml")); //$NON-NLS-1$
		request.setGoals(Collections.singletonList("dependency:copy-dependencies ")); //$NON-NLS-1$

		props.setProperty(OUTPUT_DIRECTORY_CONSTANT,
				System.getProperty(USER_DIR) + File.separator + DEPENDENCIES_FOLDER_CONSTANT);
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
		File workingDirectory = new File(calculateJsparrowTempFolderPath() 
//				+ DOT + projectIdentifier
				).getAbsoluteFile();
		setWorkingDirectory(workingDirectory);
		return workingDirectory;
	}

	private void setWorkingDirectory(File directory2) {
		this.directory = directory2;
	}

	/**
	 * cleans classpath and temp directory
	 * 
	 * @throws IOException
	 */
	public void cleanUp() {

		// CLEAN
		if (directory != null && directory.exists()) {
			try {
				deleteChildren(directory);
				Files.deleteIfExists(directory.toPath());
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
			.collect(Collectors.toMap(id -> id, id -> false));
	}

	public boolean isJsparrowRunningFlag() {
		return jsparrowAlreadyRunningError;
	}

	public Map<String, String> getConfiguration() {
		return configuration;
	}
	
	public static String calculateJsparrowTempFolderPath() {
		String file = System.getProperty(JAVA_TMP);
		return file + File.separator + JSPARROW_TEMP_FOLDER;
	}
}
