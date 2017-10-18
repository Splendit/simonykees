package eu.jsparrow.license.netlicensing;

import eu.jsparrow.i18n.Messages;

/**
 * Enumeration for indicating the reason why the license is or is not valid.
 * 
 * @author Ardit Ymeri, Ludwig Werzowa
 * @since 1.0
 *
 */
public enum LicenseStatus {

	NONE(Messages.LicenseStatus_userMessage_NONE),
	FREE_REGISTERED(Messages.LicenseStatus_userMessage_FREE_REGISTERED),
	FREE_EXPIRED(Messages.LicenseStatus_userMessage_FREE_EXPIRED),
	FREE_HW_ID_FAILURE(Messages.LicenseStatus_userMessage_FREE_HW_ID_FAILURE),
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
			case "free-registered": //$NON-NLS-1$
				status = LicenseStatus.FREE_REGISTERED;
				break;
			case "free-expired": //$NON-NLS-1$
				status = LicenseStatus.FREE_EXPIRED;
				break;
			case "free-hw-id-failure": //$NON-NLS-1$
				status = LicenseStatus.FREE_HW_ID_FAILURE;
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
			default:
				status = NONE;
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
