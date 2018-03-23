package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import java.time.ZonedDateTime;

import eu.jsparrow.license.netlicensing.cleanslate.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.cleanslate.model.DemoLicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.validation.LicenseValidation;
import eu.jsparrow.license.netlicensing.cleanslate.validation.ValidationStatus;

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
