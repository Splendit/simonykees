package eu.jsparrow.maven.adapter;

import static eu.jsparrow.maven.adapter.ConfigurationKeys.AGENT_URL;
import static eu.jsparrow.maven.adapter.ConfigurationKeys.CONFIG_FILE_OVERRIDE;
import static eu.jsparrow.maven.adapter.ConfigurationKeys.DEBUG_ENABLED;
import static eu.jsparrow.maven.adapter.ConfigurationKeys.FRAMEWORK_STORAGE_VALUE;
import static eu.jsparrow.maven.adapter.ConfigurationKeys.INSTANCE_DATA_LOCATION_CONSTANT;
import static eu.jsparrow.maven.adapter.ConfigurationKeys.LICENSE_KEY;
import static eu.jsparrow.maven.adapter.ConfigurationKeys.LIST_RULES_SELECTED_ID;
import static eu.jsparrow.maven.adapter.ConfigurationKeys.OSGI_INSTANCE_AREA_CONSTANT;
import static eu.jsparrow.maven.adapter.ConfigurationKeys.PROXY_SETTINGS;
import static eu.jsparrow.maven.adapter.ConfigurationKeys.ROOT_CONFIG_PATH;
import static eu.jsparrow.maven.adapter.ConfigurationKeys.ROOT_PROJECT_BASE_PATH;
import static eu.jsparrow.maven.adapter.ConfigurationKeys.SELECTED_PROFILE;
import static eu.jsparrow.maven.adapter.ConfigurationKeys.STANDALONE_MODE_KEY;
import static eu.jsparrow.maven.adapter.ConfigurationKeys.USER_DIR;
import static eu.jsparrow.maven.adapter.ConfigurationKeys.USE_DEFAULT_CONFIGURATION;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Constants;

