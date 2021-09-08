package eu.jsparrow.maven.i18n;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "eu.jsparrow.maven.i18n.messages"; //$NON-NLS-1$
	public static String MavenAdapter_jSparrowIsAlreadyRunning;
	public static String BundleStarter_equinoxStopped;
	public static String BundleStarter_loadOsgiBundles;
	public static String BundleStarter_startEquinox;
	public static String BundleStarter_startingBundle;
	public static String ListAllRulesMojo_supportJDK11;
	public static String WorkingDirectory_cannotReadJSparrowLockFile;
	public static String WorkingDirectory_cannotWriteToJSparrowLockFile;
	public static String MavenAdapter_configurationSetUp;
	public static String MavenAdapter_couldnotCreateTempFolder;
	public static String MavenAdapter_setProjectIds;
	public static String MavenAdapter_setUpConfiguration;
	public static String MavenAdapter_setUserDir;
	public static String MavenAdapter_jSparrowAlreadyRunning;
	public static String MavenAdapter_prepareWorkingDirectory;
	public static String MavenAdapter_projectIdsSet;
	public static String MavenAdapter_workingDirectoryPrepared;
	public static String RefactorMojo_supportJDK8and11;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
