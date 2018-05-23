package eu.jsparrow.maven.adapter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleException;

public class StandaloneAdapterTest {

	private StandaloneAdapter standaloneAdapter;
	private EmbeddedMaven embeddedMaven;
	private DependencyManager dependencyManager;
	private MavenAdapter mavenAdapter;
	private BundleStarter bundleStarter;

	@Before
	public void setUp() {
		standaloneAdapter = new TestableStandaloneAdapter();
		embeddedMaven = mock(EmbeddedMaven.class);
		dependencyManager = mock(DependencyManager.class);
		mavenAdapter = mock(MavenAdapter.class);
		bundleStarter = mock(BundleStarter.class);
	}

	@Test
	public void lazyLoadMavenAdapter_loadAdapter() throws Exception {

		MavenParameters configuration = mock(MavenParameters.class);
		MavenProject project = mock(MavenProject.class);
		Log log = mock(Log.class);
		File file = mock(File.class);

		when(configuration.getLog()).thenReturn(log);
		when(configuration.getProject()).thenReturn(project);
		when(configuration.getDefaultYamlFile()).thenReturn(Optional.of(file));
		when(configuration.getMavenSession()).thenReturn(Optional.empty());
		when(mavenAdapter.isJsparrowStarted(project)).thenReturn(false);

		boolean expected = standaloneAdapter.lazyLoadMavenAdapter(configuration);

		assertTrue(expected);
	}

	@Test
	public void lazyLoadMavenAdapter_adapterInstanceAlreadyCreated() throws Exception {

		MavenParameters configuration = mock(MavenParameters.class);
		MavenProject project = mock(MavenProject.class);
		Log log = mock(Log.class);

		when(configuration.getLog()).thenReturn(log);
		when(mavenAdapter.isJsparrowStarted(project)).thenReturn(false);
		standaloneAdapter.setState(mavenAdapter, embeddedMaven, dependencyManager);
		boolean expected = standaloneAdapter.lazyLoadMavenAdapter(configuration);

		assertTrue(expected);
	}

	@Test
	public void lazyLoadMavenAdapter_jsparrowAlreadyRunning() throws Exception {

		MavenParameters configuration = mock(MavenParameters.class);
		MavenProject project = mock(MavenProject.class);
		Log log = mock(Log.class);
		File file = mock(File.class);

		when(configuration.getLog()).thenReturn(log);
		when(configuration.getProject()).thenReturn(project);
		when(configuration.getDefaultYamlFile()).thenReturn(Optional.of(file));
		when(mavenAdapter.isJsparrowStarted(project)).thenReturn(true);

		boolean expected = standaloneAdapter.lazyLoadMavenAdapter(configuration);

		assertFalse(expected);
	}

	@Test
	public void addProjectConfiguration_noStateSet()
			throws MojoExecutionException, BundleException, InterruptedException {

		File configFile = mock(File.class);
		MavenProject project = mock(MavenProject.class);
		Log log = mock(Log.class);

		standaloneAdapter.addProjectConfiguration(project, log, configFile);

		verify(mavenAdapter, never()).addProjectConfiguration(project, configFile);
	}

	@Test
	public void addProjectConfiguration() throws MojoExecutionException, BundleException, InterruptedException {
		standaloneAdapter.setState(mavenAdapter, embeddedMaven, dependencyManager);
		File configFile = mock(File.class);
		MavenProject project = mock(MavenProject.class);
		Log log = mock(Log.class);

		when(embeddedMaven.getMavenHome()).thenReturn("maven-home"); //$NON-NLS-1$
		when(mavenAdapter.findProjectIdentifier(project)).thenReturn("projectId"); //$NON-NLS-1$
		standaloneAdapter.addProjectConfiguration(project, log, configFile);
		
		verify(mavenAdapter).addProjectConfiguration(project, configFile);
	}

	@Test
	public void startStandaloneBundle_missingState()
			throws MojoExecutionException, BundleException, InterruptedException {
		Log log = mock(Log.class);
		@SuppressWarnings("unchecked")
		Map<String, String> map = mock(Map.class);
		standaloneAdapter.startStandaloneBundle(log);
		
		verify(bundleStarter, never()).runStandalone(map);
	}

	@Test
	public void startStandaloneBundle() throws MojoExecutionException, BundleException, InterruptedException {
		standaloneAdapter.setState(mavenAdapter, embeddedMaven, dependencyManager);

		@SuppressWarnings("unchecked")
		Map<String, String> configuration = mock(Map.class);
		Log log = mock(Log.class);

		when(mavenAdapter.getConfiguration()).thenReturn(configuration);
		standaloneAdapter.startStandaloneBundle(log);

		verify(bundleStarter).runStandalone(configuration);
	}
	
	@Test
	public void copyDependencies() {
		String mavenHome = "maven-home"; //$NON-NLS-1$
		String rootIdentifier = "root-identifier"; //$NON-NLS-1$
		MavenProject rootProject = mock(MavenProject.class);
		Log log = mock(Log.class);
		when(embeddedMaven.getMavenHome()).thenReturn(mavenHome); 
		when(mavenAdapter.findProjectIdentifier(rootProject)).thenReturn(rootIdentifier);
		standaloneAdapter.setState(mavenAdapter, embeddedMaven, dependencyManager);
		
		standaloneAdapter.copyDependencies(rootProject, log);
		
		verify(dependencyManager).extractAndCopyDependencies(rootProject, mavenHome);
	}
	
	@Test
	public void copyDependencies_noStateSet() {
		MavenProject rootProject = mock(MavenProject.class);
		Log log = mock(Log.class);
		
		standaloneAdapter.copyDependencies(rootProject, log);
		
		verify(dependencyManager, never()).extractAndCopyDependencies(any(MavenProject.class), any(String.class));
	}

	class TestableStandaloneAdapter extends StandaloneAdapter {

		@Override
		protected MavenAdapter createMavenAdapterInstance(File file, Log log, MavenProject project) {
			return mavenAdapter;
		}

		protected EmbeddedMaven createEmbeddedMavenInstance(MavenParameters configuration, Log log) {
			return embeddedMaven;
		}

		@Override
		protected BundleStarter createNewBundleStarter(Log log) {
			return bundleStarter;
		}

		protected void addShutDownHook(MavenAdapter mavenAdapterInstance, BundleStarter bundleStarter) {

		}
	}

}
