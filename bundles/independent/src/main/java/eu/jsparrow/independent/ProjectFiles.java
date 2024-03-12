package eu.jsparrow.independent;

@SuppressWarnings("nls")
public enum ProjectFiles {

	PROJECT_DESCRIPTION_FILE(".project", false),
	CLASS_PATH_FILE(".classpath", false),
	SETTINGS_DIRECTORY(".settings", true),
	BIN_DIRECTORY("bin", true),
	LIB_DIRECTORY("lib", true);
	/*
	 * TODO: consider also "bin"- and "lib" directories
	 */

	private static final String TEMP_FILE_EXTENSION = ".tmp"; //$NON-NLS-1$

	private final String fileName;
	private final boolean isDirectory;

	private ProjectFiles(String fileName, boolean isDirectory) {
		this.fileName = fileName;
		this.isDirectory = isDirectory;
	}

	public String getFileName() {
		return fileName;
	}

	public String getBackupFileName() {
		return fileName + TEMP_FILE_EXTENSION;
	}

	public boolean isDirectory() {
		return isDirectory;
	}
}
