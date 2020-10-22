package eu.jsparrow.maven.mojo;

import java.io.File;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.osgi.framework.BundleException;

import eu.jsparrow.maven.adapter.BundleStarter;
import eu.jsparrow.maven.adapter.MavenAdapter;
import eu.jsparrow.maven.adapter.MavenParameters;
import eu.jsparrow.maven.adapter.StatisticsMetadata;
import eu.jsparrow.maven.adapter.WorkingDirectory;
import eu.jsparrow.maven.enums.StandaloneMode;
import eu.jsparrow.maven.i18n.Messages;
import eu.jsparrow.maven.util.JavaVersion;
import eu.jsparrow.maven.util.ProxyUtil;

@Mojo(name = "demo", requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.INITIALIZE, aggregator = true)
public class DemoMojo  extends AbstractMojo  {

	@Parameter(defaultValue = "${session}", readonly = true)
	private MavenSession mavenSession;

	@Component
	private BuildPluginManager pluginManager;

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	@Parameter(defaultValue = "${maven.home}", readonly = true)
	private String mavenHome;

	/**
	 * Path to the configuration file.
	 */
	@Parameter(property = "configFile")
	private File configFileOverride;

	/**
	 * Selected profile. Overrides the settings in the configuration file.
	 */
	@Parameter(defaultValue = "", property = "profile")
	private String profile;

	/**
	 * Use this parameter to use the default configuration.
	 */
	@Parameter(property = "defaultConfiguration")
	protected boolean defaultConfiguration;

	/**
	 * Specify the license key to use.
	 */
	@Parameter(property = "license")
	private String license;

	/**
	 * Specify the license server to use.
	 */
	@Parameter(property = "url")
	private String url;

	@Parameter(property = "startTime")
	private String startTime;

	@Parameter(defaultValue = "${project.groupId}", property = "repoOwner")
	private String repoOwner;

	@Parameter(defaultValue = "${project.name}", property = "repoName")
	private String repoName;

	@Parameter(property = "sendStatistics")
	private boolean sendStatistics;

	public void execute() throws MojoExecutionException {
		Log log = getLog();

		if (!JavaVersion.isJava8or11()) {
			log.warn(Messages.RefactorMojo_supportJDK8and11);
			throw new MojoExecutionException(Messages.RefactorMojo_supportJDK8and11);
		}

		String mode = StandaloneMode.DEMO.name();
		String start = startTime == null ? Instant.now().toString() : startTime;
		StatisticsMetadata statisticsMetadata = new StatisticsMetadata(start, repoOwner, repoName);
		MavenParameters parameters = new MavenParameters(mode, license, url, profile,
				defaultConfiguration, statisticsMetadata, sendStatistics);
		MavenAdapter mavenAdapter = new MavenAdapter(project, log);
		List<MavenProject> projects = mavenSession.getProjects();
		BundleStarter bundleStarter = new BundleStarter(log);
		File fallbackConfigFile = Paths.get(project.getBasedir()
			.getAbsolutePath(), "jsparrow.yml") //$NON-NLS-1$
			.toFile();
		Stream<Proxy> proxies = ProxyUtil.getHttpProxies(mavenSession);

		try {
			WorkingDirectory workingDirectory = mavenAdapter.setUpConfiguration(parameters, projects,
					configFileOverride, fallbackConfigFile, proxies);
			addShutdownHook(bundleStarter, workingDirectory, mavenAdapter.isJsparrowRunningFlag());
			bundleStarter.runStandalone(mavenAdapter.getConfiguration());
		} catch (BundleException | InterruptedException e1) {
			log.debug(e1.getMessage(), e1);
			log.error(e1.getMessage());
		}
	}

	/**
	 * Registers a hook which is executed either when the program exits or the
	 * virtual machine is terminated in response to a user interrupt.
	 * <b>Note:</b> The equinox framework must be shut down before the working
	 * directory is cleared.
	 * 
	 * @param starter
	 *            the instance of {@link BundleStarter} which is responsible for
	 *            starting/shutting down the equinox framework.
	 * @param workingDirectory
	 *            an instance of {@link WorkingDirectory} which is responsible
	 *            for reading/cleaning the working directory.
	 * @param dependenciesFolderName
	 *            the name of the folder containing the copied dependencies
	 * @param jSparrowStartedFlag
	 *            an indicator whether jSparrow already started flag has been
	 *            raised.
	 */
	private void addShutdownHook(BundleStarter starter, WorkingDirectory workingDirectory,
			boolean jSparrowStartedFlag) {

		Runtime.getRuntime()
			.addShutdownHook(new Thread(() -> {
				starter.shutdownFramework();
				if (!jSparrowStartedFlag) {
					workingDirectory.cleanUp();
				}
			}));
	}
}
