package eu.jsparrow.maven;

import java.io.File;

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
import eu.jsparrow.maven.adapter.EmbeddedMaven;
import eu.jsparrow.maven.adapter.MavenAdapter;
import eu.jsparrow.maven.adapter.MavenParameters;
import eu.jsparrow.maven.adapter.StandaloneAdapter;
import eu.jsparrow.maven.enums.StandaloneMode;

/**
 * Starts Equinox framework and headless version of jSparrow Eclipse plugin.
 * 
 * @author Andreja Sambolec, Matthias Webhofer, Ardit Ymeri
 * @since 2.2.1
 *
 */
@Mojo(name = "refactor", defaultPhase = LifecyclePhase.INSTALL, requiresDependencyResolution = ResolutionScope.COMPILE, aggregator = true)
public class RefactorMojo extends AbstractMojo {

	@Parameter(defaultValue = "${session}")
	private MavenSession mavenSession;

	@Component
	private BuildPluginManager pluginManager;

	/**
	 * Maven project on which plugin goal is executed
	 */
	@Parameter(defaultValue = "${project}", required = true)
	private MavenProject project;

	/**
	 * Value of maven home environment variable
	 */
	@Parameter(defaultValue = "${maven.home}")
	private String mavenHome;

	/**
	 * path to the configuration file. defaults to jsparrow.yml in the current
	 * directory.
	 */
	@Parameter(defaultValue = "jsparrow.yml", property = "configFile")
	private File configFile;

	/**
	 * selected profile. overrides the settings in the configuration file, if
	 * set by user.
	 */
	@Parameter(defaultValue = "", property = "profile")
	private String profile;

	@Parameter(property = "defaultConfiguration")
	protected boolean useDefaultConfig;

	@Parameter(property = "license")
	private String license;

	@Parameter(property = "url")
	private String url;

	@Parameter(property = "devMode")
	private boolean devMode;

	/**
	 * MOJO entry point. Registers shutdown hook for clean up and starts equinox
	 * with the given configuration
	 */
	public void execute() throws MojoExecutionException {

		Log log = getLog();
		String mode = StandaloneMode.REFACTOR.name();
		MavenParameters parameters = new MavenParameters(mode, license, url, profile, useDefaultConfig, devMode);
		MavenAdapter mavenAdapter = new MavenAdapter(project, log, configFile);
		StandaloneAdapter standaloneAdapter = new StandaloneAdapter(project, new BundleStarter(log));
		
		EmbeddedMaven embeddedMaven = new EmbeddedMaven(log, mavenHome);
		try {
			mavenAdapter.setUp(parameters, mavenSession.getAllProjects());
			standaloneAdapter.loadStandalone(mavenAdapter, new DependencyManager(log, embeddedMaven));
		} catch (BundleException | InterruptedException e1) {
			log.debug(e1.getMessage(), e1);
			log.error(e1.getMessage());
		}
	}
	
}
