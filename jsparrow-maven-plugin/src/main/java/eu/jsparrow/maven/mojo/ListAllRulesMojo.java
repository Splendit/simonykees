package eu.jsparrow.maven.mojo;

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
import eu.jsparrow.maven.adapter.WorkingDirectory;
import eu.jsparrow.maven.enums.StandaloneMode;
import eu.jsparrow.maven.i18n.Messages;

/**
 * Lists all rules with ID, name and description.
 * 
 * @author Matthias Webhofer
 * @since 2.3.0
 */
@Mojo(name = "list-rules", aggregator = true)
public class ListAllRulesMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	/**
	 * Specify a rule by ID to receive detailed information on.
	 */
	@Parameter(property = "rules")
	private String rules;
	
	private static final String JAVA_VERSION_PROPERTY_CONSTANT = "java.version"; //$NON-NLS-1$
	private static final String JAVA_VERSION_1_8 = "1.8"; //$NON-NLS-1$
	private static final String JAVA_VERSION_11 = "11"; //$NON-NLS-1$

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Log log = getLog();
		
		String javaVersion = System.getProperty(JAVA_VERSION_PROPERTY_CONSTANT);
		if (!javaVersion.startsWith(JAVA_VERSION_1_8) && !javaVersion.startsWith(JAVA_VERSION_11)) {
			log.warn(Messages.RefactorMojo_supportJDK8and11);
			throw new MojoExecutionException(Messages.RefactorMojo_supportJDK8and11);
		}
		
		String mode = StandaloneMode.LIST_RULES.name();
		MavenParameters parameters = new MavenParameters(mode);
		parameters.setRuleId(rules);
		MavenAdapter mavenAdapter = new MavenAdapter(project, log);
		BundleStarter starter = new BundleStarter(log);
		try {
			WorkingDirectory workingDir = mavenAdapter.setUpConfiguration(parameters);
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
