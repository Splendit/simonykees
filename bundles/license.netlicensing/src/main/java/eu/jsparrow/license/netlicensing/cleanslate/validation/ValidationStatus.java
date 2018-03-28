package eu.jsparrow.license.netlicensing.cleanslate.validation;

import eu.jsparrow.license.netlicensing.cleanslate.model.StatusDetail;

public class ValidationStatus {
	private boolean valid;

	private StatusDetail detail;

	public boolean isValid() {
		return valid;
	}

	public StatusDetail getStatusDetail() {
		return this.detail;
	}

	public ValidationStatus(boolean valid) {
		this.valid = valid;
	}

	public ValidationStatus(boolean valid, StatusDetail status) {
		this(valid);
		this.detail = status;
	}
}
