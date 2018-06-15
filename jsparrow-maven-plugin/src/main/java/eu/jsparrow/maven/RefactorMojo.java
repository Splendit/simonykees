package eu.jsparrow.maven;

import java.io.File;
import java.util.List;

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
import org.osgi.framework.BundleException;

import eu.jsparrow.maven.adapter.BundleStarter;
import eu.jsparrow.maven.adapter.DependencyManager;
import eu.jsparrow.maven.adapter.MavenAdapter;
import eu.jsparrow.maven.adapter.MavenParameters;
import eu.jsparrow.maven.adapter.StandaloneLoader;
import eu.jsparrow.maven.adapter.WorkingDirectory;
import eu.jsparrow.maven.enums.StandaloneMode;
import eu.jsparrow.maven.i18n.Messages;

/**
 * Runs jSparrow on the Maven project.
 * 
 * @author Andreja Sambolec, Matthias Webhofer, Ardit Ymeri
 * @since 2.2.1
 *
 */
@Mojo(name = "refactor", defaultPhase = LifecyclePhase.INSTALL, requiresDependencyResolution = ResolutionScope.COMPILE, aggregator = true)
public class RefactorMojo extends AbstractMojo {

	private static final String JAVA_VERSION_PROPERTY_CONSTANT = "java.version"; //$NON-NLS-1$
	private static final String JAVA_VERSION_1_8 = "1.8"; //$NON-NLS-1$

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
	@Parameter(defaultValue = "jsparrow.yml", property = "configFile")
	private File configFile;

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

	public void execute() throws MojoExecutionException {
		Log log = getLog();

		// With version 1.0.0 of jSparrow Maven Plugin, only JDK 8 is supported.
		String javaVersion = System.getProperty(JAVA_VERSION_PROPERTY_CONSTANT);
		if (!javaVersion.startsWith(JAVA_VERSION_1_8)) {
			log.warn(Messages.RefactorMojo_suportJDK8);
			throw new MojoExecutionException(Messages.RefactorMojo_suportJDK8);
		}

		String mode = StandaloneMode.REFACTOR.name();
		MavenParameters parameters = new MavenParameters(mode, license, url, profile, defaultConfiguration);
		MavenAdapter mavenAdapter = new MavenAdapter(project, log);
		DependencyManager dependencyManager = new DependencyManager(log, mavenHome);
		List<MavenProject> projects = mavenSession.getAllProjects();
		BundleStarter bundleStarter = new BundleStarter(log);
		StandaloneLoader loader = new StandaloneLoader(project, bundleStarter);

		try {
			WorkingDirectory workingDirectory = mavenAdapter.setUpConfiguration(parameters, projects, configFile);
			addShutdownHook(bundleStarter, workingDirectory, mavenAdapter.isJsparrowRunningFlag());
			loader.loadStandalone(mavenAdapter, dependencyManager);
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
