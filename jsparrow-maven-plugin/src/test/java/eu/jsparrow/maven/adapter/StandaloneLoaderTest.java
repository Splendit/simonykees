package eu.jsparrow.maven.adapter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StandaloneLoaderTest {

	private StandaloneLoader standaloneLoader;

	@Mock
	private MavenProject project;

	@Mock
	private BundleStarter bundleStarter;

	@Mock
	private MavenAdapter mavenAdapter;

	@Mock
	private DependencyManager dependencyManager;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() {
		standaloneLoader = new StandaloneLoader(project, bundleStarter);
	}

	@Test
	public void loadStandalone_refactoringMode() throws Exception {
		Map<String, String> configuration = new HashMap<>();
		when(mavenAdapter.getConfiguration()).thenReturn(configuration);
		when(mavenAdapter.isProjectConfigurationAdded()).thenReturn(true);

		standaloneLoader.loadStandalone(mavenAdapter, dependencyManager);

		verify(bundleStarter).runStandalone(configuration);
	}

	@Test
	public void loadStandalone_listMode() throws Exception {
		Map<String, String> configuration = new HashMap<>();
		when(mavenAdapter.getConfiguration()).thenReturn(configuration);
		when(mavenAdapter.isProjectConfigurationAdded()).thenReturn(true);

		standaloneLoader.loadStandalone(mavenAdapter);

		verify(bundleStarter).runStandalone(configuration);
	}

	@Test
	public void loadStandalone_noModulesPresentInMultiMavenProject_shouldThrowException() throws Exception {
		when(mavenAdapter.isProjectConfigurationAdded()).thenReturn(false);

		expectedException.expect(MojoExecutionException.class);

		standaloneLoader.loadStandalone(mavenAdapter);
	}
}
