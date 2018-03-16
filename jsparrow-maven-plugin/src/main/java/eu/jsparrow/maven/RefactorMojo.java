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

import eu.jsparrow.adapter.StandaloneAdapter;
import eu.jsparrow.adapter.MavenParameters;
import eu.jsparrow.maven.enums.StandaloneMode;

/**
 * Starts Equinox framework and headless version of jSparrow Eclipse plugin.
 * 
 * @author Andreja Sambolec, Matthias Webhofer, Ardit Ymeri
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

	/**
	 * MOJO entry point. Registers shutdown hook for clean up and starts equinox
	 * with the given configuration
	 */
	public void execute() throws MojoExecutionException {

		Log log = getLog();
		StandaloneAdapter serviceInstance = StandaloneAdapter.getInstance();
		String mode = StandaloneMode.REFACTOR.name();
		boolean adapterLoadad = false;
		try {
			if(!serviceInstance.isAdapterInitialized()) {
				MavenParameters config = new MavenParameters(project, log, configFile, mavenSession, mode);
				config.setMavenHome(mavenHome);
				config.setProfile(profile);
				config.setUseDefaultConfig(useDefaultConfig);
				
				adapterLoadad = serviceInstance.lazyLoadMavenAdapter(config);
				if (!adapterLoadad) {
					throw new MojoExecutionException("jSparrow is already running...");
				}
			}

			serviceInstance.addProjectConfiguration(project, log, configFile);

		} catch (BundleException | InterruptedException e1) {
			log.debug(e1.getMessage(), e1);
			log.error(e1.getMessage());
		}

	}
}
