package eu.jsparrow.maven.adapter;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleException;

import eu.jsparrow.maven.i18n.Messages;

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
	private BundleStarter bundleStarter;

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
	 * Creates instances of {@link MavenAdapter}, {@link EmbeddedMaven} and
	 * {@link DependencyManager} and sets the state of this object.
	 * 
	 * @param configuration
	 *            expected maven parameters.
	 * @return {@code true} if initialization of the state finishes
	 *         successfully, or {@code false} otherwise.
	 * @throws InterruptedException
	 */
	public synchronized boolean lazyLoadMavenAdapter(MavenParameters configuration) throws InterruptedException {
		Log log = configuration.getLog();

		if (mavenAdapter != null) {
			log.warn(Messages.StandaloneAdapter_adapterInstanceAlreadyCreated);
			return true;
		}

		log.debug(Messages.StandaloneAdapter_creatingAdapterInstance);
		MavenProject project = configuration.getProject();
		Optional<File> defaultYamlFile = configuration.getDefaultYamlFile();
		if (defaultYamlFile.isPresent()) {
			mavenAdapter = createMavenAdapterInstance(defaultYamlFile.get(), log, project);
		} else {
			mavenAdapter = createMavenAdapterInstance(log, project);
		}

		if (mavenAdapter.isJsparrowStarted(project)) {
			mavenAdapter.setJsparrowRunningFlag();
			String projectId = mavenAdapter.findProjectIdentifier(project);
			log.error(NLS.bind(Messages.StandaloneAdapter_jSparrowAlreadyRunning, projectId));
			return false;
		}

		addShutDownHook(mavenAdapter);

		mavenAdapter.prepareWorkingDirectory();
		configuration.getMavenSession()
			.ifPresent(mavenAdapter::storeProjects);
		mavenAdapter.lockProjects();

		EmbeddedMaven embeddedMavenInstance = createEmbeddedMavenInstance(configuration, log);
		embeddedMavenInstance.prepareMaven();
		mavenAdapter.addInitialConfiguration(configuration, embeddedMavenInstance.getMavenHome());
		DependencyManager dependencyManagerInstance = createDependencyManagerInstance(log);

		setState(mavenAdapter, embeddedMavenInstance, dependencyManagerInstance);
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
	 * @param project
	 *            project whose configuration has to be added.
	 * @param log
	 *            maven log
	 * @param configFile
	 *            the default yaml configuration file
	 */
	public void addProjectConfiguration(MavenProject project, Log log, File configFile) {
		if (!isMavenAdapterInitialized(log)) {
			return;
		}

		mavenAdapter.addProjectConfiguration(project, configFile);
	}

	/**
	 * Runs the standalone bundle with the configuration stored in the instance
	 * of the current {@link MavenAdapter}.
	 */
	public void startStandaloneBundle(Log log) throws BundleException, MojoExecutionException, InterruptedException {
		if (!isMavenAdapterInitialized(log)) {
			return;
		}
		Map<String, String> bundleConfiguration = mavenAdapter.getConfiguration();
		bundleStarter = createNewBundleStarter(log);

		bundleStarter.runStandalone(bundleConfiguration);
	}

	protected void addShutDownHook(MavenAdapter mavenAdapterInstance) {
		Runtime.getRuntime()
			.addShutdownHook(new Thread(() -> {
				if (bundleStarter != null) {
					bundleStarter.shutdown(mavenAdapterInstance);
				} else if (mavenAdapterInstance != null && !mavenAdapterInstance.isJsparrowRunningFlag()) {
					mavenAdapterInstance.cleanUp();
				}
			}));
	}

	protected BundleStarter createNewBundleStarter(Log log) {
		return new BundleStarter(log);
	}

	protected DependencyManager getDependencyManagerInstance() {
		return dependencyManager;
	}

	protected EmbeddedMaven getEmbeddedMavenInstance() {
		return embeddedMaven;
	}

	public void copyDependencies(MavenProject parentProject, Log log) {

		DependencyManager dependencyManagerInstance = getDependencyManagerInstance();
		if (dependencyManagerInstance == null) {
			log.error("Maven dependency manager is not created"); //$NON-NLS-1$
			return;
		}
		EmbeddedMaven embeddedMavenInstance = getEmbeddedMavenInstance();
		dependencyManagerInstance.extractAndCopyDependencies(parentProject, embeddedMavenInstance.getMavenHome());
	}

	public void setRootProjectPomPath(String rootProjectPomPath, Log log) {
		if (!isMavenAdapterInitialized(log)) {
			return;
		}

		mavenAdapter.setRootProjectPomPath(rootProjectPomPath);
	}
	
	private boolean isMavenAdapterInitialized(Log log) {
		if (mavenAdapter == null) {
			log.error("Maven adapter is not created"); //$NON-NLS-1$
			return false;
		}

		return true;
	}
}
