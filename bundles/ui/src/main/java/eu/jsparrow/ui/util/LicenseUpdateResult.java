package eu.jsparrow.ui.util;

/**
 * This is a helper class. Only used to transport the result of an update
 * license and a detailed message if necessary.
 */
public class LicenseUpdateResult {

	private boolean wasSuccessful;

	private String detailMessage;

	public LicenseUpdateResult(boolean successful, String message) {
		wasSuccessful = successful;
		detailMessage = message;
	}

	public String getDetailMessage() {
		return detailMessage;
	}

	public boolean wasSuccessful() {
		return wasSuccessful;
	}

}
