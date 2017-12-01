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

import eu.jsparrow.maven.util.MavenHelper;

/**
 * This MOJO lists all rules with id, name and description. By specifying
 * {@code -Drule=<ruleId>} only the information about this rule is printed.
 * 
 * @author Matthias Webhofer
 * @since 2.3.0
 */
@SuppressWarnings("nls")
@Mojo(name = "list-rules")
public class ListAllRulesMojo extends AbstractMojo {

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
	 * if set, only the rule with the given id will be listed
	 */
	@Parameter(property = "rule")
	private String ruleId;

	// CONSTANTS
	private static final String LIST_RULES = "LIST.RULES";
	private static final String LIST_RULES_SELECTED_ID = "LIST.RULES.SELECTED.ID";

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
			configuration.put(LIST_RULES, Boolean.toString(true));
			if (ruleId != null && !ruleId.isEmpty()) {
				configuration.put(LIST_RULES_SELECTED_ID, ruleId);
			}

			mavenHelper.startOSGI(configuration);
		} catch (BundleException | InterruptedException e) {
			getLog().debug(e.getMessage(), e);
			getLog().error(e.getMessage());
		}
	}

}
