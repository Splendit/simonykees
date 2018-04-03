package eu.jsparrow.license.netlicensing;

import eu.jsparrow.license.netlicensing.model.LicenseModel;
import eu.jsparrow.license.netlicensing.validation.ValidationStatus;

public class LicenseValidationResult {

	private ValidationStatus validationStatus;

	private LicenseModel model;

	public LicenseValidationResult(LicenseModel model, ValidationStatus validationStatus) {
		this.model = model;
		this.validationStatus = validationStatus;
	}

	public LicenseModel getModel() {
		return model;
	}

	public ValidationStatus getStatus() {
		return validationStatus;
	}

}
