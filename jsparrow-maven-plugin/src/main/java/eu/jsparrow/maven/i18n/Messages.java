package eu.jsparrow.maven.i18n;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "eu.jsparrow.maven.i18n.messages"; //$NON-NLS-1$
	public static String Mojo_jSparrowIsAlreadyRunning;
	public static String RefactorMojo_allProjectsLoaded;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
