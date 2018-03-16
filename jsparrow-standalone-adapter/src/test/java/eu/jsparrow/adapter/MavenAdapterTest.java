package eu.jsparrow.adapter;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
		when(config.getProfile()).thenReturn(Optional.empty());

		mavenAdapter.addInitialConfiguration(config, ""); //$NON-NLS-1$

		verify(config).getUseDefaultConfig();
		verify(config).getMode();
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
		@SuppressWarnings("unchecked")
		Map<String, String> configuration = mock(Map.class);

		String absolutePath = "somePath"; //$NON-NLS-1$

		when(workingDirectory.exists()).thenReturn(false);
		when(workingDirectory.mkdirs()).thenReturn(true);
		when(workingDirectory.getAbsolutePath()).thenReturn(absolutePath);

		mavenAdapter.prepareWorkingDirectory();

		verify(workingDirectory).getAbsolutePath();
		assertTrue(mavenAdapter.getConfiguration().getOrDefault("osgi.instance.area", "asdf").equals(absolutePath));
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
	public void findYamlFilePath_yamlFileExists_shouldReturnFilePath() {
		File yamlFile = mock(File.class);
		String expectedPath = "default/file/exists";
		
		when(yamlFile.exists()).thenReturn(true);
		when(yamlFile.getAbsolutePath()).thenReturn(expectedPath);
		
		String actualPath = mavenAdapter.findYamlFilePath(null, yamlFile);
		
		assertTrue(actualPath.equals(expectedPath));
	}
	
	@Test
	public void findYamlFilePath_yamlFileDoesntExists_shouldReturnFilePath() {
		MavenProject project = mock(MavenProject.class);
		File yamlFile = mock(File.class);
		File parentBaseDir = mock(File.class);
		File parentYmlFile = mock(File.class);
		
		String expectedPath = "parent/dir/file.yml";
		
		
		when(yamlFile.exists()).thenReturn(false);
		when(project.getParent()).thenReturn(project);
		when(project.getBasedir()).thenReturn(parentBaseDir);
		when(parentBaseDir.getAbsolutePath()).thenReturn("parent/dir");
		when(yamlFile.getPath()).thenReturn("file.yml");
		when(path.toFile()).thenReturn(parentYmlFile);
		when(path.toString()).thenReturn(expectedPath);
		when(parentYmlFile.exists()).thenReturn(true);
		
		String actualPath = mavenAdapter.findYamlFilePath(project, yamlFile);
		assertTrue(actualPath.equals(expectedPath));
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

	}
}
