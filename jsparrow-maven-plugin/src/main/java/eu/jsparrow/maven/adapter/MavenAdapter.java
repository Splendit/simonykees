package eu.jsparrow.maven.adapter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Constants;

import eu.jsparrow.maven.i18n.Messages;

/**
 * Sets up the configuration used for starting the equinox framework.
 * Distinguishes the properties of different projects.
 * 
 * @author Andreja Sambolec, Matthias Webhofer, Ardit Ymeri
 * @since 2.5.0
 *
 */
public class MavenAdapter {

	/**
	 * The following constants represent some keys in the BundleContext. Any
	 * change here must be reflected also in
	 * {@link eu.jsparrow.standalone.RefactoringInvoker}.
	 */
	public static final String USER_DIR = "user.dir"; //$NON-NLS-1$
	public static final String DOT = "."; //$NON-NLS-1$
	private static final String MAVEN_COMPILER_PLUGIN_ARTIFACT_ID = "maven-compiler-plugin"; //$NON-NLS-1$
	private static final String MAVEN_COMPILER_PLUGIN_CONFIGURATIN_SOURCE_NAME = "source"; //$NON-NLS-1$
	private static final String MAVEN_COMPILER_PLUGIN_PROPERTY_SOURCE_NAME = "maven.compiler.source"; //$NON-NLS-1$
	private static final String MAVEN_COMPILER_PLUGIN_DEFAULT_JAVA_VERSION = "1.5"; //$NON-NLS-1$
	private static final String SELECTED_PROFILE = "PROFILE.SELECTED"; //$NON-NLS-1$
	private static final String USE_DEFAULT_CONFIGURATION = "DEFAULT.CONFIG"; //$NON-NLS-1$
	private static final String STANDALONE_MODE_KEY = "STANDALONE.MODE"; //$NON-NLS-1$
	private static final String PROJECT_JAVA_VERSION = "PROJECT.JAVA.VERSION"; //$NON-NLS-1$
	private static final String INSTANCE_DATA_LOCATION_CONSTANT = "osgi.instance.area.default"; //$NON-NLS-1$
	private static final String FRAMEWORK_STORAGE_VALUE = "target/bundlecache"; //$NON-NLS-1$
	private static final String PROJECT_PATH_CONSTANT = "PROJECT.PATH"; //$NON-NLS-1$
	private static final String ALL_PROJECT_IDENTIFIERS = "ALL.PROJECT.IDENTIFIERS"; //$NON-NLS-1$
	private static final String PROJECT_NAME_CONSTANT = "PROJECT.NAME"; //$NON-NLS-1$
	private static final String OSGI_INSTANCE_AREA_CONSTANT = "osgi.instance.area"; //$NON-NLS-1$
	private static final String DEBUG_ENABLED = "debug.enabled"; //$NON-NLS-1$
	private static final String CONFIG_FILE_PATH = "CONFIG.FILE.PATH"; //$NON-NLS-1$
	private static final String LIST_RULES_SELECTED_ID = "LIST.RULES.SELECTED.ID"; //$NON-NLS-1$
	private static final String LICENSE_KEY = "LICENSE"; //$NON-NLS-1$
	private static final String AGENT_URL = "URL"; //$NON-NLS-1$
	private static final String DEV_MODE_KEY = "dev.mode.enabled"; //$NON-NLS-1$
	private static final String NATURE_IDS = "NATURE.IDS"; //$NON-NLS-1$
	private static final String SOURCE_FOLDER = "SOURCE.FOLDER"; //$NON-NLS-1$
	private static final String DEFAULT_SOURCE_FOLDER_PATH = "src/main/java"; //$NON-NLS-1$

	private static final String MAVEN_NATURE_ID = "org.eclipse.m2e.core.maven2Nature"; //$NON-NLS-1$
	private static final String ECLIPSE_PLUGIN_NATURE_ID = "org.eclipse.pde.PluginNature"; //$NON-NLS-1$
	private static final String JAVA_NATURE_ID = "org.eclipse.jdt.core.javanature"; //$NON-NLS-1$
	private static final String ECLIPSE_PLUGIN_PROJECT_NATURE_IDS = String.format("%s,%s,%s", MAVEN_NATURE_ID, //$NON-NLS-1$
			ECLIPSE_PLUGIN_NATURE_ID, JAVA_NATURE_ID);
	private static final String MAVEN_PROJECT_NATURE_IDS = MAVEN_NATURE_ID + "," + JAVA_NATURE_ID; //$NON-NLS-1$

	private Log log;
	private Map<String, String> configuration = new HashMap<>();
	private MavenProject rootProject;
	private Set<String> sessionProjects;
	private boolean jsparrowAlreadyRunningError = false;

