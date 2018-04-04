package eu.jsparrow.license.netlicensing.validation.impl;

import java.time.ZonedDateTime;

import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.model.DemoLicenseModel;
import eu.jsparrow.license.netlicensing.validation.LicenseValidation;

public class DemoLicenseValidation implements LicenseValidation {

	private DemoLicenseModel demoLicenseModel;

	public DemoLicenseValidation(DemoLicenseModel model) {
		this.demoLicenseModel = model;
	}

	@Override
	public LicenseValidationResult validate() {
		ZonedDateTime expirationDate = demoLicenseModel.getExpirationDate();
		boolean valid = ZonedDateTime.now()
			.isBefore(expirationDate);
		String detail = "";
		if (!valid) {
			detail = "The free license has expired.";
		}
		return new LicenseValidationResult(demoLicenseModel, valid, detail);
	}

}
