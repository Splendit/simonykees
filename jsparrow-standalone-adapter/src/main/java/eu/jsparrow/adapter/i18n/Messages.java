package eu.jsparrow.adapter.i18n;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "eu.jsparrow.adapter.i18n.messages"; //$NON-NLS-1$
	public static String Adapter_prepareConfiguration;
	public static String Adapter_embededMavenVersionDetected;
	public static String Adapter_equinoxStopped;
	public static String Adapter_extractAndCopyDependencies;
	public static String Adapter_loadOSGiBundles;
	public static String Adapter_setUserDirTo;
	public static String Adapter_start_equinox;
	public static String Adapter_startingBundle;
	public static String Adapter_unzipTemporaryMavenInstallation;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