import eu.jsparrow.maven.i18n.Messages;
import eu.jsparrow.maven.util.MavenProjectUtil;

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
	 * @param configFileOverride
	 *            path to the provided yml configuration file.
	 * @param fallbackConfigFile
	 *            the default {@code jsparrow.yml} file.
	 * @return an instance of {@link WorkingDirectory} for managing the working
	 *         directory of the equinox framework.
	 * @throws InterruptedException
	 *             if the working directory cannot be created
	 * @throws MojoExecutionException
	 *             if jSparrow is already started in the root project of the
	 *             current session.
	 * @throws JsonProcessingException
	 */
	public WorkingDirectory setUpConfiguration(MavenParameters parameters, List<MavenProject> projects,
			File configFileOverride, File fallbackConfigFile, List<Proxy> proxies)
			throws InterruptedException, MojoExecutionException {

		log.info(Messages.MavenAdapter_setUpConfiguration);

		setProjectIds(projects);
		setProxySettings(proxies);
		WorkingDirectory workingDirectory = setUpConfiguration(parameters);
		String rootProjectIdentifier = MavenProjectUtil.findProjectIdentifier(rootProject);

		if (workingDirectory.isJsparrowStarted(rootProjectIdentifier)) {
			jsparrowAlreadyRunningError = true;
			log.error(NLS.bind(Messages.MavenAdapter_jSparrowAlreadyRunning, rootProject.getArtifactId()));
			throw new MojoExecutionException(Messages.MavenAdapter_jSparrowIsAlreadyRunning);
		}

		configuration.put(ROOT_CONFIG_PATH, fallbackConfigFile.getAbsolutePath());
		configuration.put(CONFIG_FILE_OVERRIDE,
				(configFileOverride == null) ? null : configFileOverride.getAbsolutePath());
		configuration.put(ROOT_PROJECT_BASE_PATH, rootProject.getBasedir()
			.getAbsolutePath());

		workingDirectory.lockProjects();

		log.info(Messages.MavenAdapter_configurationSetUp);
		return workingDirectory;
	}

	private void setProxySettings(List<Proxy> proxies) {
		String settingsDelimiter = "^"; //$NON-NLS-1$
		String proxyDelimiter = "§"; //$NON-NLS-1$
		StringBuilder proxySettingsString = new StringBuilder();

		proxies.stream()
			.forEach(proxy -> {
				String type = proxy.getProtocol();
				String host = proxy.getHost();
				int port = proxy.getPort();
				String username = proxy.getUsername();
				String password = proxy.getPassword();
				String nonProxyHosts = proxy.getNonProxyHosts();

				proxySettingsString.append("type=") //$NON-NLS-1$
					.append(type)
					.append(settingsDelimiter);

				proxySettingsString.append("host=") //$NON-NLS-1$
					.append(host)
					.append(settingsDelimiter);

				proxySettingsString.append("port=") //$NON-NLS-1$
					.append(port)
					.append(settingsDelimiter);

				proxySettingsString.append("username=") //$NON-NLS-1$
					.append(username)
					.append(settingsDelimiter);

				proxySettingsString.append("password=") //$NON-NLS-1$
					.append(password)
					.append(settingsDelimiter);

				proxySettingsString.append("nonProxyHosts=") //$NON-NLS-1$
					.append(nonProxyHosts)
					.append(settingsDelimiter);

				proxySettingsString.append(proxyDelimiter);
			});

		configuration.put(PROXY_SETTINGS, proxySettingsString.toString());
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

	void addInitialConfiguration(MavenParameters config) {
		boolean useDefaultConfig = config.getUseDefaultConfig();
		configuration.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
		configuration.put(Constants.FRAMEWORK_STORAGE, FRAMEWORK_STORAGE_VALUE);
		configuration.put(INSTANCE_DATA_LOCATION_CONSTANT, System.getProperty(USER_DIR));

		/*
		 * This is solution B from this article:
		 * https://spring.io/blog/2009/01/19/exposing-the-boot-classpath-in-
		 * osgi/
		 */
		configuration.put(Constants.FRAMEWORK_BOOTDELEGATION, "javax.*,org.xml.*"); //$NON-NLS-1$
		configuration.put(DEBUG_ENABLED, Boolean.toString(log.isDebugEnabled()));
		configuration.put(STANDALONE_MODE_KEY, config.getMode());
		configuration.put(SELECTED_PROFILE, config.getProfile());
		configuration.put(USE_DEFAULT_CONFIGURATION, Boolean.toString(useDefaultConfig));
		configuration.put(LICENSE_KEY, config.getLicense());
		configuration.put(AGENT_URL, config.getUrl());
		config.getRuleId()
			.ifPresent(ruleId -> configuration.put(LIST_RULES_SELECTED_ID, ruleId));
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

		log.debug(Messages.MavenAdapter_prepareWorkingDirectory);
		File directory = createJsparrowTempDirectory();

		if (directory.exists() || directory.mkdirs()) {
			String directoryAbsolutePath = directory.getAbsolutePath();
			setSystemProperty(USER_DIR, directoryAbsolutePath);
			configuration.put(OSGI_INSTANCE_AREA_CONSTANT, directoryAbsolutePath);

			String loggerInfo = NLS.bind(Messages.MavenAdapter_setUserDir, directoryAbsolutePath);
			log.debug(loggerInfo);
		} else {
			throw new InterruptedException(Messages.MavenAdapter_couldnotCreateTempFolder);
		}

		log.debug(Messages.MavenAdapter_workingDirectoryPrepared);
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
		log.debug(Messages.MavenAdapter_setProjectIds);
		this.sessionProjects = allProjects.stream()
			.map(MavenProjectUtil::findProjectIdentifier)
			.collect(Collectors.toSet());
		log.debug(Messages.MavenAdapter_projectIdsSet);
	}

	public boolean isJsparrowRunningFlag() {
		return jsparrowAlreadyRunningError;
	}

	public Map<String, String> getConfiguration() {
		return configuration;
	}

}
