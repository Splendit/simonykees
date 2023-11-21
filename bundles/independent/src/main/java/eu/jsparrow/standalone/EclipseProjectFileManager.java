package eu.jsparrow.standalone;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.standalone.exceptions.StandaloneException;

/**
 * Manages the backup and restore of existing eclipse project files.
 * 
 * @since 3.3.0
 */
public class EclipseProjectFileManager {

	private static final Logger logger = LoggerFactory.getLogger(EclipseProjectFileManager.class);

	private static final String PROJECT_FILE_NAME = ".project"; //$NON-NLS-1$
	private static final String CLASSPATH_FILE_NAME = ".classpath"; //$NON-NLS-1$
	private static final String SETTINGS_DIRECTORY_NAME = ".settings"; //$NON-NLS-1$
	private static final String TEMP_FILE_EXTENSION = ".tmp"; //$NON-NLS-1$

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

		File projectDescription = getProjectDescriptionFile(path);
		File classpathFile = getClasspathFileFile(path);
		File settingsDirectory = getSettingsDirectoryFile(path);

		String loggerInfo;

		if (projectDescription.exists()) {
			moveFile(projectDescription, getProjectDescriptionRenameFile(path));
			project.setExistingProjectFileMoved(true);

			loggerInfo = NLS.bind(Messages.EclipseProjectFileManager_fileBackupDone, PROJECT_FILE_NAME, name);
			logger.debug(loggerInfo);
		}

		if (classpathFile.exists()) {
			moveFile(classpathFile, getClasspathFileRenameFile(path));
			project.setExistingClasspathFileMoved(true);

			loggerInfo = NLS.bind(Messages.EclipseProjectFileManager_fileBackupDone, CLASSPATH_FILE_NAME, name);
			logger.debug(loggerInfo);
		}

		if (settingsDirectory.exists()) {
			moveFile(settingsDirectory, getSettingsDirectoryRenameFile(path));
			project.setExistingSettingsDirectoryMoved(true);

			loggerInfo = NLS.bind(Messages.EclipseProjectFileManager_directoryBackupDone, SETTINGS_DIRECTORY_NAME,
					name);
			logger.debug(loggerInfo);
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

		String loggerInfo;
		if (project.isExistingProjectFileMoved()) {
			Files.move(getProjectDescriptionRenameFile(path).toPath(), getProjectDescriptionFile(path).toPath());
			loggerInfo = NLS.bind(Messages.EclipseProjectFileManager_fileRestoreDone, PROJECT_FILE_NAME, name);
			logger.debug(loggerInfo);
		}

		if (project.isExistingClasspathFileMoved()) {
			Files.move(getClasspathFileRenameFile(path).toPath(), getClasspathFileFile(path).toPath());
			loggerInfo = NLS.bind(Messages.EclipseProjectFileManager_fileRestoreDone, CLASSPATH_FILE_NAME, name);
			logger.debug(loggerInfo);
		}

		if (project.isExistingSettingsDirectoryMoved()) {
			Files.move(getSettingsDirectoryRenameFile(path).toPath(), getSettingsDirectoryFile(path).toPath());
			loggerInfo = NLS.bind(Messages.EclipseProjectFileManager_directoryRestoreDone, SETTINGS_DIRECTORY_NAME,
					name);
			logger.debug(loggerInfo);
		}
	}

	private void deleteCreatedEclipseProjectFiles(EclipseProjectFileManagerStatus project) throws IOException {
		String path = project.getPath();

		File settings = getSettingsDirectoryFile(path);
		removeDirectory(settings);
		Files.deleteIfExists(getClasspathFileFile(path).toPath());
		Files.deleteIfExists(getProjectDescriptionFile(path).toPath());

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

	protected File getProjectDescriptionFile(String path) {
		return new File(path + File.separator + PROJECT_FILE_NAME);
	}

	protected File getProjectDescriptionRenameFile(String path) {
		return new File(path + File.separator + PROJECT_FILE_NAME + TEMP_FILE_EXTENSION);
	}

	protected File getClasspathFileRenameFile(String path) {
		return new File(path + File.separator + CLASSPATH_FILE_NAME + TEMP_FILE_EXTENSION);
	}

	protected File getClasspathFileFile(String path) {
		return new File(path + File.separator + CLASSPATH_FILE_NAME);
	}

	protected File getSettingsDirectoryRenameFile(String path) {
		return new File(path + File.separator + SETTINGS_DIRECTORY_NAME + TEMP_FILE_EXTENSION);
	}

	protected File getSettingsDirectoryFile(String path) {
		return new File(path + File.separator + SETTINGS_DIRECTORY_NAME);
	}

	protected void moveFile(File src, File dest) throws IOException {
		Files.move(src.toPath(), dest.toPath());
	}

	protected void removeDirectory(File directory) throws IOException {
		if (!directory.isDirectory()) {
			Files.delete(directory.toPath());
			return;
		}
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				removeDirectory(file);
			} else {
				Files.deleteIfExists(file.toPath());
			}
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

		private boolean existingProjectFileMoved = false;
		private boolean existingClasspathFileMoved = false;
		private boolean existingSettingsDirectoryMoved = false;
		private boolean cleanUpAlreadyDone = false;

		public EclipseProjectFileManagerStatus(String path) {
			this.path = path;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public boolean isExistingProjectFileMoved() {
			return existingProjectFileMoved;
		}

		public void setExistingProjectFileMoved(boolean existingProjectFileMoved) {
			this.existingProjectFileMoved = existingProjectFileMoved;
		}

		public boolean isExistingClasspathFileMoved() {
			return existingClasspathFileMoved;
		}

		public void setExistingClasspathFileMoved(boolean existingClasspathFileMoved) {
			this.existingClasspathFileMoved = existingClasspathFileMoved;
		}

		public boolean isExistingSettingsDirectoryMoved() {
			return existingSettingsDirectoryMoved;
		}

		public void setExistingSettingsDirectoryMoved(boolean existingSettingsDirectoryMoved) {
			this.existingSettingsDirectoryMoved = existingSettingsDirectoryMoved;
		}

		public boolean isCleanUpAlreadyDone() {
			return cleanUpAlreadyDone;
		}

		public void setCleanUpAlreadyDone(boolean cleanUpAlreadyDone) {
			this.cleanUpAlreadyDone = cleanUpAlreadyDone;
		}
	}
}
