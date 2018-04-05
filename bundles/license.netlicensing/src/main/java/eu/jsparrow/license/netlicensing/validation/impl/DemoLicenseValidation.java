package eu.jsparrow.license.netlicensing.validation.impl;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.model.DemoLicenseModel;
import eu.jsparrow.license.netlicensing.validation.LicenseValidation;

public class DemoLicenseValidation implements LicenseValidation {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private DemoLicenseModel demoLicenseModel;

	public DemoLicenseValidation(DemoLicenseModel model) {
		this.demoLicenseModel = model;
	}

	@Override
	@SuppressWarnings("nls")
	public LicenseValidationResult validate() {
		logger.debug("Start validating demo license");
		ZonedDateTime expirationDate = demoLicenseModel.getExpirationDate();
		boolean valid = ZonedDateTime.now().isBefore(expirationDate);
		String detail = "";
		if (!valid) {
			detail = "The free license has expired.";
		}
		LicenseValidationResult result = new LicenseValidationResult(demoLicenseModel, null, valid, detail);
		logger.debug("Returning {}", result);
		return result;
	}

}
