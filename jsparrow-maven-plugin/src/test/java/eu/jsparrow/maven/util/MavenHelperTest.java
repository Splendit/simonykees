package eu.jsparrow.maven.util;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;

import java.awt.image.DirectColorModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.discovery.AbstractComponentDiscoverer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;

import com.google.common.io.Files;

public class MavenHelperTest {

	private MavenProject project;
	private Log log;
	
	private File workingDirectory;
	private BundleContext bundleContext;
	private InputStream bundleInputStream;
	private BufferedReader bundleBufferedReader;
	private InputStream resourceInputStream;
	
	private MavenHelper mavenHelper;
	
	private boolean isInputStreamNull;
	
	@Before
	public void setUp() {
		project = mock(MavenProject.class);
		log = mock(Log.class);
		workingDirectory = mock(File.class);
		bundleContext = mock(BundleContext.class);
		bundleInputStream = mock(InputStream.class);
		bundleBufferedReader = mock(BufferedReader.class);
		resourceInputStream = mock(InputStream.class);
		
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
	
	@Test
	public void loadBundles_inputStreamIsNotNull_bundlesInstalledAndAdded() throws Exception {
		Bundle bundle = mock(Bundle.class);
		
		isInputStreamNull = false;
		String line1 = "line1"; //$NON-NLS-1$
				
		doAnswer(new Answer<String>() {
			
			private int counter = 0;
			
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				if(counter > 0) {
					return null;
				} else {
					counter++;
					return line1;
				}
			} 
		}).when(bundleBufferedReader).readLine();
		
		when(bundleContext.installBundle(anyString(), eq(resourceInputStream))).thenReturn(bundle);
		
		List<Bundle> bundles = mavenHelper.loadBundles();
		
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(bundleContext).installBundle(captor.capture(), eq(resourceInputStream));
		assertTrue(captor.getValue().contains(line1));
		
		assertTrue(bundles.size() == 1);
	}
	
	@Test(expected = MojoExecutionException.class)
	public void loadBundles_inputStreamIsNull_noInteractionWithReaderOrBundleContext() throws Exception {
		isInputStreamNull = true;
		
		mavenHelper.loadBundles();
		
		assertTrue(false);
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
		
		@Override
		protected BundleContext getBundleContext() {
			return bundleContext;
		}
		
		@Override
		protected InputStream getManifestInputStream() {
			if(isInputStreamNull) {
				return null;
			}
			return bundleInputStream;
		}
		
		@Override
		protected BufferedReader getBufferedReaderFromInputStream(InputStream is) {
			return bundleBufferedReader;
		}
		
		@Override
		protected InputStream getBundleResourceInputStream(String resouceName) {
			return resourceInputStream;
		}
		
	}
}
