package at.splendit.simonykees.core.i18n;

import org.eclipse.osgi.util.NLS;

public class ExceptionMessages extends NLS {
	private static final String BUNDLE_NAME = "at.splendit.simonykees.core.i18n.exceptionMessages"; //$NON-NLS-1$
	public static String SimonykeesException_default_ui_message;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ExceptionMessages.class);
	}

	private ExceptionMessages() {
	}
}
