package eu.jsparrow.maven.adapter;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.osgi.framework.BundleException;

/**
 * A class for starting the standalone bundle based on the configuration
 * provided in the {@link MavenAdapter}
 * 
 * @author Ardit Ymeri
 * @since 2.5.0
 *
 */
public class StandaloneLoader {

	private BundleStarter bundleStarter;
	private MavenProject rootProject;

	public StandaloneLoader(MavenProject rootProject, BundleStarter bundleStarter) {
		this.rootProject = rootProject;
		this.bundleStarter = bundleStarter;
	}

	/**
	 * Copies dependencies and loads the equinox framework for running the
	 * standalone bundle.
	 * 
	 * @param mavenAdapter
	 *            an instance of {@link MavenAdapter} with required
	 *            configuration for staring equinox
	 * @param dependencyManager
	 *            an instance of {@link DependencyManager} for copying
	 *            dependencies
	 * @throws InterruptedException
	 * @throws MojoExecutionException
	 * @throws BundleException
	 */
	public void loadStandalone(MavenAdapter mavenAdapter, DependencyManager dependencyManager)
			throws InterruptedException, MojoExecutionException, BundleException {
		
		dependencyManager.copyDependencies(rootProject);
		loadStandalone(mavenAdapter);
	}

	/**
	 * Loads the equinox framework for running the standalone bundle.
	 * 
	 * @param mavenAdapter
	 *            an instance of {@link MavenAdapter} with required
	 *            configuration for staring equinox
	 * @throws InterruptedException
	 *             if the equinox framework cannot be stopped.
	 * @throws MojoExecutionException
	 *             if the manifest file cannot be found or if the exit message
	 *             when the stopping equinox indicates an error.
	 * @throws BundleException
	 *             if the OSGi bundle cannot be started, installed or stopped.
	 */
	public void loadStandalone(MavenAdapter mavenAdapter)
			throws InterruptedException, MojoExecutionException, BundleException {
		bundleStarter.runStandalone(mavenAdapter.getConfiguration());
	}
}
