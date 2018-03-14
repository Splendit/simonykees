package eu.jsparrow.adapter;

import java.io.File;
import java.util.Map;
import java.util.Optional;

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

	public static synchronized Optional<MavenAdapter> lazyLoadMavenAdapter(MavenProject project, String mavenHome2,
			MavenSession mavenSession, Log log, File defaultYamlFile) {
		if (mavenAdapter == null) {
			log.info("Creating adapter instance..."); //$NON-NLS-1$
			mavenAdapter = new MavenAdapter(project, log, defaultYamlFile);
			if (mavenAdapter.isJsparrowStarted(project)) {
				mavenAdapter.setJsparrowRunningFlag();
				return Optional.empty();
			}
			mavenAdapter.storeProjects(mavenSession);
			embeddedMaven = new EmbeddedMaven(log, mavenHome2);
			embeddedMaven.prepareMaven(MavenAdapter.calculateJsparrowTempFolderPath());
			mavenAdapter.addInitialConfiguration(embeddedMaven.getMavenHome());
		}
		return Optional.of(mavenAdapter);
	}

	public static void addProjectConfiguration(MavenProject project, Log log, Map<String, String> config, File configFile)
			throws MojoExecutionException, BundleException, InterruptedException {
		if (mavenAdapter == null) {
			log.error("Maven adapter is not created"); //$NON-NLS-1$
			return;
		}

		mavenAdapter.prepareWorkingDirectory(project);

		mavenAdapter.addProjectConfiguration(project, config, configFile, embeddedMaven.getMavenHome());
		if (mavenAdapter.allProjectConfigurationLoaded()) {
			log.info("All projects are loaded ... "); //$NON-NLS-1$

			Map<String, String> bundleConfiguration = mavenAdapter.getConfiguration();

			BundleStarter bundleStarter = new BundleStarter(log);
			Runtime.getRuntime()
				.addShutdownHook(bundleStarter.createShutdownHook(mavenAdapter));
			bundleStarter.runStandalone(bundleConfiguration);
		}
	}
}
