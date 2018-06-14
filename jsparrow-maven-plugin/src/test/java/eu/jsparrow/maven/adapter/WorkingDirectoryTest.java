package eu.jsparrow.maven.adapter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	public void cleanUp_onlyRootIdInLock_shouldDeleteTempFolder() throws IOException {
		writeToLockFile(rootProjectId);

		workingDirectory.cleanUp();

		assertTrue(readLockFile().isEmpty());
	}

	@Test
	public void cleanUp_multipleProjectsInLock_shouldNotDeleteTempFolder() throws IOException {
		writeToLockFile(rootProjectId + "\n" + "another-project-id");

		workingDirectory.cleanUp();

		assertFalse(readLockFile().isEmpty());
	}

	@Test
	public void cleanUp_multipleProjectsInLock_shouldDeleteProjectRelatedFiles() throws IOException {
		writeToLockFile(rootProjectId + "\n" + "another-project-id");

		File deps = temporaryDirectory.newFolder("temp_jsparrow" + File.separator + "deps");
		File projectRelated = new File(deps.getPath() + File.separator + artifactId);
		projectRelated.createNewFile();

		workingDirectory.cleanUp();

		assertTrue(jsparrowTempFolder.exists());
		assertFalse(Files.exists(projectRelated.toPath()));

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

	private List<String> readLockFile() throws IOException {
		List<String> content = new ArrayList<>();
		Path path = Paths.get(workingDirectory.calculateJsparrowLockFilePath());
		try (Stream<String> lines = Files.lines(path)) {
			content = lines.map(String::trim)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toList());
		}
		return content;
	}

	class TestableWorkingDirectory extends WorkingDirectory {

		public TestableWorkingDirectory(File workingDirectory, Set<String> sessionRelatedProjects, Log log) {
			super(workingDirectory, sessionRelatedProjects, log);
		}

		@Override
		protected String calculateJsparrowLockFilePath() {
			return jsparrowTempFolder.getPath() + File.separator + LOCK_FILE_NAME;
		}
	}

}
