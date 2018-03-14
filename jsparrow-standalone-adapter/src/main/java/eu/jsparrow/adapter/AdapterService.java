package eu.jsparrow.adapter;

import java.io.File;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.osgi.framework.BundleException;

public class AdapterService {

	private MavenAdapter mavenAdapter;
	private EmbeddedMaven embeddedMaven;

	private AdapterService() {
		/*
		 * Hiding the public constructor
		 */
	}

	private static AdapterService adapterService;

	public static synchronized AdapterService getInstance() {
		if (adapterService == null) {
			adapterService = new AdapterService();
		}
		return adapterService;
	}

	public synchronized boolean lazyLoadMavenAdapter(MavenProject project, Log log, String mavenHome2,
			MavenSession mavenSession, File defaultYamlFile, String profile, String mode, boolean useDefaultConfig) {

		if (mavenAdapter != null) {
			log.debug("Adapter instance is already created..."); //$NON-NLS-1$
			return true;
		}

		log.info("Creating adapter instance..."); //$NON-NLS-1$
		mavenAdapter = new MavenAdapter(project, log, defaultYamlFile);
		if (mavenAdapter.isJsparrowStarted(project)) {
			mavenAdapter.setJsparrowRunningFlag();
			log.error("jSparrow is already running!"); //$NON-NLS-1$
			return false;
		}
		mavenAdapter.storeProjects(mavenSession);
		embeddedMaven = new EmbeddedMaven(log, mavenHome2);
		embeddedMaven.prepareMaven(MavenAdapter.calculateJsparrowTempFolderPath());
		mavenAdapter.addInitialConfiguration(embeddedMaven.getMavenHome(), profile, mode, useDefaultConfig);

		return true;

	}

	public void addProjectConfiguration(MavenProject project, Log log, File configFile)
			throws MojoExecutionException, BundleException, InterruptedException {
		if (mavenAdapter == null) {
			log.error("Maven adapter is not created"); //$NON-NLS-1$
			return;
		}

		mavenAdapter.prepareWorkingDirectory(project);

		mavenAdapter.addProjectConfiguration(project, configFile, embeddedMaven.getMavenHome());
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
