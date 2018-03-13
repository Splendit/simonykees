package eu.jsparrow.adapter;

import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.osgi.framework.BundleException;

public class AdapterService {

	private static MavenAdapter mavenAdapter;
	private static EmbeddedMaven embeddedMaven;

	private AdapterService() {
		/*
		 * Hiding the public constructor
		 */
	}

	public static synchronized MavenAdapter lazyLoadMavenAdapter(MavenProject project, String mavenHome2,
			MavenSession mavenSession, Log log) {
		if (mavenAdapter == null) {
			log.info("Creating adapter instance..."); //$NON-NLS-1$
			mavenAdapter = new MavenAdapter(project, log);
			mavenAdapter.storeProjects(mavenSession);
			embeddedMaven = new EmbeddedMaven(log, mavenHome2); 
			embeddedMaven.prepareMaven(MavenAdapter.calculateJsparrowTempFolderPath());
			mavenAdapter.addInitialConfiguration(embeddedMaven.getMavenHome());
			
		}
		return mavenAdapter;
	}

	public static void addProjectConfiguration(MavenProject project, Log log, Map<String, String> config)
			throws MojoExecutionException, BundleException, InterruptedException {
		if (mavenAdapter == null) {
			log.error("Maven adapter is not created"); //$NON-NLS-1$
			return;
		}

		mavenAdapter.prepareWorkingDirectory(project);

		mavenAdapter.addProjectConfiguration(project, config);
		if (mavenAdapter.allProjectConfigurationLoaded()) {
			log.info("All projects are loaded ... "); //$NON-NLS-1$

			Map<String, String> bundleConfiguration = mavenAdapter.getConfiguration();

			BundleStarter bundleStarter = new BundleStarter(log);
			Runtime.getRuntime()
				.addShutdownHook(bundleStarter.createShutdownHook(mavenAdapter));
			mavenAdapter.extractAndCopyDependencies(embeddedMaven.getMavenHome());
			bundleStarter.runStandalone(bundleConfiguration);
		}
	}
}
