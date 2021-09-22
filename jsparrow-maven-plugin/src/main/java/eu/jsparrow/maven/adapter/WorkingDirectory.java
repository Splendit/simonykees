package eu.jsparrow.maven.adapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.plugin.logging.Log;

import eu.jsparrow.maven.i18n.Messages;

/**
 * Provides functionalities for cleaning the working directory and checking on
 * which projects the standalone is already running.
 *
 * @since 2.6.0
 */
public class WorkingDirectory {

	private static final String LOCK_FILE_NAME = "lock"; //$NON-NLS-1$
	private static final String JAVA_TMP = "java.io.tmpdir"; //$NON-NLS-1$
	private static final String JSPARROW_TEMP_FOLDER = "temp_jSparrow"; //$NON-NLS-1$

	private File directory;
	private Set<String> sessionRelatedProjects;
	private Log log;
	private String tempWorkspacePath;

	public WorkingDirectory(File workingDirectory, Set<String> sessionRelatedProjects, String tempWorkspacePath,
			Log log) {
		this.directory = workingDirectory;
		this.sessionRelatedProjects = sessionRelatedProjects;
		this.log = log;
		this.tempWorkspacePath = tempWorkspacePath;

	}

	/**
	 * Cleans the lock file and deletes the working directory. If the resulting
	 * lock file is empty, deletes the entire working directory and its
	 * contents.
	 */
	public void cleanUp() {

		boolean emptyLockFile = cleanLockFile();
		if (!emptyLockFile) {
			return;
		}

		/*
		 * Since we use File::deleteOnExit to remove the contents of directory,
		 * the files are not immediately removed, but the requests for deleting
		 * files are registered only to be executed when the virtual machine
		 * terminates. The execution is performed in reversed order, therefore
		 * the request for deleting parent directory should be registered before
		 * the one for deleting its children.
		 */
		deleteOnExit(directory);
		deleteChildrenOnExit(directory);
	}

	/**
	 * Requests the file to be deleted when the virtual machine terminates.
	 * Files are deleted in reversed order that they were requested.
	 * 
	 * @see {@link File#deleteOnExit()}
	 * 
	 * @param file
	 *            file to be deleted
	 */
	private void deleteOnExit(File file) {
		/*
		 * On windows, some of the OSGi related files could not be deleted
		 * because they were still being used by other processes. Therefore, the
		 * File::deleteOnExit is used instead of Files.deleteIfExist.
		 */
		file.deleteOnExit();
	}

	/**
	 * Removes the lines in the lock file that are related to the projects of
	 * the given session.
	 * 
	 * @return if the resulting content of the lock file is empty.
	 */
	private boolean cleanLockFile() {
		Path path = Paths.get(calculateJsparrowLockFilePath());

		if (!path.toFile()
			.exists()) {
			return true;
		}

		String remainingContent = ""; //$NON-NLS-1$
		try (Stream<String> linesStream = Files.lines(path)) {
			remainingContent = linesStream.filter(id -> !sessionRelatedProjects.contains(id))
				.collect(Collectors.joining("\n")) //$NON-NLS-1$
				.trim();

		} catch (IOException e) {
			log.warn(Messages.WorkingDirectory_cannotReadJSparrowLockFile, e);
		}

		try {
			Files.write(path, remainingContent.getBytes());
			return remainingContent.isEmpty();
		} catch (IOException e) {
			log.warn(Messages.WorkingDirectory_cannotWriteToJSparrowLockFile, e);
		}

		return false;
	}

	/**
	 * Recursively deletes all sub-folders from received folder.
	 * 
	 * @param parentDirectory
	 *            directory which content is to be deleted
	 */
	private void deleteChildrenOnExit(File parentDirectory) {
		String[] children = parentDirectory.list();
		if (children == null) {
			return;
		}
		for (String file : children) {
			File currentFile = new File(parentDirectory.getAbsolutePath(), file);
			deleteOnExit(currentFile);
			if (currentFile.isDirectory()) {
				deleteChildrenOnExit(currentFile);
			}
		}
	}

	/**
	 * Checks whether the lock file contains the id of the given project.
	 * 
	 * @param mavenProject
	 *            a maven project to be checked.
	 * @return {@code true} if the lock file contains the project id, or
	 *         {@code false} otherwise.
	 */
	public boolean isJsparrowStarted(String projectId) {
		Path path = Paths.get(calculateJsparrowLockFilePath());

		if (!path.toFile()
			.exists()) {
			return false;
		}
		try (Stream<String> linesStream = Files.lines(path)) {
			return linesStream.anyMatch(projectId::equals);
		} catch (IOException e) {
			log.warn(Messages.WorkingDirectory_cannotReadJSparrowLockFile, e);
		}

		return false;
	}

	/**
	 * Appends the project id-s of the current session in the lock file. Creates
	 * the lock file it does not exist. Uses
	 * {@link #calculateJsparrowLockFilePath()} for computing the path of the
	 * lock file.
	 */
	public synchronized void lockProjects() {
		String lockFilePath = calculateJsparrowLockFilePath();
		Path path = Paths.get(lockFilePath);
		String conntent = sessionRelatedProjects.stream()
			.collect(Collectors.joining("\n", "\n", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		try {
			Files.write(path, conntent.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			log.warn(Messages.WorkingDirectory_cannotWriteToJSparrowLockFile, e);
		}
	}

	protected String calculateJsparrowLockFilePath() {
		return calculateJsparrowTempFolderPath(tempWorkspacePath) + File.separator + LOCK_FILE_NAME;
	}

	public static String calculateJsparrowTempFolderPath(String tempWorkspacePath) {
		String file;
		if (tempWorkspacePath == null || tempWorkspacePath.isEmpty()) {
			file = System.getProperty(JAVA_TMP);
		} else {
			file = tempWorkspacePath;
		}
		return file + File.separator + JSPARROW_TEMP_FOLDER;
	}
}
