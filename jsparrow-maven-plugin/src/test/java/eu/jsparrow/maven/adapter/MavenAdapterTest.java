package eu.jsparrow.maven.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
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
	private MavenAdapter mavenAdapter;
	private Path path;
	private WorkingDirectory workingDirectory;
	private String groupId = "group-id";
	private String artifactId = "artifact-id";
	Properties properties;
	private File jsparrowTemDirectory;
	private File jsparrowYml;
	private File projectBaseDir;

	@Rule
	public TemporaryFolder directory = new TemporaryFolder();

	@Before
	public void setUp() throws IOException {
		project = mock(MavenProject.class);
		log = mock(Log.class);
		path = mock(Path.class);
		workingDirectory = mock(WorkingDirectory.class);
		mavenAdapter = new TestableMavenAdapter(project, log);

		jsparrowTemDirectory = directory.newFolder("temp_jSparrow");
		projectBaseDir = directory.newFolder("project_base_dir");
		jsparrowYml = new File(projectBaseDir.getPath() + File.separator + "jsparrow.yml");
		jsparrowYml.createNewFile();

		when(project.getGroupId()).thenReturn(groupId);
		when(project.getArtifactId()).thenReturn(artifactId);
		when(project.getBasedir()).thenReturn(projectBaseDir);
		properties = mock(Properties.class);
		when(project.getProperties()).thenReturn(properties);

	}

	@Test
	public void prepareConfiguration_additionalConfigurationNotNull() throws Exception {
		final MavenParameters config = mock(MavenParameters.class);
		when(config.getUseDefaultConfig()).thenReturn(false);
		when(config.getMode()).thenReturn(""); //$NON-NLS-1$
		when(config.getLicense()).thenReturn(""); //$NON-NLS-1$
		when(config.getUrl()).thenReturn(""); //$NON-NLS-1$
		when(config.getProfile()).thenReturn(""); //$NON-NLS-1$
		when(config.getRuleId()).thenReturn(Optional.empty());

		mavenAdapter.addInitialConfiguration(config); // $NON-NLS-1$

		verify(config).getUseDefaultConfig();
		verify(config).getMode();
		verify(config).getLicense();
		verify(config).getUrl();
		verify(config).getProfile();
	}

	@Test(expected = InterruptedException.class)
	public void prepareWorkingDirectory_directoryDoesNotExistAndMkdirsNotWorking() throws Exception {
		jsparrowTemDirectory = mock(File.class);
		when(jsparrowTemDirectory.exists()).thenReturn(false);
		when(jsparrowTemDirectory.mkdirs()).thenReturn(false);

		mavenAdapter.prepareWorkingDirectory();

		assertTrue(false);
	}

	@Test
	public void prepareWorkingDirectory_directoryDoesNotExistAndMkdirsIsWorking() throws Exception {

		String absolutePath = "somePath";
		jsparrowTemDirectory = mock(File.class);
		when(jsparrowTemDirectory.exists()).thenReturn(false);
		when(jsparrowTemDirectory.mkdirs()).thenReturn(true);
		when(jsparrowTemDirectory.getAbsolutePath()).thenReturn(absolutePath);

		mavenAdapter.prepareWorkingDirectory();

		verify(jsparrowTemDirectory).getAbsolutePath();
		assertTrue(mavenAdapter.getConfiguration()
			.getOrDefault("osgi.instance.area", "asdf") //$NON-NLS-1$ //$NON-NLS-2$
			.equals(absolutePath));
	}

	@Test
	public void setUp_listOfProjects() throws Exception {
		String expectedCompilerSource = "expectedCompilerSource";
		MavenParameters mavenParameters = new MavenParameters("list-rules");

		when(workingDirectory.isJsparrowStarted(any(String.class))).thenReturn(false);
		when(project.getPackaging()).thenReturn("jar");
		when(path.toFile()).thenReturn(jsparrowYml);
		when(properties.getProperty("maven.compiler.source")).thenReturn(expectedCompilerSource);

		mavenAdapter.setUpConfiguration(mavenParameters, Collections.singletonList(project), jsparrowYml);

		Map<String, String> configurations = mavenAdapter.getConfiguration();
		assertTrue(configurations.containsKey("NATURE.IDS." + groupId + "." + artifactId));
	}

	@Test(expected = MojoExecutionException.class)
	public void setUp_jsparrowAlreadyRunning() throws Exception {
		MavenParameters mavenParameters = new MavenParameters("list-rules");

		when(workingDirectory.isJsparrowStarted(any(String.class))).thenReturn(true);

		mavenAdapter.setUpConfiguration(mavenParameters, Collections.singletonList(project), jsparrowYml);

		assertTrue(false);
	}

	@Test
	public void setUp_initialConfiguration() throws InterruptedException {
		String expectedUrl = "https://localhost:8081";
		String expectedLicenseKey = "license-key";
		String expectedMode = "list-rules";
		MavenParameters mavenParameters = new MavenParameters(expectedMode, expectedLicenseKey, expectedUrl);

		mavenAdapter.setUpConfiguration(mavenParameters);

		Map<String, String> configuration = mavenAdapter.getConfiguration();
		assertTrue(configuration.containsKey("URL"));
		assertEquals(expectedUrl, configuration.getOrDefault("URL", ""));
		assertEquals(expectedLicenseKey, configuration.getOrDefault("LICENSE", ""));
		assertEquals(expectedMode, configuration.getOrDefault("STANDALONE.MODE", expectedMode));
	}

	class TestableMavenAdapter extends MavenAdapter {

		public TestableMavenAdapter(MavenProject project, Log log) {
			super(project, log);
		}

		@Override
		protected File createJsparrowTempDirectory() {
			return jsparrowTemDirectory;
		}

		@Override
		protected void setSystemProperty(String key, String value) {

		}

		@Override
		protected WorkingDirectory createWorkingDirectory(File directory) {
			return workingDirectory;
		}
	}
}
