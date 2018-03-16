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
public class StandaloneAdapter {

	private MavenAdapter mavenAdapter;
	private EmbeddedMaven embeddedMaven;
	private DependencyManager dependencyManager;

	protected StandaloneAdapter() {
		/*
		 * Hiding the public constructor
		 */
	}

	private static StandaloneAdapter standaloneAdapter;

	public static synchronized StandaloneAdapter getInstance() {
		if (standaloneAdapter == null) {
			standaloneAdapter = new StandaloneAdapter();
		}
		return standaloneAdapter;
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
		
		MavenAdapter adapterInstance = getMavenAdapterInstance();

		if (adapterInstance != null) {
			log.debug("Adapter instance is already created..."); //$NON-NLS-1$
			return true;
		}

		log.info("Creating adapter instance..."); //$NON-NLS-1$
		MavenProject project = configuration.getProject();
		adapterInstance = createMavenAdapterInstance(configuration, log, project);
		
		if (adapterInstance.isJsparrowStarted(project)) {
			adapterInstance.setJsparrowRunningFlag();
			log.error("jSparrow is already running!"); //$NON-NLS-1$
			return false;
		}
		adapterInstance.prepareWorkingDirectory();
		adapterInstance.storeProjects(configuration.getMavenSession());
		adapterInstance.lockProjects();
		
		EmbeddedMaven embeddedMavenInstance = createEmbeddedMavenInstance(configuration, log);
		embeddedMavenInstance.prepareMaven();
		adapterInstance.addInitialConfiguration(configuration, embeddedMavenInstance.getMavenHome());
		DependencyManager dependencyManagerInstance = createDependencyManagerInstance(log);
		
		setState(adapterInstance, embeddedMavenInstance, dependencyManagerInstance);
		return true;
	}

	protected void setState(MavenAdapter adapterInstance, EmbeddedMaven embeddedMavenInstance,
			DependencyManager dependencyManagerInstance) {
		this.mavenAdapter = adapterInstance;
		this.embeddedMaven = embeddedMavenInstance;
		this.dependencyManager = dependencyManagerInstance;
		
	}

	protected DependencyManager createDependencyManagerInstance(Log log) {
		return new DependencyManager(log);
	}

	protected EmbeddedMaven createEmbeddedMavenInstance(MavenParameters configuration, Log log) {
		return new EmbeddedMaven(log, configuration.getMavenHome().orElse("")); //$NON-NLS-1$
	}

	protected MavenAdapter createMavenAdapterInstance(MavenParameters configuration, Log log, MavenProject project) {
		return new MavenAdapter(project, log, configuration.getDefaultYamlFile());
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
		MavenAdapter mavenAdapterInstance = getMavenAdapterInstance();
		if (mavenAdapterInstance == null) {
			log.error("Maven adapter is not created"); //$NON-NLS-1$
			return;
		}
		
		mavenAdapterInstance.addProjectConfiguration(project, configFile);
		DependencyManager dependencyManagerInstance = getDependencyManagerInstance();
		EmbeddedMaven embeddedMavenInstance = getEmbeddedMavenInstance();
		dependencyManagerInstance.extractAndCopyDependencies(project, embeddedMavenInstance.getMavenHome(), mavenAdapterInstance.findProjectIdentifier(project));
		if (mavenAdapterInstance.allProjectConfigurationLoaded()) {
			log.info("All projects are loaded ... "); //$NON-NLS-1$

			Map<String, String> bundleConfiguration = mavenAdapterInstance.getConfiguration();

			BundleStarter bundleStarter = createNewBundleStarter(log);
			addShutDownHook(mavenAdapterInstance, bundleStarter);
			bundleStarter.runStandalone(bundleConfiguration);
		}
	}

	protected void addShutDownHook(MavenAdapter mavenAdapterInstance, BundleStarter bundleStarter) {
		Runtime.getRuntime()
			.addShutdownHook(bundleStarter.createShutdownHook(mavenAdapterInstance));
	}

	protected BundleStarter createNewBundleStarter(Log log) {
		return new BundleStarter(log);
	}

	protected DependencyManager getDependencyManagerInstance() {
		return dependencyManager;
	}

	public MavenAdapter getMavenAdapterInstance() {
		return this.mavenAdapter;
	}
	
	protected EmbeddedMaven getEmbeddedMavenInstance() {
		return embeddedMaven;
	}
}