	public MavenAdapter(MavenProject rootProject, Log log) {
		this.rootProject = rootProject;
		this.log = log;
		this.sessionProjects = new HashSet<>();
	}

	/**
	 * Creates a map with the required configurations for starting the equinox
	 * framework based on the provided {@link MavenParameter}. Additionally,
	 * adds the all projects related information to the configuration map.
	 * 
	 * @param parameters
	 *            a set of parameters provided from the mojo.
	 * @param projects
	 *            the list of all projects in the current session
	 * @param defaultYamlFile
	 *            the default {@code jsparrow.yml} file.
	 * @return an instance of {@link WorkingDirectory} for managing the working
	 *         directory of the equinox.
	 * @throws InterruptedException
	 *             if the working directory cannot be created
	 * @throws MojoExecutionException
	 *             if jSparrow is already started in the root project of the
	 *             current session.
	 */
	public WorkingDirectory setUp(MavenParameters parameters, List<MavenProject> projects, File defaultYamlFile)
			throws InterruptedException, MojoExecutionException {

		setProjectIds(projects);
		WorkingDirectory workingDirectory = setUp(parameters);
		String rootProjectIdentifier = findProjectIdentifier(rootProject);
		if (workingDirectory.isJsparrowStarted(rootProjectIdentifier)) {
			jsparrowAlreadyRunningError = true;
			log.error(NLS.bind(Messages.MavenAdapter_jSparrowAlreadyRunning, rootProject.getArtifactId()));
			throw new MojoExecutionException(Messages.MavenAdapter_jSparrowIsAlreadyRunning);
		}
		workingDirectory.lockProjects();

		for (MavenProject mavenProject : projects) {
			if (!isAggregateProject(mavenProject)) {
				addProjectConfiguration(mavenProject, defaultYamlFile);
			}
		}
		log.info(Messages.MavenAdapter_allProjectsLoaded);
		return workingDirectory;
	}

