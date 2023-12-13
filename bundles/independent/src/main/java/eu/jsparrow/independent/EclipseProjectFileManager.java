package eu.jsparrow.independent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.independent.exceptions.StandaloneException;

/**
 * Manages the backup and restore of existing eclipse project files.
 * 
 * @since 3.3.0
 */
public class EclipseProjectFileManager {

	private static final List<ProjectFiles> PROJECT_FILES_LIST = Collections
		.unmodifiableList(Arrays.asList(ProjectFiles.PROJECT_DESCRIPTION_FILE,
				ProjectFiles.CLASS_PATH_FILE, ProjectFiles.SETTINGS_DIRECTORY));

	private static final Logger logger = LoggerFactory.getLogger(EclipseProjectFileManager.class);
	private List<EclipseProjectFileManagerStatus> projects;

	public EclipseProjectFileManager() {
		this.projects = new LinkedList<>();
	}

	/**
	 * Add a project to be tracked by this {@link EclipseProjectFileManager}.
	 * 
	 * @param projectRootPath
	 *            root path of the project to be tracked
	 */
	public void addProject(String projectRootPath) {
		EclipseProjectFileManagerStatus projectStatus = new EclipseProjectFileManagerStatus(projectRootPath);
		projects.add(projectStatus);
	}

	/**
	 * Add projects to be tracked by this {@link EclipseProjectFileManager}.
	 * 
	 * @param projectRootPaths
	 *            list of root paths of projects to be tracked
	 */
	public void addProjects(List<String> projectRootPaths) {
		projectRootPaths.stream()
			.forEach(this::addProject);
	}

	/**
	 * this method prepares projects for creating an eclipse project accordingly
	 * by renaming any existing .project and .classpath files and the .settings
	 * directory temporarily.
	 * 
	 * @throws StandaloneException
	 *             if the existing project files cannot be renamed
	 */
	public void backupExistingEclipseFiles() throws IOException {
		for (EclipseProjectFileManagerStatus p : projects) {
			doBackupExistingEclipseFiles(p);
		}
	}

	private void doBackupExistingEclipseFiles(EclipseProjectFileManagerStatus project) throws IOException {
		String path = project.getPath();
		String name = Paths.get(path)
			.getFileName()
			.toString();

		for (ProjectFiles projectFile : PROJECT_FILES_LIST) {
			File fileToBackup = getFile(path, projectFile);
			if (fileToBackup.exists()) {
				moveFile(fileToBackup, getBackupFile(path, projectFile));
				project.setExistingFileMoved(projectFile, true);
				String loggerInfo = getBackupDoneMessage(projectFile, name);
				logger.debug(loggerInfo);
			}
		}
	}

	/**
	 * On stop, checks if eclipse project files were existing and backed up
	 * before refactoring and reverts them.
	 * 
	 * @throws IOException
	 */
	private void restoreExistingEclipseFiles(EclipseProjectFileManagerStatus project) throws IOException {

		String path = project.getPath();
		String name = Paths.get(path)
			.getFileName()
			.toString();

		for (ProjectFiles projectFile : PROJECT_FILES_LIST) {
			if (project.isExistingFileMoved(projectFile)) {
				Files.move(getBackupFile(path, projectFile).toPath(), getFile(path, projectFile).toPath());
				String loggerInfo = getRestoreDoneMessage(projectFile, name);
				logger.debug(loggerInfo);
			}
		}
	}

	private void deleteCreatedEclipseProjectFiles(EclipseProjectFileManagerStatus project) throws IOException {
		String path = project.getPath();

		File settings = getFile(path, ProjectFiles.SETTINGS_DIRECTORY);
		removeDirectory(settings);
		Files.deleteIfExists(getFile(path, ProjectFiles.CLASS_PATH_FILE).toPath());
		Files.deleteIfExists(getFile(path, ProjectFiles.PROJECT_DESCRIPTION_FILE).toPath());
	}

	public void revertEclipseProjectFiles() {
		for (EclipseProjectFileManagerStatus p : projects) {
			try {
				doRevertEclipseProjectFiles(p);
			} catch (IOException e) {
				logger.debug(e.getMessage(), e);
			}
		}
	}

	private void doRevertEclipseProjectFiles(EclipseProjectFileManagerStatus project) throws IOException {
		if (!project.isCleanUpAlreadyDone()) {
			deleteCreatedEclipseProjectFiles(project);
			restoreExistingEclipseFiles(project);
			project.setCleanUpAlreadyDone(true);
		}
	}

	protected String getBackupDoneMessage(ProjectFiles projectFile, String projectName) {
		String fileName = projectFile.getFileName();
		if (projectFile.isDirectory()) {
			return NLS.bind(Messages.EclipseProjectFileManager_directoryBackupDone, fileName, projectName);
		}
		return NLS.bind(Messages.EclipseProjectFileManager_fileBackupDone, fileName, projectName);
	}

	protected String getRestoreDoneMessage(ProjectFiles projectFile, String projectName) {
		String fileName = projectFile.getFileName();
		if (projectFile.isDirectory()) {
			return NLS.bind(Messages.EclipseProjectFileManager_directoryRestoreDone, fileName, projectName);
		}
		return NLS.bind(Messages.EclipseProjectFileManager_fileRestoreDone, fileName, projectName);
	}

	protected File getFile(String path, ProjectFiles projectFile) {
		return new File(path + File.separator + projectFile.getFileName());
	}

	protected File getBackupFile(String path, ProjectFiles projectFile) {
		return new File(path + File.separator + projectFile.getBackupFileName());
	}

	protected void moveFile(File src, File dest) throws IOException {
		Files.move(src.toPath(), dest.toPath());
	}

	protected void removeDirectory(File directory) throws IOException {
		if (!directory.isDirectory()) {
			Files.deleteIfExists(directory.toPath());
			return;
		}
		for (File file : directory.listFiles()) {
			removeDirectory(file);
		}
		Files.delete(directory.toPath());
	}

	protected List<EclipseProjectFileManagerStatus> getProjects() {
		return this.projects;
	}

	/**
	 * Holds each project's status for managing eclipse project files
	 * 
	 * @since 3.3.0
	 */
	class EclipseProjectFileManagerStatus {
		private String path;
		private boolean cleanUpAlreadyDone = false;
		private final Map<ProjectFiles, Boolean> movedProjectFiles = new HashMap<>();

		public EclipseProjectFileManagerStatus(String path) {
			this.path = path;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public boolean isExistingFileMoved(ProjectFiles projectFile) {
			return movedProjectFiles.containsKey(projectFile) && movedProjectFiles.get(projectFile)
				.booleanValue();
		}

		public void setExistingFileMoved(ProjectFiles projectFile, boolean existingFileMoved) {
			movedProjectFiles.put(projectFile, Boolean.valueOf(existingFileMoved));
		}

		public boolean isCleanUpAlreadyDone() {
			return cleanUpAlreadyDone;
		}

		public void setCleanUpAlreadyDone(boolean cleanUpAlreadyDone) {
			this.cleanUpAlreadyDone = cleanUpAlreadyDone;
		}
	}
}
