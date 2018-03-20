package eu.jsparrow.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.osgi.framework.BundleException;

import eu.jsparrow.adapter.MavenParameters;
import eu.jsparrow.adapter.StandaloneAdapter;
import eu.jsparrow.maven.enums.StandaloneMode;
import eu.jsparrow.maven.i18n.Messages;

/**
 * This MOJO lists all rules with id, name and description. By specifying
 * {@code -Drule=<ruleId>} only the information about this rule is printed.
 * 
 * @author Matthias Webhofer
 * @since 2.3.0
 */
@Mojo(name = "list-rules", aggregator = true)
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
	@Parameter(property = "rules")
	private String ruleId;

	/**
	 * MOJO entry point. Registers shutdown hook for clean up and starts equinox
	 * with the given configuration
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		Log log = getLog();
		StandaloneAdapter serviceInstance = StandaloneAdapter.getInstance();
		String mode = StandaloneMode.LIST_RULES.name();

		try {
			if (!serviceInstance.isAdapterInitialized()) {
				MavenParameters config = new MavenParameters(project, log, mode);
				config.setRuleId(ruleId);

				boolean adapterLoadad = serviceInstance.lazyLoadMavenAdapter(config);
				if (!adapterLoadad) {
					throw new MojoExecutionException(Messages.ListAllRulesMojo_jsparrowAlreadyRunning);
				}

				serviceInstance.startStandaloneBundle(log);
			}
		} catch (BundleException | InterruptedException e1) {
			log.debug(e1.getMessage(), e1);
			log.error(e1.getMessage());
		}
	}
}
