package eu.jsparrow.adapter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import java.io.File;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleException;

public class StandAloneAdapterTest {
	
	private StandaloneAdapter standaloneAdapter;
	private EmbeddedMaven embaddedMaven;
	private DependencyManager dependencyManager;
	private MavenAdapter mavenAdapter;
	private BundleStarter bundleStarter;
	
	@Before
	public void setUp() {
		standaloneAdapter = new TestableStandaloneAdapter();
		embaddedMaven = mock(EmbeddedMaven.class);
		dependencyManager = mock(DependencyManager.class);
		mavenAdapter = mock(MavenAdapter.class);
		bundleStarter = mock(BundleStarter.class);
	}
	
	@Test
	public void lazyLoadMavenAdapter_loadAdapter() throws Exception {
		
		MavenParameters configuration = mock(MavenParameters.class);
		MavenProject project = mock(MavenProject.class);
		Log log = mock(Log.class);
		
		when(configuration.getLog()).thenReturn(log);
		when(configuration.getProject()).thenReturn(project);
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
		standaloneAdapter.setState(mavenAdapter, embaddedMaven, dependencyManager);
		boolean expected = standaloneAdapter.lazyLoadMavenAdapter(configuration);
		
		assertTrue(expected);
	}
	
	@Test
	public void lazyLoadMavenAdapter_jsparrowAlreadyRunning() throws Exception {
		
		MavenParameters configuration = mock(MavenParameters.class);
		MavenProject project = mock(MavenProject.class);
		Log log = mock(Log.class);
		
		when(configuration.getLog()).thenReturn(log);
		when(configuration.getProject()).thenReturn(project);
		when(mavenAdapter.isJsparrowStarted(project)).thenReturn(true);
		
		boolean expected = standaloneAdapter.lazyLoadMavenAdapter(configuration);
		
		assertFalse(expected);
	}
	
	@Test
	public void addProjectConfiguration_noStateSet() throws MojoExecutionException, BundleException, InterruptedException {

		File configFile = mock(File.class);
		MavenProject project = mock(MavenProject.class);
		Log log = mock(Log.class);
		
		standaloneAdapter.addProjectConfiguration(project, log, configFile);
		
		verify(mavenAdapter, never()).addProjectConfiguration(project, configFile);
	}
	
	@Test
	public void addProjectConfiguration_notAllProjectsLoaded() throws MojoExecutionException, BundleException, InterruptedException {
		standaloneAdapter.setState(mavenAdapter, embaddedMaven, dependencyManager);
		File configFile = mock(File.class);
		MavenProject project = mock(MavenProject.class);
		Log log = mock(Log.class);
		
		when(mavenAdapter.allProjectConfigurationLoaded()).thenReturn(false);
		standaloneAdapter.addProjectConfiguration(project, log, configFile);
		
		verify(mavenAdapter, never()).getConfiguration();
	}
	
	@Test
	public void addProjectConfiguration_allProjectsLoaded() throws MojoExecutionException, BundleException, InterruptedException {
		standaloneAdapter.setState(mavenAdapter, embaddedMaven, dependencyManager);
		
		File configFile = mock(File.class);
		MavenProject project = mock(MavenProject.class);
		Log log = mock(Log.class);
		@SuppressWarnings("unchecked")
		Map<String, String> map = mock(Map.class);
		
		when(mavenAdapter.getConfiguration()).thenReturn(map);
		when(mavenAdapter.allProjectConfigurationLoaded()).thenReturn(true);
		
		standaloneAdapter.addProjectConfiguration(project, log, configFile);
		
		verify(bundleStarter).runStandalone(map);
	}
	
	
	class TestableStandaloneAdapter extends StandaloneAdapter {

		@Override
		protected MavenAdapter createMavenAdapterInstance(MavenParameters configuration, Log log, MavenProject project) {
			return mavenAdapter;
		}
		
		protected EmbeddedMaven createEmbeddedMavenInstance(MavenParameters configuration, Log log) {
			return embaddedMaven;
		}
		
		@Override
		protected BundleStarter createNewBundleStarter(Log log) {
			return bundleStarter;
		}
		
		protected void addShutDownHook(MavenAdapter mavenAdapterInstance, BundleStarter bundleStarter) {

		}
	}

}