	/**
	 * Creates a map with the required configurations for starting the equinox
	 * framework based on the provided {@link MavenParameter}.
	 * 
	 * @param parameters
	 *            a set of parameters provided from the mojo.
	 * @return an instance of {@link WorkingDirectory} for managing the working
	 *         directory of the equinox.
	 * @throws InterruptedException
	 *             if the working directory cannot be created
	 */
	public WorkingDirectory setUp(MavenParameters parameters) throws InterruptedException {
		addInitialConfiguration(parameters);
		return prepareWorkingDirectory();
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
	 * <b>Note:</b> if the project represents and aggregate project, then no
	 * configuration is stored. Only the cofiguration of child projects need to
	 * be stored.
	 * 
	 * @param project
	 *            the maven project to store the configuration for
	 * @param configFile
	 *            the expected jsparrow.yml file
	 */
	private void addProjectConfiguration(MavenProject project, File configFile) {
		log.info(String.format(Messages.MavenAdapter_addingProjectConfiguration, project.getName()));

		File baseDir = project.getBasedir();
		String projectPath = baseDir.getAbsolutePath();
		String projectIdentifier = findProjectIdentifier(project);
		String artifactId = project.getArtifactId();

		String allIdentifiers = getAllProjectIdentifiers();
		configuration.put(ALL_PROJECT_IDENTIFIERS, joinWithComma(allIdentifiers, projectIdentifier));
		configuration.put(PROJECT_PATH_CONSTANT + DOT + projectIdentifier, projectPath);
		configuration.put(PROJECT_NAME_CONSTANT + DOT + projectIdentifier, artifactId);
		String yamlFilePath = findYamlFilePath(project, configFile.getName());
		log.info(Messages.MavenAdapter_jSparrowConfigurationFile + yamlFilePath);
		configuration.put(CONFIG_FILE_PATH + DOT + projectIdentifier, yamlFilePath);
		configuration.put(PROJECT_JAVA_VERSION + DOT + projectIdentifier, getCompilerCompliance(project));
		configuration.put(NATURE_IDS + DOT + projectIdentifier, findNatureIds(project));

	}

	private String findNatureIds(MavenProject project) {
		if (project.getPackaging()
			.equals("eclipse-plugin")) { //$NON-NLS-1$
			return ECLIPSE_PLUGIN_PROJECT_NATURE_IDS;
		} else {
			return MAVEN_PROJECT_NATURE_IDS;
		}
	}

	/**
	 * Finds the path of the corresponding yaml configuration file of the
	 * project. If the provided yamlFile exists, its path is immediately
	 * returned. Otherwise, finds the yaml file in the closest ancestor until
	 * reaching the {@link #rootProject}.
	 * 
	 * @param project
	 *            the project to find the configuration file for.
	 * @param yamlFile
	 *            expected yaml file
	 * @return the path of the corresponding yaml file
	 */
	protected String findYamlFilePath(MavenProject project, String yamlFile) {
		File projectYamlFile = joinPaths(project.getBasedir(), yamlFile).toFile();
		if (projectYamlFile.exists()) {
			return projectYamlFile.getAbsolutePath();
		}
		MavenProject parent = project;
		while ((parent = parent.getParent()) != null) {
			if (parent == rootProject) {
				break;
			}
			File parentBaseDir = parent.getBasedir();

			Path parentYamlPath = joinPaths(parentBaseDir, yamlFile);
			if (parentYamlPath.toFile()
				.exists()) {
				return parentYamlPath.toString();
			}
		}
		return joinPaths(rootProject.getBasedir(), yamlFile).toString();
	}

	protected Path joinPaths(File parentBaseDir, String yamlFile) {
		return Paths.get(parentBaseDir.getAbsolutePath(), yamlFile);
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

	void addInitialConfiguration(MavenParameters config) {
		boolean useDefaultConfig = config.getUseDefaultConfig()
			.orElse(false);
		configuration.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
		configuration.put(Constants.FRAMEWORK_STORAGE, FRAMEWORK_STORAGE_VALUE);
		configuration.put(INSTANCE_DATA_LOCATION_CONSTANT, System.getProperty(USER_DIR));
		configuration.put(SOURCE_FOLDER, DEFAULT_SOURCE_FOLDER_PATH);

		/*
		 * This is solution B from this article:
		 * https://spring.io/blog/2009/01/19/exposing-the-boot-classpath-in-
		 * osgi/
		 */
		configuration.put(Constants.FRAMEWORK_BOOTDELEGATION, "javax.*,org.xml.*"); //$NON-NLS-1$
		configuration.put(DEBUG_ENABLED, Boolean.toString(log.isDebugEnabled()));
		configuration.put(STANDALONE_MODE_KEY, config.getMode());
		configuration.put(SELECTED_PROFILE, config.getProfile()
			.orElse("")); //$NON-NLS-1$
		configuration.put(USE_DEFAULT_CONFIGURATION, Boolean.toString(useDefaultConfig));
		configuration.put(LICENSE_KEY, config.getLicense());
		configuration.put(AGENT_URL, config.getUrl());
		config.getRuleId()
			.ifPresent(ruleId -> configuration.put(LIST_RULES_SELECTED_ID, ruleId));
		configuration.put(DEV_MODE_KEY, Boolean.toString(config.isDevMode()));
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
	public WorkingDirectory prepareWorkingDirectory() throws InterruptedException {

		File directory = createJsparrowTempDirectory();

		if (directory.exists() || directory.mkdirs()) {
			String directoryAbsolutePath = directory.getAbsolutePath();
			setSystemProperty(USER_DIR, directoryAbsolutePath);
			configuration.put(OSGI_INSTANCE_AREA_CONSTANT, directoryAbsolutePath);

			String loggerInfo = NLS.bind(Messages.MavenAdapter_setUserDir, directoryAbsolutePath);
			log.info(loggerInfo);
		} else {
			throw new InterruptedException(Messages.MavenAdapter_couldnotCreateTempFolder);
		}
		return createWorkingDirectory(directory);
	}

	protected WorkingDirectory createWorkingDirectory(File directory) {
		return new WorkingDirectory(directory, sessionProjects, log);
	}

	protected void setSystemProperty(String key, String directoryAbsolutePath) {
		System.setProperty(key, directoryAbsolutePath);
	}

	protected File createJsparrowTempDirectory() {
		return new File(WorkingDirectory.calculateJsparrowTempFolderPath()).getAbsoluteFile();
	}

	public void setProjectIds(List<MavenProject> allProjects) {
		this.sessionProjects = allProjects.stream()
			.map(this::findProjectIdentifier)
			.collect(Collectors.toSet());
	}

	public boolean isJsparrowRunningFlag() {
		return jsparrowAlreadyRunningError;
	}

	public Map<String, String> getConfiguration() {
		return configuration;
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

		String sourceFromPlugin = getCompilerComplienceFromCompilerPlugin(buildPlugins);
		if (!sourceFromPlugin.isEmpty()) {
			return sourceFromPlugin;
		}

		Properties projectProperties = project.getProperties();

		String sourceProperty = projectProperties.getProperty(MAVEN_COMPILER_PLUGIN_PROPERTY_SOURCE_NAME);
		if (null != sourceProperty) {
			return sourceProperty;
		}

		return MAVEN_COMPILER_PLUGIN_DEFAULT_JAVA_VERSION;
	}

	private String getCompilerComplienceFromCompilerPlugin(List<Plugin> buildPlugins) {
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
		return ""; //$NON-NLS-1$
	}
}
