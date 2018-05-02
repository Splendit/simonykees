package eu.jsparrow.maven;

import java.io.File;

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
 * This MOJO prints all rules with name and id in a table.
 * 
 * @author Matthias Webhofer
 * @since 2.3.0
 */
@Mojo(name = "license-info", aggregator = true)
public class LicenseInfoMojo extends AbstractMojo {

	/**
	 * Maven project on which plugin goal is executed
	 */
	@Parameter(defaultValue = "${project}", required = true)
	private MavenProject project;

	/**
	 * path to the configuration file. defaults to jsparrow.yml in the current
	 * directory.
	 */
	@Parameter(defaultValue = "jsparrow.yml", property = "configFile")
	private File configFile;

	@Parameter(property = "license")
	private String license;

	@Parameter(property = "url")
	private String url;

	/**
	 * MOJO entry point. Starts equinox with the given configuration
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		Log log = getLog();
		StandaloneAdapter serviceInstance = StandaloneAdapter.getInstance();
		String mode = StandaloneMode.LICENSE_INFO.name();

		try {

			MavenParameters config = new MavenParameters(project, log, configFile, null, mode, license, url);

			boolean adapterLoadad = serviceInstance.lazyLoadMavenAdapter(config);
			if (!adapterLoadad) {
				throw new MojoExecutionException(Messages.Mojo_jSparrowIsAlreadyRunning);
			}

			serviceInstance.startStandaloneBundle(log);
		} catch (BundleException | InterruptedException e1) {
			log.debug(e1.getMessage(), e1);
			log.error(e1.getMessage());
		}
	}
}
