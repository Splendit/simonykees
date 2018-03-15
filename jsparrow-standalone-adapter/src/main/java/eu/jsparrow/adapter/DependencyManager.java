package eu.jsparrow.adapter;

import java.io.File;
import java.util.Collections;
import java.util.Properties;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

public class DependencyManager {
	
	protected static final String OUTPUT_DIRECTORY_CONSTANT = "outputDirectory"; //$NON-NLS-1$
	private static final String DEPENDENCIES_FOLDER_CONSTANT = "deps"; //$NON-NLS-1$
	
	private Log log;
	
	public DependencyManager(Log log) {
		this.log = log;
	}
	
	/**
	 * Executes maven goal copy-dependencies on the project to copy all resolved
	 * needed dependencies to the temp folder for use from bundles.
	 */
	public void extractAndCopyDependencies(MavenProject project, String mavenHome, String outputDirectorySuffix) {
		log.debug("Extract and copy dependencies");

		final InvocationRequest request = new DefaultInvocationRequest();
		final Properties props = new Properties();
		prepareDefaultRequest(project, request, props, outputDirectorySuffix);
		final Invoker invoker = new DefaultInvoker();
		invokeMaven(invoker, request, mavenHome);
	}

	protected void prepareDefaultRequest(MavenProject project, InvocationRequest request, Properties props, String suffix) {
		File projectBaseDir = project.getBasedir();
		String projectPath = projectBaseDir.getAbsolutePath();
		request.setPomFile(new File(projectPath + File.separator + "pom.xml")); //$NON-NLS-1$
		request.setGoals(Collections.singletonList("dependency:copy-dependencies ")); //$NON-NLS-1$

		props.setProperty(OUTPUT_DIRECTORY_CONSTANT, System.getProperty(MavenAdapter.USER_DIR) + File.separator
				+ DEPENDENCIES_FOLDER_CONSTANT + MavenAdapter.DOT + suffix);
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
