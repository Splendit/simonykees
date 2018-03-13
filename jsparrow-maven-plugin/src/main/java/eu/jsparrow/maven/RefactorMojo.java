package eu.jsparrow.maven;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.osgi.framework.BundleException;

import eu.jsparrow.adapter.AdapterService;
import eu.jsparrow.adapter.MavenAdapter;
import eu.jsparrow.maven.enums.StandaloneMode;
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

	// CONSTANTS
	private static final String CONFIG_FILE_PATH = "CONFIG.FILE.PATH";
	private static final String SELECTED_PROFILE = "PROFILE.SELECTED";
	private static final String USE_DEFAULT_CONFIGURATION = "DEFAULT.CONFIG";
	private static final String STANDALONE_MODE_KEY = "STANDALONE.MODE"; //$NON-NLS-1$
	private static final String PROJECT_JAVA_VERSION = "PROJECT.JAVA.VERSION";

	private static final String MAVEN_COMPILER_PLUGIN_ARTIFACT_ID = "maven-compiler-plugin";
	private static final String MAVEN_COMPILER_PLUGIN_CONFIGURATIN_SOURCE_NAME = "source";
	private static final String MAVEN_COMPILER_PLUGIN_DEFAULT_JAVA_VERSION = "1.5";

	/**
	 * MOJO entry point. Registers shutdown hook for clean up and starts equinox
	 * with the given configuration
	 */
	public void execute() throws MojoExecutionException {
//		MavenHelper mavenHelper = new MavenHelper(project, mavenHome, mavenSession, pluginManager, getLog());
		MavenAdapter adapter = AdapterService.lazyLoadMavenAdapter(project, mavenHome, mavenSession, getLog());

//		Runtime.getRuntime()
//			.addShutdownHook(adapter.createShutdownHook());

		try {

			final Map<String, String> configuration = new HashMap<>();
			configuration.put(STANDALONE_MODE_KEY, StandaloneMode.REFACTOR.name());

			configuration.put(CONFIG_FILE_PATH, configFile.getAbsolutePath());
			configuration.put(SELECTED_PROFILE, (profile == null) ? "" : profile);
			configuration.put(USE_DEFAULT_CONFIGURATION, Boolean.toString(useDefaultConfig));

			configuration.put(PROJECT_JAVA_VERSION, getCompilerCompliance());
			AdapterService.addProjectConfiguration(project, getLog(), configuration);

//			adapter.startOSGI(configuration, project);
		} catch (BundleException | InterruptedException e) {
			getLog().debug(e.getMessage(), e);
			getLog().error(e.getMessage());
		}
	}

	/**
	 * reads the current java source version from the maven-compiler-plugin
	 * configuration in the pom.xml. If no configuration is found, the java
	 * version is 1.5 by default (as stated in the documentation of
	 * maven-compiler-plugin:
	 * https://maven.apache.org/plugins/maven-compiler-plugin/).
	 * 
	 * @return the project's java version
	 */
	private String getCompilerCompliance() {
		List<Plugin> buildPlugins = project.getBuildPlugins();

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

		return MAVEN_COMPILER_PLUGIN_DEFAULT_JAVA_VERSION;
	}
}
