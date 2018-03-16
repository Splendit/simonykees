package eu.jsparrow.adapter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class EmbeddedMavenTest {

	private Log log;
	private EmbeddedMaven embeddedMaven;
	private InputStream inputStream;
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		log = mock(Log.class);
		embeddedMaven = new TestableEmbeddedMaven(log, "mavenHome"); //$NON-NLS-1$
	}

	@Test
	public void unzip_isFile() throws Exception {
		File zipInputStream = createDummyZip("test.txt", false); //$NON-NLS-1$

		embeddedMaven.unzip(new FileInputStream(zipInputStream), folder.getRoot()
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

		embeddedMaven.unzip(new FileInputStream(zipInputStream), folder.getRoot()
			.getAbsolutePath());

		List<String> fileList = Files.list(folder.getRoot()
			.toPath())
			.map(x -> x.getFileName()
				.toString())
			.collect(Collectors.toList());
		assertThat(fileList, hasItem("test")); //$NON-NLS-1$
	}
	
	@Test
	public void prepareMaven_shouldReturnExistingMavenHome() {
		String expectedMavenHome = "expected/maven/home";
		embeddedMaven.setMavenHome(expectedMavenHome);
		
		String actualMavenHome = embeddedMaven.prepareMaven();
		
		assertTrue(expectedMavenHome.equals(actualMavenHome));
	}
	
	@Test
	public void prepareMaven_shouldReturnNewMavenHome() throws Exception {
		String providedMavenHome = "expected/maven/home/EMBEDDED";
		embeddedMaven.setMavenHome(providedMavenHome);
		File zipInputStream = createDummyZip("/newmaven/", true);
		inputStream = new FileInputStream(zipInputStream);
		String actualMavenHome = embeddedMaven.prepareMaven();
		
		assertFalse(actualMavenHome.equals(providedMavenHome));
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

	class TestableEmbeddedMaven extends EmbeddedMaven {

		public TestableEmbeddedMaven(Log log, String mavenHome) {
			super(log, mavenHome);
		}
		
		@Override
		protected InputStream getMavenZipInputStream() {
			return inputStream;
		}
	}
}
