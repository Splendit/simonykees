package eu.jsparrow.adapter;

import java.io.File;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.osgi.framework.BundleException;

public class AdapterService {

	private static MavenAdapter mavenAdapter;
	private static EmbeddedMaven embaddedMaven;

	public static synchronized MavenAdapter lazyLoadMavenAdapter(MavenProject project, String mavenHome,
			MavenSession mavenSession, Log log) {
		if (mavenAdapter == null) {
			log.info("Creating adapter instance..."); //$NON-NLS-1$
			embaddedMaven = new EmbeddedMaven(log, mavenHome);
			mavenAdapter = new MavenAdapter(project, log);

			mavenAdapter.storeProjects(mavenSession);
			mavenAdapter.addInitialConfiguration(embaddedMaven.getMavenHome());
		}
		return mavenAdapter;
	}

	public static void addProjectConfiguration(MavenProject project, Log log)
			throws MojoExecutionException, BundleException, InterruptedException {
		if (mavenAdapter == null) {
			log.error("Maven adapter is not created"); //$NON-NLS-1$
			return;
		}

		mavenAdapter.addProjectConfiguration(project);
		if (mavenAdapter.allProjectConfigurationLoaded()) {
			log.info("All projects are loaded ... "); //$NON-NLS-1$
			File directory = mavenAdapter.createWorkingDirectory();
			embaddedMaven.prepareMaven(directory);

			Map<String, String> bundleConfiguration = mavenAdapter.getConfiguration();

			BundleStarter bundleStarter = new BundleStarter();
			Runtime.getRuntime()
				.addShutdownHook(bundleStarter.createShutdownHook(mavenAdapter));
			mavenAdapter.prepareWorkingDirectory();
			mavenAdapter.extractAndCopyDependencies(embaddedMaven.getMavenHome());
			bundleStarter.runStandalone(bundleConfiguration);
		}
	}
}
