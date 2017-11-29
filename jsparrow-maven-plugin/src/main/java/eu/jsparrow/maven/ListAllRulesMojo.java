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

import eu.jsparrow.maven.util.MavenUtil;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.3.0
 */
@SuppressWarnings("nls")
@Mojo(name = "listRules")
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

	@Parameter(property = "rule")
	private String ruleId;

	private static final String LIST_RULES = "LIST.RULES";
	private static final String LIST_RULES_SELECTED_ID = "LIST.RULES.SELECTED.ID";

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			final Map<String, String> configuration = new HashMap<>();
			configuration.put(LIST_RULES, Boolean.toString(true));
			if (ruleId != null && !ruleId.isEmpty()) {
				configuration.put(LIST_RULES_SELECTED_ID, ruleId);
			}

			MavenUtil.startOSGI(project, mavenHome, getLog(), configuration);
		} catch (BundleException | InterruptedException e) {
			getLog().error(e.getMessage(), e);
		}
	}

}
