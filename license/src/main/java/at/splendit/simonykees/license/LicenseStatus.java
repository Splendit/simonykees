package at.splendit.simonykees.license;

import at.splendit.simonykees.license.i18n.Messages; //TODO: add i18n support

/**
 * Enumeration for indicating the reason why the license is or is not valid.
 * 
 * @author Ardit Ymeri, Ludwig Werzowa
 * @since 1.0
 *
 */
public enum LicenseStatus {

	NONE(Messages.LicenseStatus_userMessage_NONE),
	TRIAL_REGISTERED(Messages.LicenseStatus_userMessage_TRIAL_REGISTERED), 
	TRIAL_EXPIRED(Messages.LicenseStatus_userMessage_TRIAL_EXPIRED), 
	TRIAL_HW_ID_FAILURE(Messages.LicenseStatus_userMessage_TRIAL_HW_ID_FAILURE),
	NODE_LOCKED_REGISTERED(Messages.LicenseStatus_userMessage_NODE_LOCKED_REGISTERED), 
	NODE_LOCKED_EXPIRED(Messages.LicenseStatus_userMessage_NODE_LOCKED_EXPIRED), 
	NODE_LOCKED_HW_ID_FAILURE(Messages.LicenseStatus_userMessage_NODE_LOCKED_HW_ID_FAILURE),
	FLOATING_CHECKED_OUT(Messages.LicenseStatus_userMessage_FLOATING_CHECKED_OUT), 
	FLOATING_EXPIRED(Messages.LicenseStatus_userMessage_FLOATING_EXPIRED), 
	FLOATING_OUT_OF_SESSION(Messages.LicenseStatus_userMessage_FLOATING_OUT_OF_SESSION), 
	FLOATING_CHECKED_IN(Messages.LicenseStatus_userMessage_FLOATING_CHECKED_IN),
	CONNECTION_FAILURE_UNREGISTERED(Messages.LicenseStatus_userMessage_CONNECTION_FAILURE_UNREGISTERED),
	CONNECTION_FAILURE(Messages.LicenseStatus_userMessage_CONNECTION_FAILURE);

	public static LicenseStatus fromString(String value) {
		LicenseStatus status = NONE;

		if (value != null) {
			switch (value.toLowerCase()) {
			case "conncection-failure": //$NON-NLS-1$
				status = LicenseStatus.CONNECTION_FAILURE;
				break;
			case "trial-registered": //$NON-NLS-1$
				status = LicenseStatus.TRIAL_REGISTERED;
				break;
			case "trial-expired": //$NON-NLS-1$
				status = LicenseStatus.TRIAL_EXPIRED;
				break;
			case "trial-hw-id-failure": //$NON-NLS-1$
				status = LicenseStatus.TRIAL_HW_ID_FAILURE;
				break;
			case "node-locked-registered": //$NON-NLS-1$
				status = LicenseStatus.NODE_LOCKED_REGISTERED;
				break;
			case "node-locked-expired": //$NON-NLS-1$
				status = LicenseStatus.NODE_LOCKED_EXPIRED;
				break;
			case "node-locked-hw-id-failure": //$NON-NLS-1$
				status = NODE_LOCKED_HW_ID_FAILURE;
				break;
			case "floating-checked-out": //$NON-NLS-1$
				status = LicenseStatus.FLOATING_CHECKED_OUT;
				break;
			case "floating-expired": //$NON-NLS-1$
				status = LicenseStatus.FLOATING_EXPIRED;
				break;
			case "floating-out-of-session": //$NON-NLS-1$
				status = LicenseStatus.FLOATING_OUT_OF_SESSION;
				break;
			case "floating-checked-in": //$NON-NLS-1$
				status = LicenseStatus.FLOATING_CHECKED_IN;
				break;
			}
		}

		return status;
	}

	private LicenseStatus(String userMessage) {
		this.userMessage = userMessage;
	}

	private String userMessage;

	public String getUserMessage() {
		return this.userMessage;
	}
}
