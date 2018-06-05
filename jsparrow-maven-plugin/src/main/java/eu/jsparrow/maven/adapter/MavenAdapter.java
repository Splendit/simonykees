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

	public static final String DOT = "."; //$NON-NLS-1$

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
	 *         directory of the equinox framework.
	 * @throws InterruptedException
	 *             if the working directory cannot be created
	 * @throws MojoExecutionException
	 *             if jSparrow is already started in the root project of the
	 *             current session.
	 */
	public WorkingDirectory setUpConfiguration(MavenParameters parameters, List<MavenProject> projects,
			File defaultYamlFile) throws InterruptedException, MojoExecutionException {

		setProjectIds(projects);
		WorkingDirectory workingDirectory = setUpConfiguration(parameters);
		String rootProjectIdentifier = findProjectIdentifier(rootProject);
		if (workingDirectory.isJsparrowStarted(rootProjectIdentifier)) {
			jsparrowAlreadyRunningError = true;
			log.error(NLS.bind(Messages.MavenAdapter_jSparrowAlreadyRunning, rootProject.getArtifactId()));
			throw new MojoExecutionException(Messages.MavenAdapter_jSparrowIsAlreadyRunning);
		}
		configuration.put(ConfigurationKeys.ROOT_CONFIG_PATH, defaultYamlFile.getAbsolutePath());
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
	public WorkingDirectory setUpConfiguration(MavenParameters parameters) throws InterruptedException {
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
	 * @param project
	 *            the maven project to store the configuration for
	 * @param defaultYamlFile
	 *            the expected jsparrow.yml file
	 */
	private void addProjectConfiguration(MavenProject project, File defaultYamlFile) {
		log.info(String.format(Messages.MavenAdapter_addingProjectConfiguration, project.getName()));

		File baseDir = project.getBasedir();
		String projectPath = baseDir.getAbsolutePath();
		String projectIdentifier = findProjectIdentifier(project);
		String artifactId = project.getArtifactId();

		String allIdentifiers = getAllProjectIdentifiers();
		configuration.put(ConfigurationKeys.ALL_PROJECT_IDENTIFIERS, joinWithComma(allIdentifiers, projectIdentifier));
		configuration.put(ConfigurationKeys.PROJECT_PATH_CONSTANT + DOT + projectIdentifier, projectPath);
		configuration.put(ConfigurationKeys.PROJECT_NAME_CONSTANT + DOT + projectIdentifier, artifactId);
		String yamlFilePath = findYamlFilePath(project, defaultYamlFile);
		log.info(Messages.MavenAdapter_jSparrowConfigurationFile + yamlFilePath);
		configuration.put(ConfigurationKeys.CONFIG_FILE_PATH + DOT + projectIdentifier, yamlFilePath);
		configuration.put(ConfigurationKeys.PROJECT_JAVA_VERSION + DOT + projectIdentifier,
				getCompilerCompliance(project));
		configuration.put(ConfigurationKeys.NATURE_IDS + DOT + projectIdentifier, findNatureIds(project));

	}

	private String findNatureIds(MavenProject project) {
		if (project.getPackaging()
			.equals("eclipse-plugin")) { //$NON-NLS-1$
			return ConfigurationKeys.ECLIPSE_PLUGIN_PROJECT_NATURE_IDS;
		} else {
			return ConfigurationKeys.MAVEN_PROJECT_NATURE_IDS;
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
	 * @param defaultYamlFile
	 *            the default yaml file if one on the project base directory
	 *            does not exist
	 * @return the path of the corresponding yaml file
	 */
	protected String findYamlFilePath(MavenProject project, File defaultYamlFile) {
		String yamlFileName = defaultYamlFile.getName();
		File baseDir = project.getBasedir();
		Path yamlPath = joinPaths(yamlFileName, baseDir);
		if (yamlPath.toFile()
			.exists()) {
			return yamlPath.toString();
		}

		MavenProject parent = project;
		while ((parent = parent.getParent()) != null) {
			if (parent == rootProject) {
				break;
			}
			File parentBaseDir = parent.getBasedir();
			Path parentYamlPath = joinPaths(yamlFileName, parentBaseDir);
			if (parentYamlPath.toFile()
				.exists()) {
				return parentYamlPath.toString();
			}
		}
		return defaultYamlFile.getAbsolutePath();
	}

	protected Path joinPaths(String yamlFileName, File parentBaseDir) {
		return Paths.get(parentBaseDir.getAbsolutePath(), yamlFileName);
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
		return configuration.getOrDefault(ConfigurationKeys.ALL_PROJECT_IDENTIFIERS, ""); //$NON-NLS-1$
	}

	void addInitialConfiguration(MavenParameters config) {
		boolean useDefaultConfig = config.getUseDefaultConfig();
		configuration.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
		configuration.put(Constants.FRAMEWORK_STORAGE, ConfigurationKeys.FRAMEWORK_STORAGE_VALUE);
		configuration.put(ConfigurationKeys.INSTANCE_DATA_LOCATION_CONSTANT,
				System.getProperty(ConfigurationKeys.USER_DIR));
		configuration.put(ConfigurationKeys.SOURCE_FOLDER, ConfigurationKeys.DEFAULT_SOURCE_FOLDER_PATH);

		/*
		 * This is solution B from this article:
		 * https://spring.io/blog/2009/01/19/exposing-the-boot-classpath-in-
		 * osgi/
		 */
		configuration.put(Constants.FRAMEWORK_BOOTDELEGATION, "javax.*,org.xml.*"); //$NON-NLS-1$
		configuration.put(ConfigurationKeys.DEBUG_ENABLED, Boolean.toString(log.isDebugEnabled()));
		configuration.put(ConfigurationKeys.STANDALONE_MODE_KEY, config.getMode());
		configuration.put(ConfigurationKeys.SELECTED_PROFILE, config.getProfile());
		configuration.put(ConfigurationKeys.USE_DEFAULT_CONFIGURATION, Boolean.toString(useDefaultConfig));
		configuration.put(ConfigurationKeys.LICENSE_KEY, config.getLicense());
		configuration.put(ConfigurationKeys.AGENT_URL, config.getUrl());
		config.getRuleId()
			.ifPresent(ruleId -> configuration.put(ConfigurationKeys.LIST_RULES_SELECTED_ID, ruleId));
		configuration.put(ConfigurationKeys.DEV_MODE_KEY, Boolean.toString(config.isDevMode()));
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
			setSystemProperty(ConfigurationKeys.USER_DIR, directoryAbsolutePath);
			configuration.put(ConfigurationKeys.OSGI_INSTANCE_AREA_CONSTANT, directoryAbsolutePath);

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
	 * Note: This setting determines, which rules can be applied by default. We
	 * must use the same default version as Maven. Otherwise Maven would compile
	 * the sources with Java 1.5 anyways and our rules for 1.6 and above would
	 * result in compilation errors.
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

		String sourceProperty = projectProperties
			.getProperty(ConfigurationKeys.MAVEN_COMPILER_PLUGIN_PROPERTY_SOURCE_NAME);
		if (null != sourceProperty) {
			return sourceProperty;
		}

		return ConfigurationKeys.MAVEN_COMPILER_PLUGIN_DEFAULT_JAVA_VERSION;
	}

	private String getCompilerComplienceFromCompilerPlugin(List<Plugin> buildPlugins) {
		for (Plugin plugin : buildPlugins) {
			if (ConfigurationKeys.MAVEN_COMPILER_PLUGIN_ARTIFACT_ID.equals(plugin.getArtifactId())) {
				Xpp3Dom pluginConfig = (Xpp3Dom) plugin.getConfiguration();
				if (pluginConfig != null) {
					for (Xpp3Dom child : pluginConfig.getChildren()) {
						if (ConfigurationKeys.MAVEN_COMPILER_PLUGIN_CONFIGURATIN_SOURCE_NAME.equals(child.getName())) {
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
