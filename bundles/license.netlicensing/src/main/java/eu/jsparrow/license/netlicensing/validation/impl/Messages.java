package eu.jsparrow.license.netlicensing.validation.impl;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "eu.jsparrow.license.netlicensing.validation.impl.messages"; //$NON-NLS-1$
	public static String Netlicensing_validationResult_freeLicenseExpired1;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
