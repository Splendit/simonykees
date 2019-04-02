package eu.jsparrow.maven.mojo;

import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.osgi.framework.BundleException;

import eu.jsparrow.maven.adapter.BundleStarter;
import eu.jsparrow.maven.adapter.MavenAdapter;
import eu.jsparrow.maven.adapter.MavenParameters;
import eu.jsparrow.maven.adapter.WorkingDirectory;
import eu.jsparrow.maven.enums.StandaloneMode;
import eu.jsparrow.maven.i18n.Messages;
import eu.jsparrow.maven.util.JavaVersion;
import eu.jsparrow.maven.util.ProxyUtil;

/**
 * Check validity of the given license.
 * 
 * @author Matthias Webhofer
 * @since 2.3.0
 */
@Mojo(name = "license-info", aggregator = true)
public class LicenseInfoMojo extends AbstractMojo {

	@Parameter(defaultValue = "${session}", readonly = true)
	private MavenSession mavenSession;	

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	/**
	 * The license key to validate.
	 */
	@Parameter(property = "license")
	private String license;

	/**
	 * The URL to the license server to use.
	 */
	@Parameter(property = "url")
	private String url;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Log log = getLog();

		// With version 1.0.0 of jSparrow Maven Plugin, only JDK 8 is supported.
		if (!JavaVersion.isJava8or11()) {
			log.warn(Messages.RefactorMojo_supportJDK8and11);
			throw new MojoExecutionException(Messages.RefactorMojo_supportJDK8and11);
		}

		String mode = StandaloneMode.LICENSE_INFO.name();
		MavenParameters parameters = new MavenParameters(mode, license, url);
		MavenAdapter mavenAdapter = new MavenAdapter(project, log);
		BundleStarter starter = new BundleStarter(log);
		List<Proxy> proxies = ProxyUtil.getHttpProxies(mavenSession);
		try {
			WorkingDirectory workingDir = mavenAdapter.setUpConfiguration(parameters, proxies);
			addShutdownHook(starter, workingDir);
			starter.runStandalone(mavenAdapter.getConfiguration());
		} catch (BundleException | InterruptedException e1) {
			log.debug(e1.getMessage(), e1);
			log.error(e1.getMessage());
		}
	}

	private void addShutdownHook(BundleStarter starter, WorkingDirectory workingDirectory) {
		Runtime.getRuntime()
			.addShutdownHook(new Thread(() -> {
				starter.shutdownFramework();
				workingDirectory.cleanUp();
			}));
	}
}
