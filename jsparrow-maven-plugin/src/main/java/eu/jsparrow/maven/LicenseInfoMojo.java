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
import eu.jsparrow.maven.i18n.Messages;

/**
 * Check validity of the given license.  
 * 
 * @author Matthias Webhofer
 * @since 2.3.0
 */
@Mojo(name = "license-info", aggregator = true)
public class LicenseInfoMojo extends AbstractMojo {

	private static final String JAVA_VERSION_PROPERTY_CONSTANT = "java.version"; //$NON-NLS-1$
	private static final String JAVA_VERSION_1_8 = "1.8"; //$NON-NLS-1$

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
		String javaVersion = System.getProperty(JAVA_VERSION_PROPERTY_CONSTANT);
		if (!javaVersion.startsWith(JAVA_VERSION_1_8)) {
			log.warn(Messages.RefactorMojo_suportJDK8);
			throw new MojoExecutionException(Messages.RefactorMojo_suportJDK8);
		}

		String mode = StandaloneMode.LICENSE_INFO.name();
		MavenParameters parameters = new MavenParameters(mode, license, url);
		MavenAdapter mavenAdapter = new MavenAdapter(project, log);
		BundleStarter starter = new BundleStarter(log);
		StandaloneLoader loader = new StandaloneLoader(project, starter);
		try {
			WorkingDirectory workingDir = mavenAdapter.setUpConfiguration(parameters);
			addShutdownHook(starter, workingDir);
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
