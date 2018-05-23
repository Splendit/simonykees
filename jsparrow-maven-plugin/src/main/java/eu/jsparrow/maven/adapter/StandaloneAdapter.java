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
	
	private static final String POM_FILE_NAME = "pom.xml"; //$NON-NLS-1$

	private MavenAdapter mavenAdapter;
	private BundleStarter bundleStarter;
	private MavenParameters parameters;
	
	public StandaloneAdapter(MavenParameters parameters) {
		this.parameters = parameters;
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
	public boolean lazyLoadMavenAdapter(MavenProject project, Log log) throws InterruptedException {

		if (mavenAdapter != null) {
			log.warn(Messages.StandaloneAdapter_adapterInstanceAlreadyCreated);
			return true;
		}

		log.debug(Messages.StandaloneAdapter_creatingAdapterInstance);
		Optional<File> defaultYamlFile = parameters.getDefaultYamlFile();
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
		parameters.getMavenSession().ifPresent(mavenAdapter::storeProjects);
		mavenAdapter.lockProjects();
		mavenAdapter.addInitialConfiguration(parameters);
		mavenAdapter.setRootProjectPomPath(project.getBasedir()
				.getAbsolutePath() + File.separator + POM_FILE_NAME);
		
		return true;
	}

	protected MavenAdapter createMavenAdapterInstance(File defaultYamlFile, Log log, MavenProject project) {
		return new MavenAdapter(project, log, defaultYamlFile);
	}

	protected MavenAdapter createMavenAdapterInstance(Log log, MavenProject project) {
		return new MavenAdapter(project, log);
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

	public void copyDependencies(MavenProject parentProject, Log log) {
		DependencyManager dependencyManagerInstance = new DependencyManager(log);
		String mavenHome = parameters.getMavenHome().orElse(""); //$NON-NLS-1$
		EmbeddedMaven embeddedMaven = new EmbeddedMaven(log, mavenHome);
		embeddedMaven.prepareMaven();
		dependencyManagerInstance.extractAndCopyDependencies(parentProject, embeddedMaven.getMavenHome());
	}
	
	private boolean isMavenAdapterInitialized(Log log) {
		if (mavenAdapter == null) {
			log.error("Maven adapter is not created"); //$NON-NLS-1$
			return false;
		}

		return true;
	}
}
