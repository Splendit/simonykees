package eu.jsparrow.license.netlicensing.validation.impl;

import java.time.ZonedDateTime;

import eu.jsparrow.license.netlicensing.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.model.DemoLicenseModel;
import eu.jsparrow.license.netlicensing.validation.LicenseValidation;
import eu.jsparrow.license.netlicensing.validation.ValidationStatus;

public class DemoLicenseValidation implements LicenseValidation {

	private DemoLicenseModel demoLicenseModel;
	
	public DemoLicenseValidation(DemoLicenseModel model) {
		this.demoLicenseModel = model;
	}

	@Override
	public LicenseValidationResult validate() {
		ZonedDateTime expirationDate = demoLicenseModel.getExpirationDate();
		ValidationStatus status = null;
		if(ZonedDateTime.now().isBefore(expirationDate)) {
			status = new  ValidationStatus(true);
		}
		else {
			status = new ValidationStatus(false);
		}
		return new LicenseValidationResult(demoLicenseModel, status);
	}

}
