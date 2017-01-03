package at.splendit.simonykees.core.license;

public enum LicenseStatus {

	NONE,

	TRIAL_REGISTERED, TRIAL_EXPIRED, TRIAL_HW_ID_FAILURE,

	NODE_LOCKED_REGISTERED, NODE_LOCKED_EXPIRED, NODE_LOCKED_HW_ID_FAILURE,

	FLOATING_CHECKED_OUT, FLOATING_EXPIRED, FLOATING_OUT_OF_SESSION, FLOATING_CHECKED_IN,

	CONNECTION_FAILURE;

	@SuppressWarnings("nls")
	public static LicenseStatus fromString(String value) {
		LicenseStatus status = NONE;

		switch (value.toLowerCase()) {
		case "trial-registered":
			status = LicenseStatus.TRIAL_REGISTERED;
			break;
		case "trial-expired":
			status = LicenseStatus.TRIAL_EXPIRED;
			break;
		case "nodelocked-registered":
			status = LicenseStatus.NODE_LOCKED_REGISTERED;
			break;
		case "nodelocked-expired":
			status = LicenseStatus.NODE_LOCKED_EXPIRED;
			break;
		case "node-locked-hw-id-failure":
			status = NODE_LOCKED_HW_ID_FAILURE;
			break;
		case "floating-checked-out":
			status = LicenseStatus.FLOATING_CHECKED_OUT;
			break;
		case "floating-expired":
			status = LicenseStatus.FLOATING_EXPIRED;
			break;
		case "floating-out-of-session":
			status = LicenseStatus.FLOATING_OUT_OF_SESSION;
			break;
		case "floating-checked-in":
			status = LicenseStatus.FLOATING_CHECKED_IN;
			break;

		}

		return status;
	}
}
