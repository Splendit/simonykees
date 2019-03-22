package eu.jsparrow.standalone;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.core.runtime.CoreException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import eu.jsparrow.standalone.exceptions.StandaloneException;

/**
 * Tests for {@link EclipseProjectFileManager}
 * 
 * @since 3.3.0
 *
 */
public class EclipseProjectFileManagerTest {

	private static Path path;

	private static final String DOT_PROJECT = ".project"; //$NON-NLS-1$
	private static final String DOT_CLASSPATH = ".classpath"; //$NON-NLS-1$
	private static final String DOT_SETTINGS = ".settings"; //$NON-NLS-1$
	private static final String DOT_TEMP = ".tmp"; //$NON-NLS-1$
	private static final String PROJECT_ROOT_DIR = "project-root-dir"; //$NON-NLS-1$

	private File projectFile;
	private File projectFileTmp;
	private File classpathFile;
	private File classpathFileTmp;
	private File settingsDirFile;
	private File settingsDirFileTmp;
	private File baseDir;

	private EclipseProjectFileManager manager;

	@Rule
	public TemporaryFolder directory = new TemporaryFolder();

	@BeforeClass
	public static void setUpClass() throws IOException {
		path = Files.createTempDirectory("jsparrow-standlaone-test-"); //$NON-NLS-1$
	}

	@AfterClass
	public static void tearDownClass() throws IOException {

		Files.deleteIfExists(path);
	}

	@Before
	public void setUp() throws Exception {
		baseDir = directory.newFolder(PROJECT_ROOT_DIR);
		projectFile = directory.newFile(PROJECT_ROOT_DIR + File.separator + DOT_PROJECT);
		classpathFile = directory.newFile(PROJECT_ROOT_DIR + File.separator + DOT_CLASSPATH);
		settingsDirFile = directory.newFolder(PROJECT_ROOT_DIR, DOT_SETTINGS);
		projectFileTmp = new File(baseDir.getPath() + File.separator + DOT_PROJECT + DOT_TEMP);
		classpathFileTmp = new File(baseDir.getPath() + File.separator + DOT_CLASSPATH + DOT_TEMP);
		settingsDirFileTmp = new File(baseDir.getPath() + File.separator + DOT_SETTINGS + DOT_TEMP);

		manager = new TestableEclipseProjectFileManager();
		manager.addProject(baseDir.getAbsolutePath());
	}

	@Test
	public void backupExistingEclipseFiles_dotProjectExists() throws StandaloneException, IOException {

		projectFileTmp = new File(baseDir.getPath() + File.separator + DOT_PROJECT + DOT_TEMP);
		classpathFile = new File(baseDir.getPath() + File.separator + DOT_CLASSPATH);
		settingsDirFile = new File(baseDir.getPath(), DOT_SETTINGS);

		manager.backupExistingEclipseFiles();

		assertTrue(Files.exists(projectFileTmp.toPath()));

	}

	@Test
	public void backupExistingEclipseFiles_dotClasspathExists() throws StandaloneException, IOException {
		classpathFileTmp = new File(baseDir.getPath() + File.separator + DOT_CLASSPATH + DOT_TEMP);
		projectFile = new File(baseDir.getPath() + File.separator + DOT_PROJECT);
		settingsDirFile = new File(baseDir.getPath(), DOT_SETTINGS);

		manager.backupExistingEclipseFiles();

		assertTrue(Files.exists(classpathFileTmp.toPath()));

	}

	@Test
	public void backupExistingEclipseFiles_dotSettingsExists() throws StandaloneException, IOException {
		projectFile = new File(baseDir.getPath() + File.separator + DOT_PROJECT);
		classpathFile = new File(baseDir.getPath() + File.separator + DOT_CLASSPATH);
		settingsDirFileTmp = new File(baseDir.getPath() + File.separator + DOT_SETTINGS + DOT_TEMP);

		manager.backupExistingEclipseFiles();

		assertTrue(Files.exists(settingsDirFileTmp.toPath()));

	}

	@Test
	public void restoreExistingEclipseFiles_projectFileMoved() throws IOException, CoreException {
		manager.getProjects()
			.stream()
			.forEach(p -> p.setExistingProjectFileMoved(true));

		projectFileTmp = directory.newFile(PROJECT_ROOT_DIR + File.separator + DOT_PROJECT + DOT_TEMP);

		manager.revertEclipseProjectFiles();

		assertTrue(projectFile.exists());
		assertFalse(projectFileTmp.exists());
	}

	@Test
	public void restoreExistingEclipseFiles_classPathFileMoved() throws IOException, CoreException {
		manager.getProjects()
			.stream()
			.forEach(p -> p.setExistingClasspathFileMoved(true));

		classpathFileTmp = directory.newFile(PROJECT_ROOT_DIR + File.separator + DOT_PROJECT + DOT_TEMP);

		manager.revertEclipseProjectFiles();

		assertTrue(classpathFile.exists());
		assertFalse(classpathFileTmp.exists());
	}

	@Test
	public void restoreExistingEclipseFiles_settingsFolderMoved() throws IOException, CoreException {
		manager.getProjects()
			.stream()
			.forEach(p -> p.setExistingSettingsDirectoryMoved(true));

		settingsDirFileTmp = directory.newFolder(PROJECT_ROOT_DIR, DOT_SETTINGS + DOT_TEMP);

		manager.revertEclipseProjectFiles();

		assertTrue(settingsDirFile.exists());
		assertFalse(settingsDirFileTmp.exists());
	}

	class TestableEclipseProjectFileManager extends EclipseProjectFileManager {

		public TestableEclipseProjectFileManager() {

		}

		@Override
		protected File getProjectDescriptionFile(String path) {
			return projectFile;
		}

		@Override
		protected File getProjectDescriptionRenameFile(String path) {
			return projectFileTmp;
		}

		@Override
		protected File getClasspathFileRenameFile(String path) {
			return classpathFileTmp;
		}

		@Override
		protected File getClasspathFileFile(String path) {
			return classpathFile;
		}

		@Override
		protected File getSettingsDirectoryRenameFile(String path) {
			return settingsDirFileTmp;
		}

		@Override
		protected File getSettingsDirectoryFile(String path) {
			return settingsDirFile;
		}
	}
}
