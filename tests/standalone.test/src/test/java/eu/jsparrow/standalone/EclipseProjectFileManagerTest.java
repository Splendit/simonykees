package eu.jsparrow.standalone;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.core.runtime.CoreException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.standalone.exceptions.StandaloneException;

/**
 * Tests for {@link EclipseProjectFileManager}
 * 
 * @since 3.3.0
 *
 */
public class EclipseProjectFileManagerTest {

	private static final String DOT_PROJECT = ".project"; //$NON-NLS-1$
	private static final String DOT_CLASSPATH = ".classpath"; //$NON-NLS-1$
	private static final String DOT_SETTINGS = ".settings"; //$NON-NLS-1$
	private static final String DOT_TEMP = ".tmp"; //$NON-NLS-1$
	private static final String PROJECT_ROOT_DIR = "project-root-dir"; //$NON-NLS-1$

	private Path tempDirectory;
	private File projectFile;
	private File projectFileTmp;
	private File classpathFile;
	private File classpathFileTmp;
	private File settingsDirFile;
	private File settingsDirFileTmp;
	private File baseDir;

	private EclipseProjectFileManager manager;

	private static void deleteIfExists(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			File[] childFiles = path.toFile().listFiles();
			for (File childFile : childFiles) {
				deleteIfExists(childFile.toPath());
			}
			Files.delete(path);
		} else {
			Files.deleteIfExists(path);
		}
	}

	@BeforeEach
	public void setUp() throws Exception {
		tempDirectory = Files.createTempDirectory("jsparrow-standalone-test-").toAbsolutePath();
		baseDir = new File(tempDirectory.toFile(), PROJECT_ROOT_DIR);
		Files.createDirectory(baseDir.toPath());

		projectFile = new File(baseDir.getPath(), DOT_PROJECT);
		classpathFile = new File(baseDir.getPath(), DOT_CLASSPATH);
		settingsDirFile = new File(baseDir.getPath(), DOT_SETTINGS);
		Files.createFile(projectFile.toPath());
		Files.createFile(classpathFile.toPath());
		Files.createDirectory(settingsDirFile.toPath());

		projectFileTmp = new File(baseDir.getPath(), DOT_PROJECT + DOT_TEMP);
		classpathFileTmp = new File(baseDir.getPath(), DOT_CLASSPATH + DOT_TEMP);
		settingsDirFileTmp = new File(baseDir.getPath(), DOT_SETTINGS + DOT_TEMP);

		manager = new TestableEclipseProjectFileManager();
		manager.addProject(baseDir.getAbsolutePath());
	}

	@AfterEach
	public void tearDown() throws IOException {
		deleteIfExists(tempDirectory);
	}

	@Test
	public void backupExistingEclipseFiles_dotProjectExists() throws StandaloneException, IOException {

		manager.backupExistingEclipseFiles();

		assertTrue(Files.exists(projectFileTmp.toPath()));

	}

	@Test
	public void backupExistingEclipseFiles_dotClasspathExists() throws StandaloneException, IOException {

		manager.backupExistingEclipseFiles();

		assertTrue(Files.exists(classpathFileTmp.toPath()));

	}

	@Test
	public void backupExistingEclipseFiles_dotSettingsExists() throws StandaloneException, IOException {

		manager.backupExistingEclipseFiles();

		assertTrue(Files.exists(settingsDirFileTmp.toPath()));

	}

	@Test
	public void restoreExistingEclipseFiles_projectFileMoved() throws IOException, CoreException {
		manager.getProjects().stream().forEach(p -> p.setExistingProjectFileMoved(true));

		Files.createFile(projectFileTmp.toPath());

		manager.revertEclipseProjectFiles();

		assertTrue(projectFile.exists());
		assertFalse(projectFileTmp.exists());
	}

	@Test
	public void restoreExistingEclipseFiles_classPathFileMoved() throws IOException, CoreException {
		manager.getProjects().stream().forEach(p -> p.setExistingClasspathFileMoved(true));

		Files.createFile(classpathFileTmp.toPath());

		manager.revertEclipseProjectFiles();

		assertTrue(classpathFile.exists());
		assertFalse(classpathFileTmp.exists());
	}

	@Test
	public void restoreExistingEclipseFiles_settingsFolderMoved() throws IOException, CoreException {
		manager.getProjects().stream().forEach(p -> p.setExistingSettingsDirectoryMoved(true));

		Files.createDirectory(settingsDirFileTmp.toPath());

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
