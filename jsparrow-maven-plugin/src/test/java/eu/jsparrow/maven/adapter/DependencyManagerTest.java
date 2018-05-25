package eu.jsparrow.maven.adapter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

@SuppressWarnings({ "nls" })
public class DependencyManagerTest {

	private Log log;
	private DependencyManager dependencyManager;
	private InputStream inputStream;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	

	@Before
	public void setUp() {
		log = mock(Log.class);
		dependencyManager = new TestableDependencyManager(log, "mavenHome");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void prepareDefaultRequest() {
		InvocationRequest request = mock(InvocationRequest.class);
		Properties props = mock(Properties.class);
		MavenProject project = mock(MavenProject.class);
		File projectBaseDir = mock(File.class);
		String baseDirPath = "project/base/directory";

		when(project.getBasedir()).thenReturn(projectBaseDir);
		when(projectBaseDir.getAbsolutePath()).thenReturn(baseDirPath);

		dependencyManager.prepareDefaultRequest(project, request, props);

		ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
		verify(request).setPomFile(fileCaptor.capture());
		assertTrue(fileCaptor.getValue()
			.getAbsolutePath()
			.endsWith("pom.xml"));

		ArgumentCaptor<List> goalsCaptor = ArgumentCaptor.forClass(List.class);
		verify(request).setGoals(goalsCaptor.capture());
		assertTrue(goalsCaptor.getValue()
			.size() == 1);

		verify(props).setProperty(eq(DependencyManager.OUTPUT_DIRECTORY_OPTION_KEY), anyString());

		verify(request).setProperties(eq(props));
	}
	
	@Test
	public void unzip_isFile() throws Exception {
		File zipInputStream = createDummyZip("test.txt", false);

		dependencyManager.unzip(new FileInputStream(zipInputStream), folder.getRoot()
			.getAbsolutePath());

		List<String> fileList = Files.list(folder.getRoot()
			.toPath())
			.map(x -> x.getFileName()
				.toString())
			.collect(Collectors.toList());
		assertThat(fileList, hasItem("test.txt"));
	}

	@Test
	public void unzip_isDirectoryTrue() throws Exception {
		File zipInputStream = createDummyZip("test/", true);

		dependencyManager.unzip(new FileInputStream(zipInputStream), folder.getRoot()
			.getAbsolutePath());

		List<String> fileList = Files.list(folder.getRoot()
			.toPath())
			.map(x -> x.getFileName()
				.toString())
			.collect(Collectors.toList());
		assertThat(fileList, hasItem("test"));
	}

	@Test
	public void prepareMaven_shouldReturnExistingMavenHome() {
		String expectedMavenHome = "expected/maven/home";
		dependencyManager = new TestableDependencyManager(log, expectedMavenHome);
		
		String actualMavenHome = dependencyManager.prepareMavenHome();

		assertTrue(expectedMavenHome.equals(actualMavenHome));
	}

	@Test
	public void prepareMaven_shouldReturnNewMavenHome() throws Exception {
		String providedMavenHome = "expected/maven/home/EMBEDDED";
		dependencyManager = new TestableDependencyManager(log, providedMavenHome);
		File zipInputStream = createDummyZip("/newmaven/", true);
		inputStream = new FileInputStream(zipInputStream);
		String actualMavenHome = dependencyManager.prepareMavenHome();

		assertFalse(actualMavenHome.equals(providedMavenHome));
	}

	private File createDummyZip(String zipEntryName, boolean isDirectory) throws IOException {
		File file = folder.newFile("test.zip"); //$NON-NLS-1$
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
		ZipEntry entry = new ZipEntry(zipEntryName);
		out.putNextEntry(entry);

		if (!isDirectory) {
			StringBuilder sb = new StringBuilder();
			sb.append("Test String");
			byte[] data = sb.toString()
				.getBytes();
			out.write(data, 0, data.length);
		}

		out.closeEntry();
		out.close();
		return file;
	}
	
	
	class TestableDependencyManager extends DependencyManager {

		public TestableDependencyManager(Log log, String mavenHome) {
			super(log, mavenHome);
		}

		@Override
		protected InputStream getMavenZipInputStream() {
			return inputStream;
		}
	}
}
