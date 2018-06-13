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
import eu.jsparrow.maven.adapter.StandaloneLoader;
import eu.jsparrow.maven.adapter.WorkingDirectory;
import eu.jsparrow.maven.enums.StandaloneMode;

/**
 * Prints all rules with their name and ID in a table.
 * 
 * @author Matthias Webhofer
 * @since 2.3.0
 */
@Mojo(name = "list-rules-short", aggregator = true)
public class ListAllRulesShortMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		Log log = getLog();

		String mode = StandaloneMode.LIST_RULES_SHORT.name();
		MavenParameters parameters = new MavenParameters(mode);
		MavenAdapter mavenAdapter = new MavenAdapter(project, log);
		BundleStarter bundleStarter = new BundleStarter(log);
		StandaloneLoader loader = new StandaloneLoader(project, bundleStarter);
		try {
			WorkingDirectory workingDir = mavenAdapter.setUpConfiguration(parameters);
			addShutdownHook(bundleStarter, workingDir);
			loader.loadStandalone(mavenAdapter);
		} catch (BundleException | InterruptedException e1) {
			log.debug(e1.getMessage(), e1);
			log.error(e1.getMessage());
		}
	}
	
	private void addShutdownHook(BundleStarter starter, WorkingDirectory workingDirectory) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			starter.shutdownFramework();
			workingDirectory.cleanUp();
		}));
	}
}
