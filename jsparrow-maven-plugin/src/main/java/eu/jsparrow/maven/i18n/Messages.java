package eu.jsparrow.maven.i18n;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "eu.jsparrow.maven.i18n.messages"; //$NON-NLS-1$
	public static String ListAllRulesMojo_jsparrowAlreadyRunning;
	public static String ListAllRulesShortMojo_jsparrowAlreadyRunning;
	public static String RefactorMojo_allProjectsLoaded;
	public static String RefactorMojo_jsparrowAlreadyRunning;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
