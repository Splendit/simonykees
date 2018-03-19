package eu.jsparrow.adapter.i18n;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "eu.jsparrow.adapter.i18n.messages"; //$NON-NLS-1$

	public static String Adapter_embededMavenVersionDetected;
	
	public static String BundleStarter_equinoxStopped;

	public static String BundleStarter_loadOsgiBundles;

	public static String BundleStarter_startEquinox;

	public static String BundleStarter_startingBundle;

	public static String DependencyManager_extractAndCopyDependencies;

	public static String EmbeddedMaven_createDir;

	public static String EmbeddedMaven_fileUnzip;

	public static String EmbeddedMaven_unzipTemporaryMavenInstallation;

	public static String MavenAdapter_cannotReadJsparrowLockFile;

	public static String MavenAdapter_cannotWriteToJsparrowLockFile;

	public static String MavenAdapter_jsparrowConfigurationFile;

	public static String MavenAdapter_setUserDir;

	public static String StandaloneAdapter_adapterInstanceAlreadyCreated;

	public static String StandaloneAdapter_creatingAdapterInstance;

	public static String StandaloneAdapter_jsparrowAlreadyRunning;

	public static String StandaloneAdapter_mavenAdapterAlreadyCreated;

	public static String StandaloneAdapter_mavenAdapterInstanceNotCreated;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
