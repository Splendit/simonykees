package eu.jsparrow.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.osgi.framework.BundleException;

import eu.jsparrow.maven.adapter.BundleStarter;
import eu.jsparrow.maven.adapter.MavenAdapter;
import eu.jsparrow.maven.adapter.MavenParameters;
import eu.jsparrow.maven.adapter.StandaloneAdapter;
import eu.jsparrow.maven.enums.StandaloneMode;

/**
 * This MOJO prints all rules with name and id in a table.
 * 
 * @author Matthias Webhofer
 * @since 2.3.0
 */
@Mojo(name = "list-rules-short", aggregator = true)
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

	/**
	 * MOJO entry point. Registers shutdown hook for clean up and starts equinox
	 * with the given configuration
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		Log log = getLog();

		String mode = StandaloneMode.LIST_RULES_SHORT.name();
		MavenParameters parameters = new MavenParameters(mode);
		MavenAdapter mavenAdapter = new MavenAdapter(project, log);
		StandaloneAdapter serviceInstance = new StandaloneAdapter(project, new BundleStarter(log));

		try {
			mavenAdapter.setUp(parameters);
			serviceInstance.loadStandalone(mavenAdapter);
		} catch (BundleException | InterruptedException e1) {
			log.debug(e1.getMessage(), e1);
			log.error(e1.getMessage());
		}
	}
}
