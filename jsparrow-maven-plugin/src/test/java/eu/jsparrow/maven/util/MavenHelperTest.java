package eu.jsparrow.maven.util;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;

import java.awt.image.DirectColorModel;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.discovery.AbstractComponentDiscoverer;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Constants;

import com.google.common.io.Files;

public class MavenHelperTest {

	private MavenProject project = mock(MavenProject.class);
	private Log log = mock(Log.class);
	
	File workingDirectory = mock(File.class);
	
	private MavenHelper mavenHelper;
	
	@Before
	public void setUp() {
		mavenHelper = new TestableMavenHelper(project, "mavenHome", log); //$NON-NLS-1$
	}
	
	@Test
	public void prepareConfiguration_additionalConfigurationNull() throws Exception {
		
		final Map<String, String> configuration = mavenHelper.prepareConfiguration(null);

		assertTrue(configuration.size() == 5);
	}

	@Test
	public void prepareConfiguration_additionalConfigurationNotNull() throws Exception {
		@SuppressWarnings("unchecked")
		final Map<String, String> additionalConfiguration = mock(HashMap.class);

		mavenHelper.prepareConfiguration(additionalConfiguration);

		verify(additionalConfiguration).put(eq(Constants.FRAMEWORK_STORAGE_CLEAN), eq(Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT));
		verify(additionalConfiguration).put(eq(Constants.FRAMEWORK_STORAGE), anyString());
		verify(additionalConfiguration, times(5)).put(anyString(), anyString());		
	}

	@Test(expected = InterruptedException.class)
	public void prepareWorkingDirectory_directoryDoesNotExistAndMkdirsNotWorking() throws Exception {
		when(workingDirectory.exists()).thenReturn(false);
		when(workingDirectory.mkdirs()).thenReturn(false);

		mavenHelper.prepareWorkingDirectory(null);
		
		assertTrue(false);
	}
	
	@Test
	public void prepareWorkingDirectory_directoryDoesNotExistAndMkdirsIsWorking() throws Exception {
		@SuppressWarnings("unchecked")
		Map<String, String> configuration = mock(Map.class);
		
		String absolutePath = "somePath";
		
		when(workingDirectory.exists()).thenReturn(false);
		when(workingDirectory.mkdirs()).thenReturn(true);
		when(workingDirectory.getAbsolutePath()).thenReturn(absolutePath);
		
		mavenHelper.prepareWorkingDirectory(configuration);
		
		verify(workingDirectory, times(3)).getAbsolutePath();
		verify(configuration).put(anyString(), eq(absolutePath));
	}
	
	@Test(expected = InterruptedException.class)
	public void prepareWorkingDirectory_directoryExistsDoesNotContainExactlyOneElement() throws Exception {
		when(workingDirectory.exists()).thenReturn(true);
		when(workingDirectory.list()).thenReturn(new String[] {});
		
		mavenHelper.prepareWorkingDirectory(null);
		
		assertTrue(false);
	}
	
	@Test
	public void prepareWorkingDirectory_directoryExistsAndContainsExactlyOneElement() throws Exception {
		@SuppressWarnings("unchecked")
		Map<String, String> configuration = mock(HashMap.class);
		
		when(workingDirectory.exists()).thenReturn(true);
		when(workingDirectory.list()).thenReturn(new String[] {"element"}); //$NON-NLS-1$
		
		String apsolutePath = "somePath"; //$NON-NLS-1$
		when(workingDirectory.getAbsolutePath()).thenReturn(apsolutePath);
		
		mavenHelper.prepareWorkingDirectory(configuration);	
		
		verify(workingDirectory, times(3)).getAbsolutePath();
		verify(configuration).put(anyString(), eq(apsolutePath));
	}

	@SuppressWarnings("nls")
	class TestableMavenHelper extends MavenHelper {

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
		protected void setWorkingDirectory() {
			setDirectory(workingDirectory);
		}
	}
}
