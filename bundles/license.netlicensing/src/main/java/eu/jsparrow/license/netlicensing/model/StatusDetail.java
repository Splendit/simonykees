package eu.jsparrow.license.netlicensing.model;

import eu.jsparrow.i18n.Messages;

public enum StatusDetail {

	DEMO(Messages.LicenseStatus_userMessage_FREE_REGISTERED),
	DEMO_EXPIRED(Messages.LicenseStatus_userMessage_FREE_EXPIRED),

	PAY_PER_USE(Messages.StatusDetail_PAY_PER_USE_REGISTERED),
	PAY_PER_USE_OUT_OF_CREDIT(Messages.StatusDetail_PAY_PER_USE_outOfCredit),
	
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

	private StatusDetail(String userMessage) {
		this.userMessage = userMessage;
	}

	public String getUserMessage() {
		return this.userMessage;
	}
}
