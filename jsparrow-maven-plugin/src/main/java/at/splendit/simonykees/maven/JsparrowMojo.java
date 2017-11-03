package at.splendit.simonykees.maven;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.osgi.framework.BundleException;

import at.splendit.simonykees.maven.util.MavenUtil;

/**
 * Starts Equinox framework and headless version of jSparrow Eclipse plugin.
 * 
 * @author Andreja Sambolec
 * @since 2.2.1
 *
 */
@SuppressWarnings("nls")
@Mojo(name = "refactor", defaultPhase = LifecyclePhase.INSTALL, requiresDependencyResolution = ResolutionScope.COMPILE, requiresProject = true)
public class JsparrowMojo extends AbstractMojo {

	/**
	 * Maven project on which plugin goal is executed
	 */
	@Parameter(defaultValue = "${project}", required = true)
	private MavenProject project;

	/**
	 * Value of maven home environment variable
	 */
	@Parameter(defaultValue = "${maven.home}", required = true)
	private String mavenHome;

	@Parameter(defaultValue = "jsparrow.yml", property = "configFile")
	private File configFile;

	@Parameter(defaultValue = "", property = "profile")
	private String profile;

	// CONSTANTS
	private static final String CONFIG_FILE_PATH = "CONFIG.FILE.PATH";
	private static final String SELECTED_PROFILE = "PROFILE.SELECTED";

	public void execute() throws MojoExecutionException {
		try {
			final Map<String, String> configuration = new HashMap<>();
			configuration.put(CONFIG_FILE_PATH,
					(configFile.exists() && !configFile.isDirectory()) ? configFile.getAbsolutePath() : "");
			configuration.put(SELECTED_PROFILE, (profile == null) ? "" : profile);

			MavenUtil.startOSGI(project, mavenHome, getLog(), configuration);
		} catch (BundleException | InterruptedException e) {
			getLog().error(e.getMessage(), e);
		}
	}

}
