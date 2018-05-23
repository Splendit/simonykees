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

import eu.jsparrow.maven.adapter.MavenParameters;
import eu.jsparrow.maven.adapter.StandaloneAdapter;
import eu.jsparrow.maven.enums.StandaloneMode;
import eu.jsparrow.maven.i18n.Messages;

/**
 * Starts Equinox framework and headless version of jSparrow Eclipse plugin.
 * 
 * @author Andreja Sambolec, Matthias Webhofer, Ardit Ymeri
 * @since 2.2.1
 *
 */
@Mojo(name = "refactor", defaultPhase = LifecyclePhase.INSTALL, requiresDependencyResolution = ResolutionScope.COMPILE, aggregator = true)
public class RefactorMojo extends AbstractMojo {
	
	private static final String POM_FILE_NAME = "pom.xml"; //$NON-NLS-1$

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
		StandaloneAdapter serviceInstance = StandaloneAdapter.getInstance();
		String mode = StandaloneMode.REFACTOR.name();
		try {
			if (!serviceInstance.isAdapterInitialized()) {
				MavenParameters config = new MavenParameters(project, log, configFile, mavenSession, mode, license,
						url);
				config.setMavenHome(mavenHome);
				config.setProfile(profile);
				config.setUseDefaultConfig(useDefaultConfig);
				config.setDevMode(devMode);

				boolean adapterLoadad = serviceInstance.lazyLoadMavenAdapter(config);
				if (!adapterLoadad) {
					throw new MojoExecutionException(Messages.Mojo_jSparrowIsAlreadyRunning);
				}
				serviceInstance.copyDependencies(project, log);
				serviceInstance.setRootProjectPomPath(project.getBasedir()
					.getAbsolutePath() + File.separator + POM_FILE_NAME, log);
			}

			for (MavenProject mavenProject : mavenSession.getAllProjects()) {
				serviceInstance.addProjectConfiguration(mavenProject, log, configFile);
			}
			log.info(Messages.RefactorMojo_allProjectsLoaded);
			serviceInstance.startStandaloneBundle(log);

		} catch (BundleException | InterruptedException e1) {
			log.debug(e1.getMessage(), e1);
			log.error(e1.getMessage());
		}

	}
}
