package eu.jsparrow.adapter;

import java.io.File;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.osgi.framework.BundleException;

/**
 * 
 * @author Ardit Ymeri
 * @since 2.5.0
 *
 */
public class AdapterService {

	private MavenAdapter mavenAdapter;
	private EmbeddedMaven embeddedMaven;
	private DependencyManager dependencyManager;

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

	public synchronized boolean isAdapterInitialized() {
		return mavenAdapter != null;
	}

	/**
	 * 
	 * @param configuration
	 * @return
	 * @throws InterruptedException
	 */
	public synchronized boolean lazyLoadMavenAdapter(MavenParameters configuration) throws InterruptedException {
		Log log = configuration.getLog();

		if (mavenAdapter != null) {
			log.debug("Adapter instance is already created..."); //$NON-NLS-1$
			return true;
		}

		log.info("Creating adapter instance..."); //$NON-NLS-1$
		MavenProject project = configuration.getProject();
		mavenAdapter = new MavenAdapter(project, log, configuration.getDefaultYamlFile());
		
		if (mavenAdapter.isJsparrowStarted(project)) {
			mavenAdapter.setJsparrowRunningFlag();
			log.error("jSparrow is already running!"); //$NON-NLS-1$
			return false;
		}
		mavenAdapter.prepareWorkingDirectory();
		mavenAdapter.storeProjects(configuration.getMavenSession());
		mavenAdapter.lockProjects();
		embeddedMaven = new EmbeddedMaven(log, configuration.getMavenHome().orElse("")); //$NON-NLS-1$
		embeddedMaven.prepareMaven(MavenAdapter.calculateJsparrowTempFolderPath());
		mavenAdapter.addInitialConfiguration(configuration, embeddedMaven.getMavenHome());
		dependencyManager = new DependencyManager(log);
		return true;
	}

	protected void setMavenAdapter(MavenAdapter mavenAdapter2) {
		this.mavenAdapter = mavenAdapter2;

	}

	/**
	 * 
	 * @param project
	 * @param log
	 * @param configFile
	 * @throws MojoExecutionException
	 * @throws BundleException
	 * @throws InterruptedException
	 */
	public void addProjectConfiguration(MavenProject project, Log log, File configFile)
			throws MojoExecutionException, BundleException, InterruptedException {
		if (mavenAdapter == null) {
			log.error("Maven adapter is not created"); //$NON-NLS-1$
			return;
		}
		
		mavenAdapter.addProjectConfiguration(project, configFile);
		dependencyManager.extractAndCopyDependencies(project, embeddedMaven.getMavenHome(), mavenAdapter.findProjectIdentifier(project));
		if (mavenAdapter.allProjectConfigurationLoaded()) {
			log.info("All projects are loaded ... "); //$NON-NLS-1$

			Map<String, String> bundleConfiguration = mavenAdapter.getConfiguration();

			BundleStarter bundleStarter = new BundleStarter(log);
			Runtime.getRuntime()
				.addShutdownHook(bundleStarter.createShutdownHook(mavenAdapter));
			bundleStarter.runStandalone(bundleConfiguration);
		}
	}

	public MavenAdapter getMavenAdapterInstance() {
		return this.mavenAdapter;
	}
}
