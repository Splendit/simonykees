package eu.jsparrow.maven.adapter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@SuppressWarnings("nls")
public class WorkingDirectoryTest {

	private static final String LOCK_FILE_NAME = "lock";

	private WorkingDirectory workingDirectory;
	private String artifactId = "artifact-id";
	private String rootProjectId = "group-id." + artifactId;

	private File jsparrowTempFolder;

	@Rule
	public TemporaryFolder temporaryDirectory = new TemporaryFolder();

	@Before
	public void setUp() throws IOException {
		Log log = mock(Log.class);
		Set<String> sessionProjectIds = new HashSet<>();
		sessionProjectIds.add(rootProjectId);
		jsparrowTempFolder = temporaryDirectory.newFolder("temp_jsparrow");
		workingDirectory = new TestableWorkingDirectory(jsparrowTempFolder, sessionProjectIds, log);
	}

	@Test
	public void cleanUp_onlyRootIdInLock_shouldDeleteAllContentOfLockFile() throws IOException {
		writeToLockFile(rootProjectId);

		workingDirectory.cleanUp();

		assertTrue(isEmptyLockFile());
	}

	@Test
	public void cleanUp_multipleProjectsInLock_shouldNotDeleteAllContentsOfLockFile() throws IOException {
		writeToLockFile(rootProjectId + "\n" + "another-project-id");

		workingDirectory.cleanUp();

		assertFalse(isEmptyLockFile());
	}

	@Test
	public void lockProjects_shouldSaveAllIdsInLock() throws IOException {
		Set<String> projectIds = new HashSet<>();
		projectIds.add("root-project.id");
		projectIds.add("child-project.id");
		WorkingDirectory workDir = new TestableWorkingDirectory(jsparrowTempFolder, projectIds, mock(Log.class));

		workDir.lockProjects();

		List<String> lines = Files.lines(Paths.get(workDir.calculateJsparrowLockFilePath()))
			.collect(Collectors.toList());
		assertTrue(lines.containsAll(projectIds));
	}

	@Test
	public void isJsparrowRunning_shouldReturnTrue() throws IOException {
		writeToLockFile(rootProjectId);

		boolean runningOnRoot = workingDirectory.isJsparrowStarted(rootProjectId);

		assertTrue(runningOnRoot);
	}

	@Test
	public void isJsparrowRunning_shouldReturnFalse() throws IOException {
		String newProjectId = "new-project-id";
		writeToLockFile(rootProjectId);

		boolean runningOnRoot = workingDirectory.isJsparrowStarted(newProjectId);

		assertFalse(runningOnRoot);
	}

	@Test
	public void isJsparrowRunning_emptyLockFile_shouldReturnFalse() throws IOException {
		String newProjectId = "new-project-id";

		boolean runningOnRoot = workingDirectory.isJsparrowStarted(newProjectId);

		assertFalse(runningOnRoot);
	}

	private void writeToLockFile(String content) throws IOException {
		Files.write(Paths.get(workingDirectory.calculateJsparrowLockFilePath()), content.getBytes(),
				StandardOpenOption.CREATE, StandardOpenOption.APPEND);
	}

	private boolean isEmptyLockFile() throws IOException {
		String lockFilePath = workingDirectory.calculateJsparrowLockFilePath();
		return new File(lockFilePath).length() == 0;
	}

	class TestableWorkingDirectory extends WorkingDirectory {

		public TestableWorkingDirectory(File workingDirectory, Set<String> sessionRelatedProjects, Log log) {
			super(workingDirectory, sessionRelatedProjects, "", log);
		}

		@Override
		protected String calculateJsparrowLockFilePath() {
			return jsparrowTempFolder.getPath() + File.separator + LOCK_FILE_NAME;
		}
	}

}
