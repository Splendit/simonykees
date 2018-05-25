package eu.jsparrow.maven.adapter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class MavenAdapterTest {

	private MavenProject project;
	private Log log;

	private File workingDirectory;

	private MavenAdapter mavenAdapter;
	private Path path;

	@Before
	public void setUp() {
		project = mock(MavenProject.class);
		log = mock(Log.class);
		workingDirectory = mock(File.class);
		path = mock(Path.class);

		mavenAdapter = new TestableMavenAdapter(project, log);
	}

	@Test
	public void prepareConfiguration_additionalConfigurationNotNull() throws Exception {
		final MavenParameters config = mock(MavenParameters.class);
		when(config.getUseDefaultConfig()).thenReturn(Optional.empty());
		when(config.getMode()).thenReturn(""); //$NON-NLS-1$
		when(config.getLicense()).thenReturn(""); //$NON-NLS-1$
		when(config.getUrl()).thenReturn(""); //$NON-NLS-1$
		when(config.getProfile()).thenReturn(Optional.empty());
		when(config.getRuleId()).thenReturn(Optional.empty());

		mavenAdapter.addInitialConfiguration(config); //$NON-NLS-1$

		verify(config).getUseDefaultConfig();
		verify(config).getMode();
		verify(config).getLicense();
		verify(config).getUrl();
		verify(config).getProfile();
	}

	@Test(expected = InterruptedException.class)
	public void prepareWorkingDirectory_directoryDoesNotExistAndMkdirsNotWorking() throws Exception {
		when(workingDirectory.exists()).thenReturn(false);
		when(workingDirectory.mkdirs()).thenReturn(false);

		mavenAdapter.prepareWorkingDirectory();

		assertTrue(false);
	}

	@Test
	public void prepareWorkingDirectory_directoryDoesNotExistAndMkdirsIsWorking() throws Exception {

		String absolutePath = "somePath";

		when(workingDirectory.exists()).thenReturn(false);
		when(workingDirectory.mkdirs()).thenReturn(true);
		when(workingDirectory.getAbsolutePath()).thenReturn(absolutePath);

		mavenAdapter.prepareWorkingDirectory();

		verify(workingDirectory).getAbsolutePath();
		assertTrue(mavenAdapter.getConfiguration()
			.getOrDefault("osgi.instance.area", "asdf") //$NON-NLS-1$ //$NON-NLS-2$
			.equals(absolutePath));
	}

	@Test
	public void findProjectIdentifier_groupAndArtifactId() {
		String expectedProjectId = "group.id.artifact.id";
		MavenProject mavenProject = mock(MavenProject.class);

		when(mavenProject.getGroupId()).thenReturn("group.id");
		when(mavenProject.getArtifactId()).thenReturn("artifact.id");

		String actualValue = mavenAdapter.findProjectIdentifier(mavenProject);
		assertTrue(expectedProjectId.equals(actualValue));

	}

	@Test
	public void isAggregateProject_hasPomPckage() {
		MavenProject project = mock(MavenProject.class);
		when(project.getPackaging()).thenReturn("pom");
		assertTrue(mavenAdapter.isAggregateProject(project));
	}

	@Test
	public void isAggregateProject_hasListOfModules() {
		MavenProject project = mock(MavenProject.class);
		when(project.getPackaging()).thenReturn("");
		when(project.getModules()).thenReturn(Collections.singletonList("module"));
		assertTrue(mavenAdapter.isAggregateProject(project));
	}

	@Test
	public void isAggregateProject_shouldReturnFalse_jarPackagingNoModules() {
		MavenProject project = mock(MavenProject.class);
		when(project.getPackaging()).thenReturn("jar");
		when(project.getModules()).thenReturn(Collections.emptyList());
		assertFalse(mavenAdapter.isAggregateProject(project));
	}

	@Test
	public void joinWithComma_emptyLeftSide() {
		String expected = "right";
		String actual = mavenAdapter.joinWithComma("", expected);
		assertTrue(expected.equals(actual));
	}

	@Test
	public void joinWithComma_shouldReturnCommaConcatenated() {
		String expected = "project.one.id,project.two.id";
		String actual = mavenAdapter.joinWithComma("project.one.id", "project.two.id");
		assertTrue(expected.equals(actual));
	}

	class TestableMavenAdapter extends MavenAdapter {

		public TestableMavenAdapter(MavenProject project, Log log) {
			super(project, log);
		}

		protected File createWorkingDirectory() {
			return workingDirectory;
		}

		protected Path joinPaths(File parent, File child) {
			return path;
		}

		protected void setSystemProperty(String key, String value) {

		}

	}
}
