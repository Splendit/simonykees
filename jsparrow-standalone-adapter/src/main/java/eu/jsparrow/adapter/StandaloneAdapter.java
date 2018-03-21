package eu.jsparrow.adapter;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.osgi.framework.BundleException;

import eu.jsparrow.adapter.i18n.Messages;

/**
 * An adapter between the maven plugin and the standalone bundle. 
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
	 * Creates instances of {@link MavenAdapter}, {@link EmbeddedMaven} and {@link DependencyManager} and
	 * sets the state of this object. 
	 * 
	 * @param configuration expected maven parameters.
	 * @return {@code true} if initialization of the state finishes successfully, or {@code false} otherwise. 
	 * @throws InterruptedException
	 */
	public synchronized boolean lazyLoadMavenAdapter(MavenParameters configuration) throws InterruptedException {
		Log log = configuration.getLog();

		MavenAdapter adapterInstance = getMavenAdapterInstance();

		if (adapterInstance != null) {
			log.warn(Messages.StandaloneAdapter_adapterInstanceAlreadyCreated);
			return true;
		}

		log.debug(Messages.StandaloneAdapter_creatingAdapterInstance);
		MavenProject project = configuration.getProject();
		Optional<File> defaultYamlFile = configuration.getDefaultYamlFile();
		if(defaultYamlFile.isPresent()) {			
			adapterInstance = createMavenAdapterInstance(defaultYamlFile.get(), log, project);
		} else {
			adapterInstance = createMavenAdapterInstance(log, project);
		}

		if (adapterInstance.isJsparrowStarted(project)) {
			adapterInstance.setJsparrowRunningFlag();
			log.error(Messages.StandaloneAdapter_jSparrowAlreadyRunning);
			return false;
		}
		adapterInstance.prepareWorkingDirectory();
		configuration.getMavenSession().ifPresent(adapterInstance::storeProjects);
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
		return new EmbeddedMaven(log, configuration.getMavenHome()
			.orElse("")); //$NON-NLS-1$
	}

	protected MavenAdapter createMavenAdapterInstance(File defaultYamlFile, Log log, MavenProject project) {
		return new MavenAdapter(project, log, defaultYamlFile);
	}
	
	protected MavenAdapter createMavenAdapterInstance(Log log, MavenProject project) {
		return new MavenAdapter(project, log);
	}

	protected void setMavenAdapter(MavenAdapter mavenAdapter2) {
		this.mavenAdapter = mavenAdapter2;
	}

	/**
	 * Adds the configuration related to the given project in the session. 
	 * 
	 * @param project project whose configuration has to be added.
	 * @param log maven log
	 * @param configFile the default yaml configuration file
	 */
	public void addProjectConfiguration(MavenProject project, Log log, File configFile) {
		MavenAdapter mavenAdapterInstance = getMavenAdapterInstance();
		if (mavenAdapterInstance == null) {
			log.error(Messages.StandaloneAdapter_mavenAdapterInstanceNotCreated);
			return;
		}

		mavenAdapterInstance.addProjectConfiguration(project, configFile);
		DependencyManager dependencyManagerInstance = getDependencyManagerInstance();
		EmbeddedMaven embeddedMavenInstance = getEmbeddedMavenInstance();
		dependencyManagerInstance.extractAndCopyDependencies(project, embeddedMavenInstance.getMavenHome(),
				mavenAdapterInstance.findProjectIdentifier(project));
	}

	/**
	 * Runs the standalone bundle with the configuration stored in the instance 
	 * of the current {@link MavenAdapter}. 
	 */
	public void startStandaloneBundle(Log log) throws BundleException, MojoExecutionException, InterruptedException {
		MavenAdapter mavenAdapterInstance = getMavenAdapterInstance();
		if (mavenAdapterInstance == null) {
			log.error(Messages.StandaloneAdapter_mavenAdapterInstanceNotCreated);
			return;
		}
		Map<String, String> bundleConfiguration = mavenAdapterInstance.getConfiguration();
		BundleStarter bundleStarter = createNewBundleStarter(log);
		addShutDownHook(mavenAdapterInstance, bundleStarter);
		
		bundleStarter.runStandalone(bundleConfiguration);
	}

	/**
	 * @return if the configuration of all projects in the session are loaded; 
	 */
	public boolean allProjectsLoaded() {

		MavenAdapter adapterInstance = getMavenAdapterInstance();

		if (adapterInstance == null) {
			return false;
		}

		return adapterInstance.allProjectConfigurationLoaded();
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
