package eu.jsparrow.license.netlicensing.cleanslate.validation;

import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseStatus;

public class ValidationStatus {

	private boolean valid;

	private LicenseStatus status;

	public boolean isValid() {
		return valid;
	}

	public LicenseStatus getStatus() {
		return this.status;
	}

	public ValidationStatus(boolean valid) {
		this.valid = valid;
	}

	public ValidationStatus(boolean valid, LicenseStatus status) {
		this(valid);
		this.status = status;
	}
}
