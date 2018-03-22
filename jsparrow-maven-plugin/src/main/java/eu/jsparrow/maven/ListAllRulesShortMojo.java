package eu.jsparrow.maven;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.osgi.framework.BundleException;

import eu.jsparrow.maven.enums.StandaloneMode;
import eu.jsparrow.maven.util.MavenHelper;

/**
 * This MOJO prints all rules with name and id in a table.
 * 
 * @author Matthias Webhofer
 * @since 2.3.0
 */
@Mojo(name = "list-rules-short")
public class ListAllRulesShortMojo extends AbstractMojo {

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

	// CONSTANTS
	private static final String STANDALONE_MODE_KEY = "STANDALONE.MODE"; //$NON-NLS-1$

	/**
	 * MOJO entry point. Registers shutdown hook for clean up and starts equinox
	 * with the given configuration
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		MavenHelper mavenHelper = new MavenHelper(project, mavenHome, getLog());

		Runtime.getRuntime()
			.addShutdownHook(mavenHelper.createShutdownHook());

		try {
			final Map<String, String> configuration = new HashMap<>();
			configuration.put(STANDALONE_MODE_KEY, StandaloneMode.LIST_RULES_SHORT.name());

			mavenHelper.startOSGI(configuration);
		} catch (BundleException | InterruptedException e) {
			getLog().debug(e.getMessage(), e);
			getLog().error(e.getMessage());
		}
	}
}
