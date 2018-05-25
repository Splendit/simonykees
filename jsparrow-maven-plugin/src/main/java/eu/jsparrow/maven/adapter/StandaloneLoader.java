package eu.jsparrow.maven.adapter;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.osgi.framework.BundleException;

/**
 * An adapter between the maven plugin and the standalone bundle.
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

	public void loadStandalone(MavenAdapter mavenAdapter, DependencyManager dependencyManager)
			throws InterruptedException, MojoExecutionException, BundleException {

		dependencyManager.extractAndCopyDependencies(rootProject);
		loadStandalone(mavenAdapter);
	}

	public void loadStandalone(MavenAdapter mavenAdapter)
			throws InterruptedException, MojoExecutionException, BundleException {

		addShutDownHook(mavenAdapter);
		bundleStarter.runStandalone(mavenAdapter.getConfiguration());
	}

	private void addShutDownHook(MavenAdapter mavenAdapterInstance) {
		Runtime.getRuntime()
			.addShutdownHook(new Thread(() -> {
				if (bundleStarter != null) {
					bundleStarter.shutdown(mavenAdapterInstance);
				} else if (mavenAdapterInstance != null && !mavenAdapterInstance.isJsparrowRunningFlag()) {
					WorkingDirectory workingDir = bundleStarter.getWorkingDirectoryWatcher();
					workingDir.cleanUp();
				}
			}));
	}
}
