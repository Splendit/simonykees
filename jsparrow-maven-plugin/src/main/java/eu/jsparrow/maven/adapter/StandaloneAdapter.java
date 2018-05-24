package eu.jsparrow.maven.adapter;

import java.io.File;
import java.util.List;
import java.util.Map;

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

	private BundleStarter bundleStarter;

	public void loadStandalone(MavenProject root, MavenParameters parameters, List<MavenProject> projects,
			File defaultYamlFile, Log log, String mavenHome)
			throws InterruptedException, MojoExecutionException, BundleException {
		
		MavenAdapter mavenAdapter = createMavenAdapterInstance(defaultYamlFile, log, root);
		if (mavenAdapter.isJsparrowStarted(root)) {
			mavenAdapter.setJsparrowRunningFlag();
			String projectId = mavenAdapter.findProjectIdentifier(root);
			log.error(NLS.bind(Messages.StandaloneAdapter_jSparrowAlreadyRunning, projectId));
			throw new MojoExecutionException(Messages.Mojo_jSparrowIsAlreadyRunning);
		}
		addShutDownHook(mavenAdapter);
		mavenAdapter.prepareWorkingDirectory();
		mavenAdapter.addInitialConfiguration(parameters);
		mavenAdapter.setProjectIds(projects);
		mavenAdapter.lockProjects();

		copyDependencies(root, log, mavenHome);

		for (MavenProject mavenProject : projects) {
			mavenAdapter.addProjectConfiguration(mavenProject, defaultYamlFile);
		}

		log.info(Messages.RefactorMojo_allProjectsLoaded);
		startStandaloneBundle(mavenAdapter.getConfiguration(), log);
	}

	public void loadStandalone(MavenProject root, MavenParameters parameters, Log log)
			throws InterruptedException, MojoExecutionException, BundleException {
		MavenAdapter mavenAdapter = createMavenAdapterInstance(log, root);
		addShutDownHook(mavenAdapter);
		mavenAdapter.prepareWorkingDirectory();
		mavenAdapter.addInitialConfiguration(parameters);

		log.info(Messages.RefactorMojo_allProjectsLoaded);
		startStandaloneBundle(mavenAdapter.getConfiguration(), log);
	}

	protected MavenAdapter createMavenAdapterInstance(File defaultYamlFile, Log log, MavenProject project) {
		return new MavenAdapter(project, log, defaultYamlFile);
	}

	protected MavenAdapter createMavenAdapterInstance(Log log, MavenProject project) {
		return new MavenAdapter(project, log);
	}

	/**
	 * Runs the standalone bundle with the configuration stored in the instance
	 * of the current {@link MavenAdapter}.
	 */
	private void startStandaloneBundle(Map<String, String> bundleConfiguration, Log log)
			throws BundleException, MojoExecutionException, InterruptedException {
		bundleStarter = new BundleStarter(log);
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

	public void copyDependencies(MavenProject parentProject, Log log, String mavenHome) {
		DependencyManager dependencyManagerInstance = new DependencyManager(log);
		EmbeddedMaven embeddedMaven = new EmbeddedMaven(log, mavenHome);
		embeddedMaven.prepareMaven();
		dependencyManagerInstance.extractAndCopyDependencies(parentProject, embeddedMaven.getMavenHome());
	}
}
