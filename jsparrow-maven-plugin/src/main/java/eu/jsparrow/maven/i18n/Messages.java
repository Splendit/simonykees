package eu.jsparrow.maven.i18n;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "eu.jsparrow.maven.i18n.messages"; //$NON-NLS-1$
	public static String Mojo_jSparrowIsAlreadyRunning;
	public static String RefactorMojo_allProjectsLoaded;
	public static String Adapter_embededMavenVersionDetected;

	public static String BundleStarter_equinoxStopped;

	public static String BundleStarter_loadOsgiBundles;

	public static String BundleStarter_startEquinox;

	public static String BundleStarter_startingBundle;

	public static String DependencyManager_extractAndCopyDependencies;

	public static String EmbeddedMaven_createDir;

	public static String EmbeddedMaven_fileUnzip;

	public static String EmbeddedMaven_unzipTemporaryMavenInstallation;

	public static String MavenAdapter_addingProjectConfiguration;

	public static String MavenAdapter_cannotReadJSparrowLockFile;

	public static String MavenAdapter_cannotWriteToJSparrowLockFile;

	public static String MavenAdapter_couldnotCreateTempFolder;

	public static String MavenAdapter_jSparrowConfigurationFile;

	public static String MavenAdapter_setUserDir;

	public static String StandaloneAdapter_adapterInstanceAlreadyCreated;

	public static String StandaloneAdapter_creatingAdapterInstance;

	public static String StandaloneAdapter_jSparrowAlreadyRunning;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
