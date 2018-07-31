package eu.jsparrow.maven.adapter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

public class StandaloneLoaderTest {

	private StandaloneLoader standaloneLoader;
	private MavenProject project;
	private BundleStarter bundleStarter;

	@Before
	public void setUp() {
		project = mock(MavenProject.class);
		bundleStarter = mock(BundleStarter.class);
		standaloneLoader = new StandaloneLoader(project, bundleStarter);
	}

	@Test
	public void loadStandalone_refactoringMode() throws Exception {
		Map<String, String> configuration = new HashMap<>();
		MavenAdapter mavenAdapter = mock(MavenAdapter.class);
		DependencyManager dependencyManager = mock(DependencyManager.class);
		when(mavenAdapter.getConfiguration()).thenReturn(configuration);

		standaloneLoader.loadStandalone(mavenAdapter, dependencyManager);

		verify(bundleStarter).runStandalone(configuration);
	}

	@Test
	public void loadStandalone_listMode() throws Exception {
		Map<String, String> configuration = new HashMap<>();
		MavenAdapter mavenAdapter = mock(MavenAdapter.class);
		when(mavenAdapter.getConfiguration()).thenReturn(configuration);

		standaloneLoader.loadStandalone(mavenAdapter);

		verify(bundleStarter).runStandalone(configuration);
	}
}
