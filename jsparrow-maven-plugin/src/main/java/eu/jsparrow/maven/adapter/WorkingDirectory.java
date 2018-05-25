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

public class WorkingDirectory {

	private static final String LOCK_FILE_NAME = "lock"; //$NON-NLS-1$
	private static final String JAVA_TMP = "java.io.tmpdir"; //$NON-NLS-1$
	private static final String JSPARROW_TEMP_FOLDER = "temp_jSparrow"; //$NON-NLS-1$

	private File directory;
	private Set<String> sessionRelatedProjects;
	private Log log;

	public WorkingDirectory(File workingDirectory, Set<String> sessionRelatedProjects, Log log) {
		this.directory = workingDirectory;
		this.sessionRelatedProjects = sessionRelatedProjects;
		this.log = log;
	}

	/**
	 * Cleans classpath and temp directory
	 */
	public void cleanUp() {

		// CLEAN
		if (directory == null || !directory.exists()) {
			return;
		}

		try {
			deleteSessionRelatedFiles();
			boolean emptyLockFile = cleanLockFile();
			if (emptyLockFile) {
				deleteChildren(directory);
				Files.deleteIfExists(directory.toPath());
			}
		} catch (IOException e) {
			log.debug(e.getMessage(), e);
			log.error(e.getMessage());
		}
	}

	/**
	 * Deletes the children files related to the projects on the current
	 * session.
	 * 
	 * @param parentDirectory
	 *            the file representing the parent directory containing session
	 *            related files.
	 */
	private void deleteSessionRelatedFiles() {
		String[] children = directory.list();
		if (children == null) {
			return;
		}

		for (String file : children) {
			if (isSessionRelated(file)) {
				File currentFile = new File(directory.getAbsolutePath(), file);
				deleteChildren(currentFile);
				deleteIfExists(currentFile);
			}
		}
	}

	private void deleteIfExists(File currentFile) {
		try {
			Files.deleteIfExists(currentFile.toPath());
		} catch (IOException e) {
			log.debug(e.getMessage(), e);
			log.error(e.getMessage());
		}
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
			log.warn(Messages.MavenAdapter_cannotReadJSparrowLockFile, e);
		}

		try {
			Files.write(path, remainingContent.getBytes());
			return remainingContent.isEmpty();
		} catch (IOException e) {
			log.warn(Messages.MavenAdapter_cannotWriteToJSparrowLockFile, e);
		}

		return false;
	}

	/**
	 * Recursively deletes all sub-folders from received folder.
	 * 
	 * @param parentDirectory
	 *            directory which content is to be deleted
	 * @throws IOException
	 */
	private void deleteChildren(File parentDirectory) {
		String[] children = parentDirectory.list();
		if (children != null) {
			for (String file : children) {
				File currentFile = new File(parentDirectory.getAbsolutePath(), file);
				if (currentFile.isDirectory()) {
					deleteChildren(currentFile);
				}

				deleteIfExists(currentFile);
			}
		}
	}

	private boolean isSessionRelated(String file) {
		return sessionRelatedProjects.stream()
			.anyMatch(file::contains);
	}

	/**
	 * Checks whether the lock file contains the id of the given project.
	 * 
	 * @param mavenProject
	 *            a maven project to be checked.
	 * @return {@code true}if the lock file contains the project id, or
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
			log.warn(Messages.MavenAdapter_cannotReadJSparrowLockFile, e);
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
			log.warn(Messages.MavenAdapter_cannotWriteToJSparrowLockFile, e);
		}
	}

	protected String calculateJsparrowLockFilePath() {
		return calculateJsparrowTempFolderPath() + File.separator + LOCK_FILE_NAME;
	}

	public static String calculateJsparrowTempFolderPath() {
		String file = System.getProperty(JAVA_TMP);
		return file + File.separator + JSPARROW_TEMP_FOLDER;
	}
}
