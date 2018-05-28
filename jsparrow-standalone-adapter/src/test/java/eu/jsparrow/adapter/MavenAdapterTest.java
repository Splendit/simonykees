package eu.jsparrow.adapter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@SuppressWarnings("nls")
public class MavenAdapterTest {

	private MavenProject project;
	private Log log;
	private File defaultYamlConfig;

	private File workingDirectory;

	private MavenAdapter mavenAdapter;
	private Path path;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void setUp() {
		project = mock(MavenProject.class);
		log = mock(Log.class);
		defaultYamlConfig = mock(File.class);
		workingDirectory = mock(File.class);
		path = mock(Path.class);

		mavenAdapter = new TestableMavenAdapter(project, log, defaultYamlConfig);
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
		when(config.getDefaultYamlFile()).thenReturn(Optional.empty());

		mavenAdapter.addInitialConfiguration(config, ""); //$NON-NLS-1$

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
	public void findYamlFilePath_yamlFileExists_shouldReturnFilePath() throws IOException {
		MavenProject project = mock(MavenProject.class);
		File yamlFile = folder.newFile("file.yaml");

		when(project.getBasedir()).thenReturn(folder.getRoot());

		String actualPath = mavenAdapter.findYamlFilePath(project, yamlFile);

		String expectedPath = yamlFile.getAbsolutePath();
		assertTrue(actualPath.equals(expectedPath));
	}

	@Test
	public void findYamlFilePath_yamlFileDoesntExists_shouldReturnParentFilePath() throws IOException {
		MavenProject project = mock(MavenProject.class);
		File yamlFile = folder.newFile("file.yaml");
		File baseDir = mock(File.class);
		MavenProject parentProject = mock(MavenProject.class);

		String expectedPath = yamlFile.getAbsolutePath();

		when(project.getBasedir()).thenReturn(baseDir);
		when(project.getParent()).thenReturn(parentProject);
		when(parentProject.getBasedir()).thenReturn(folder.getRoot());

		String actualPath = mavenAdapter.findYamlFilePath(project, yamlFile);
		assertTrue(actualPath.equals(expectedPath));
	}

	@Test
	public void findYamlFilePath_parentIsRootProject_shouldReturnFilePath() throws IOException {
		MavenProject project = mock(MavenProject.class);
		File yamlFile = mock(File.class);
		File parentYmlFile = folder.newFile("file.yaml");

		String expectedPath = parentYmlFile.getAbsolutePath();

		mavenAdapter.setRootProject(project);
		mavenAdapter.setDefaultYamlFile(parentYmlFile);
		when(project.getBasedir()).thenReturn(mock(File.class));
		when(yamlFile.exists()).thenReturn(false);
		when(project.getParent()).thenReturn(project);
		when(yamlFile.getName()).thenReturn("file.yml");

		String actualPath = mavenAdapter.findYamlFilePath(project, yamlFile);
		assertTrue(actualPath.equals(expectedPath));
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

		public TestableMavenAdapter(MavenProject project, Log log, File defaultYamlFile) {
			super(project, log, defaultYamlFile);
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
