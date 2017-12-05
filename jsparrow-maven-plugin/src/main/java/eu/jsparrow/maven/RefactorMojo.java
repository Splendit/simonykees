package eu.jsparrow.maven;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.osgi.framework.BundleException;

import eu.jsparrow.maven.util.MavenHelper;

/**
 * Starts Equinox framework and headless version of jSparrow Eclipse plugin.
 * 
 * @author Andreja Sambolec, Matthias Webhofer
 * @since 2.2.1
 *
 */
@SuppressWarnings("nls")
@Mojo(name = "refactor", defaultPhase = LifecyclePhase.INSTALL, requiresDependencyResolution = ResolutionScope.COMPILE, requiresProject = true)
public class RefactorMojo extends AbstractMojo {

	@Parameter(defaultValue = "${session}")
	private MavenSession mavenSession;

	@Component
	private BuildPluginManager pluginManager;

	/**
	 * Maven project on which plugin goal is executed
	 */
	@Parameter(defaultValue = "${project}", required = true)
	MavenProject project;

	/**
	 * Value of maven home environment variable
	 */
	@Parameter(defaultValue = "${maven.home}")
	String mavenHome;

	/**
	 * path to the configuration file. defaults to jsparrow.yml in the current
	 * directory.
	 */
	@Parameter(defaultValue = "jsparrow.yml", property = "configFile")
	protected File configFile;

	/**
	 * selected profile. overrides the settings in the configuration file, if
	 * set by user.
	 */
	@Parameter(defaultValue = "", property = "profile")
	protected String profile;

	// CONSTANTS
	public static final String CONFIG_FILE_PATH = "CONFIG.FILE.PATH";
	public static final String SELECTED_PROFILE = "PROFILE.SELECTED";

	/**
	 * MOJO entry point. Registers shutdown hook for clean up and starts equinox
	 * with the given configuration
	 */
	public void execute() throws MojoExecutionException {
		MavenHelper mavenHelper = new MavenHelper(project, mavenHome, mavenSession, pluginManager, getLog());

		Runtime.getRuntime()
			.addShutdownHook(mavenHelper.createShutdownHook());

		try {

			final Map<String, String> configuration = new HashMap<>();
			configuration.put(CONFIG_FILE_PATH,
					(configFile.exists() && !configFile.isDirectory()) ? configFile.getAbsolutePath() : "");
			configuration.put(SELECTED_PROFILE, (profile == null) ? "" : profile);

			mavenHelper.startOSGI(configuration);
		} catch (BundleException | InterruptedException e) {
			getLog().debug(e.getMessage(), e);
			getLog().error(e.getMessage());
		}
	}
}
