package eu.jsparrow.license.netlicensing.cleanslate.model;

import eu.jsparrow.i18n.Messages;

public enum LicenseStatus {

	DEMO(Messages.LicenseStatus_userMessage_FREE_REGISTERED),
	DEMO_EXPIRED(Messages.LicenseStatus_userMessage_FREE_EXPIRED),

	NODE_LOCKED(Messages.LicenseStatus_userMessage_NODE_LOCKED_REGISTERED),
	NODE_LOCKED_EXPIRED(Messages.LicenseStatus_userMessage_NODE_LOCKED_EXPIRED),
	NODE_LOCKED_HARDWARE_MISMATCH(Messages.LicenseStatus_userMessage_NODE_LOCKED_HW_ID_FAILURE),

	FLOATING(Messages.LicenseStatus_userMessage_FLOATING_CHECKED_OUT),
	FLOATING_EXPIRED(Messages.LicenseStatus_userMessage_FLOATING_EXPIRED),
	FLOATING_OUT_OF_SESSIONS(Messages.LicenseStatus_userMessage_FLOATING_OUT_OF_SESSION),
	FLOATING_CHECKED_IN(Messages.LicenseStatus_userMessage_FLOATING_CHECKED_IN),

	UNDEFINED(Messages.LicenseStatus_userMessage_NONE),
	CONNECTION_FAILURE(Messages.LicenseStatus_userMessage_CONNECTION_FAILURE);

	private String userMessage;

	private LicenseStatus(String userMessage) {
		this.userMessage = userMessage;
	}

	public String getUserMessage() {
		return this.userMessage;
	}

	public static LicenseStatus fromString(String value) {
		LicenseStatus status = UNDEFINED;

		if (value != null) {
			switch (value.toLowerCase()) {
			case "conncection-failure": //$NON-NLS-1$
				status = LicenseStatus.CONNECTION_FAILURE;
				break;
			case "free-registered": //$NON-NLS-1$
				status = LicenseStatus.DEMO;
				break;
			case "free-expired": //$NON-NLS-1$
				status = LicenseStatus.DEMO_EXPIRED;
				break;
			case "node-locked-registered": //$NON-NLS-1$
				status = LicenseStatus.NODE_LOCKED;
				break;
			case "node-locked-expired": //$NON-NLS-1$
				status = LicenseStatus.NODE_LOCKED_EXPIRED;
				break;
			case "node-locked-hw-id-failure": //$NON-NLS-1$
				status = NODE_LOCKED_HARDWARE_MISMATCH;
				break;
			case "floating-checked-out": //$NON-NLS-1$
				status = LicenseStatus.FLOATING;
				break;
			case "floating-expired": //$NON-NLS-1$
				status = LicenseStatus.FLOATING_EXPIRED;
				break;
			case "floating-out-of-session": //$NON-NLS-1$
				status = LicenseStatus.FLOATING_OUT_OF_SESSIONS;
				break;
			case "floating-checked-in": //$NON-NLS-1$
				status = LicenseStatus.FLOATING_CHECKED_IN;
				break;
			default:
				status = UNDEFINED;
			}
		}

		return status;
	}
}
