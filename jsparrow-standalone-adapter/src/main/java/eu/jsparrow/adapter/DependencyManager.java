package eu.jsparrow.adapter;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import eu.jsparrow.adapter.i18n.Messages;

/**
 * Contains functionalities for running {@code mvn dependency:copy-dependencies}
 * plugin.
 * 
 * @author Andreja Sambolec, Matthias Webhofer, Ardit Ymeri
 * @since 2.5.0
 *
 */
public class DependencyManager {

	protected static final String OUTPUT_DIRECTORY_OPTION_KEY = "outputDirectory"; //$NON-NLS-1$
	private static final String OUTPUT_DIRECTORY_PREFIX = "deps"; //$NON-NLS-1$
	private static final String DEPENDENCY_PLUGIN_ID = "dependency"; //$NON-NLS-1$
	private static final String COPY_DEPENDENCIES_GOAL = "copy-dependencies"; //$NON-NLS-1$
	private static final String POM_FILE_NAME = "pom.xml"; //$NON-NLS-1$

	private Log log;

	public DependencyManager(Log log) {
		this.log = log;
	}

	/**
	 * Executes maven goal copy-dependencies on the project to copy all resolved
	 * needed dependencies to the temp folder for use from bundles.
	 */
	public void extractAndCopyDependencies(MavenProject project, String mavenHome) {
		log.debug(Messages.DependencyManager_extractAndCopyDependencies);

		final InvocationRequest request = new DefaultInvocationRequest();
		final Properties props = new Properties();
		prepareDefaultRequest(project, request, props);
		final Invoker invoker = new DefaultInvoker();
		invokeMaven(invoker, request, mavenHome);
	}

	/**
	 * Sets the pom file, goals and properties to the provided
	 * {@link InvocationRequest} for running:
	 * {@code mvn depedency:copy-dependencies outputDirectory=[user.dir]/deps.[suffix]}.
	 * 
	 * @see <a href=
	 *      "https://maven.apache.org/plugins/maven-dependency-plugin/copy-dependencies-mojo.html">
	 *      Apache Maven Dependency Plugin </a>.
	 * 
	 * 
	 * @param project
	 *            the project to take the pom file from.
	 * @param request
	 *            the {@link InvocationRequest} to be updated.
	 * @param props
	 *            properties to be added to the invocation request.
	 */
	protected void prepareDefaultRequest(MavenProject project, InvocationRequest request, Properties props) {
		File projectBaseDir = project.getBasedir();
		String projectPath = projectBaseDir.getAbsolutePath();
		request.setPomFile(new File(projectPath + File.separator + POM_FILE_NAME));
		String cleanPackageCopyDependencies = String.format("clean package %s:%s ", DEPENDENCY_PLUGIN_ID, //$NON-NLS-1$
				COPY_DEPENDENCIES_GOAL);
		List<String> goals = Collections.singletonList(cleanPackageCopyDependencies);
		request.setGoals(goals);
		String outputDirectoryPath = System.getProperty(MavenAdapter.USER_DIR) + File.separator
				+ OUTPUT_DIRECTORY_PREFIX + File.separator + "\\${project.artifactId}"; //$NON-NLS-1$
		props.setProperty(OUTPUT_DIRECTORY_OPTION_KEY, outputDirectoryPath);
		props.setProperty("skipTests", "true"); //$NON-NLS-1$ , //$NON-NLS-2$
		request.setProperties(props);
	}

	protected void invokeMaven(Invoker invoker, InvocationRequest request, String preparedMavenHome) {
		invoker.setMavenHome(new File(preparedMavenHome));

		try {
			invoker.execute(request);
		} catch (final MavenInvocationException e) {
			log.debug(e.getMessage(), e);
			log.error(e.getMessage());
		}
	}

}
