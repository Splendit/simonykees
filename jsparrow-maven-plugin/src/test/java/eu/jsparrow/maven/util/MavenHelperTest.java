package eu.jsparrow.maven.util;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

public class MavenHelperTest {

	private MavenProject project = mock(MavenProject.class);
	private Log log = mock(Log.class);
	
	@Test
	@SuppressWarnings("nls")
	public void prepareConfiguration_additionalConfigurationNull() throws Exception {
		MavenHelper mavenHepler = new TestableMavenHelper(project, "mavenHome", log);

		final Map<String, String> configuration = mavenHepler.prepareConfiguration(null);

		assertTrue(configuration.size() == 5);
	}

	@Test
	@SuppressWarnings("nls")
	public void prepareConfiguration_additionalConfigurationNotNull() throws Exception {
		MavenHelper mavenHepler = new TestableMavenHelper(project, "mavenHome", log);

		// TODO how to test this
		final Map<String, String> additionalConfiguration = new HashMap<>();

		final Map<String, String> configuration = mavenHepler.prepareConfiguration(additionalConfiguration);

		assertTrue(configuration.size() == 5);
	}

	@Test(expected = InterruptedException.class)
	public void prepareWorkingDirectory_directoryExists() throws Exception {
		MavenHelper mavenHepler = new TestableMavenHelper(project, "mavenHome", log);

		final Map<String, String> configuration = mavenHepler.prepareConfiguration(null);
		
		mavenHepler.prepareWorkingDirectory(configuration);
	}

	public void prepareWorkingDirectory_directoryMkdirs() throws Exception {

	}

	public void prepareWorkingDirectory_Else() throws Exception {

	}

	@SuppressWarnings("nls")
	class TestableMavenHelper extends MavenHelper {

		private File directory;

		public TestableMavenHelper(MavenProject project, String mavenHome, Log log) {
			super(project, mavenHome, log);
		}

		@Override
		protected String getProjectPath() {
			return "path";
		}

		@Override
		protected String getProjectName() {
			return "name";
		}
		
		@Override
		protected void setDirectory() {
			directory = new File("tmp");
		}
	}
}
