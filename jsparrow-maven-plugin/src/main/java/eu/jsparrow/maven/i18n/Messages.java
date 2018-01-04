package eu.jsparrow.maven.i18n;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "eu.jsparrow.maven.util.messages"; //$NON-NLS-1$
	public static String MavenHelper_CleanTemporaryDirectories;
	public static String MavenHelper_CouldNotCreateTempFolder;
	public static String MavenHelper_CreateDir;
	public static String MavenHelper_EmbeddedMavenDetected;
	public static String MavenHelper_EquinoxStopped;
	public static String MavenHelper_ExtractAndCopyDependencies;
	public static String MavenHelper_FileUnzip;
	public static String MavenHelper_jSparrowIsAlreadyRunning;
	public static String MavenHelper_LoadOsgiBundles;
	public static String MavenHelper_PrepareConfiguration;
	public static String MavenHelper_Session;
	public static String MavenHelper_SetUserDirTo;
	public static String MavenHelper_StartEquinox;
	public static String MavenHelper_StartingBundle;
	public static String MavenHelper_UnzipTemporaryMavenInstallation;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
