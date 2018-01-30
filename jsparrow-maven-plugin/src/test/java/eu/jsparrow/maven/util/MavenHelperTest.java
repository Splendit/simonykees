package eu.jsparrow.maven.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

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

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

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

		verify(additionalConfiguration).put(eq(Constants.FRAMEWORK_STORAGE_CLEAN),
				eq(Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT));
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

		String absolutePath = "somePath"; //$NON-NLS-1$

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
		when(workingDirectory.list()).thenReturn(new String[] { "element" }); //$NON-NLS-1$

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
				if (counter > 0) {
					return null;
				} else {
					counter++;
					return line1;
				}
			}
		}).when(bundleBufferedReader)
			.readLine();

		when(bundleContext.installBundle(anyString(), eq(resourceInputStream))).thenReturn(bundle);

		List<Bundle> bundles = mavenHelper.loadBundles();

		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(bundleContext).installBundle(captor.capture(), eq(resourceInputStream));
		assertTrue(captor.getValue()
			.contains(line1));

		assertTrue(bundles.size() == 1);
	}

	@Test(expected = MojoExecutionException.class)
	public void loadBundles_inputStreamIsNull_noInteractionWithReaderOrBundleContext() throws Exception {
		isInputStreamNull = true;

		mavenHelper.loadBundles();

		assertTrue(false);
	}

	@Test
	public void startBundles_shouldStartBundle() throws Exception {
		@SuppressWarnings("unchecked")
		Dictionary<String, String> headers = mock(Dictionary.class);
		List<Bundle> bundles = new ArrayList<>();

		Bundle bundle = mock(Bundle.class);
		bundles.add(bundle);

		when(bundle.getHeaders()).thenReturn(headers);
		when(headers.get(eq(Constants.FRAGMENT_HOST))).thenReturn(null);
		when(bundle.getSymbolicName()).thenReturn(MavenHelper.STANDALONE_BUNDLE_NAME);

		mavenHelper.startBundles(bundles);

		verify(bundle).start();
		assertTrue(mavenHelper.isStandaloneStarted());
	}

	@Test
	public void startBundles_fragmentHostNotNull() throws Exception {
		@SuppressWarnings("unchecked")
		Dictionary<String, String> headers = mock(Dictionary.class);
		List<Bundle> bundles = new ArrayList<>();

		Bundle bundle = mock(Bundle.class);
		bundles.add(bundle);

		when(bundle.getHeaders()).thenReturn(headers);
		when(headers.get(eq(Constants.FRAGMENT_HOST))).thenReturn("someHost"); //$NON-NLS-1$

		mavenHelper.startBundles(bundles);

		verify(bundle).getHeaders();
		verifyNoMoreInteractions(bundle);
		assertFalse(mavenHelper.isStandaloneStarted());
	}

	@Test
	public void startBundles_symbolicNameIsNull() throws Exception {
		@SuppressWarnings("unchecked")
		Dictionary<String, String> headers = mock(Dictionary.class);
		List<Bundle> bundles = new ArrayList<>();

		Bundle bundle = mock(Bundle.class);
		bundles.add(bundle);

		when(bundle.getHeaders()).thenReturn(headers);
		when(headers.get(eq(Constants.FRAGMENT_HOST))).thenReturn(null);
		when(bundle.getSymbolicName()).thenReturn(null);

		mavenHelper.startBundles(bundles);

		verify(bundle).getHeaders();
		verify(bundle).getSymbolicName();
		verifyNoMoreInteractions(bundle);
		assertFalse(mavenHelper.isStandaloneStarted());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void prepareDefaultRequest() {
		InvocationRequest request = mock(InvocationRequest.class);
		Properties props = mock(Properties.class);

		mavenHelper.prepareDefaultRequest(request, props);

		ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
		verify(request).setPomFile(fileCaptor.capture());
		assertTrue(fileCaptor.getValue()
			.getAbsolutePath()
			.endsWith("pom.xml")); //$NON-NLS-1$

		ArgumentCaptor<List> goalsCaptor = ArgumentCaptor.forClass(List.class);
		verify(request).setGoals(goalsCaptor.capture());
		assertTrue(goalsCaptor.getValue()
			.size() == 1);

		verify(props).setProperty(eq(MavenHelper.OUTPUT_DIRECTORY_CONSTANT), anyString());

		verify(request).setProperties(eq(props));
	}

	@Test
	public void unzip_isFile() throws Exception {
		File zipInputStream = createDummyZip("test.txt", false); //$NON-NLS-1$

		mavenHelper.unzip(new FileInputStream(zipInputStream), folder.getRoot()
			.getAbsolutePath());

		List<String> fileList = Files.list(folder.getRoot()
			.toPath())
			.map(x -> x.getFileName()
				.toString())
			.collect(Collectors.toList());
		assertThat(fileList, hasItem("test.txt")); //$NON-NLS-1$
	}

	@Test
	public void unzip_isDirectoryTrue() throws Exception {
		File zipInputStream = createDummyZip("test/", true); //$NON-NLS-1$

		mavenHelper.unzip(new FileInputStream(zipInputStream), folder.getRoot()
			.getAbsolutePath());

		List<String> fileList = Files.list(folder.getRoot()
			.toPath())
			.map(x -> x.getFileName()
				.toString())
			.collect(Collectors.toList());
		assertThat(fileList, hasItem("test")); //$NON-NLS-1$
	}

	private File createDummyZip(String zipEntryName, boolean isDirectory) throws IOException {
		File file = folder.newFile("test.zip"); //$NON-NLS-1$
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
		ZipEntry entry = new ZipEntry(zipEntryName);
		out.putNextEntry(entry);

		if (!isDirectory) {
			StringBuilder sb = new StringBuilder();
			sb.append("Test String"); //$NON-NLS-1$
			byte[] data = sb.toString()
				.getBytes();
			out.write(data, 0, data.length);
		}

		out.closeEntry();
		out.close();
		return file;
	}

	class TestableMavenHelper extends MavenHelper {

		public TestableMavenHelper(MavenProject project, String mavenHome, Log log) {
			super(project, mavenHome, log);
		}

		@Override
		protected String getProjectPath() {
			return "path"; //$NON-NLS-1$
		}

		@Override
		protected String getProjectName() {
			return "name"; //$NON-NLS-1$
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
			if (isInputStreamNull) {
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
