package eu.jsparrow.license.netlicensing.validation.impl;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.model.DemoLicenseModel;
import eu.jsparrow.license.netlicensing.validation.LicenseValidation;

/**
 * Implements {@link LicenseValidation} for demo licenses. A demo license is
 * valid only if it is not expired.
 */
public class DemoLicenseValidation implements LicenseValidation {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private DemoLicenseModel demoLicenseModel;

	public DemoLicenseValidation(DemoLicenseModel model) {
		this.demoLicenseModel = model;
	}

	@Override
	public LicenseValidationResult validate() {
		logger.debug("Start validating demo license"); //$NON-NLS-1$
		ZonedDateTime expirationDate = demoLicenseModel.getExpirationDate();
		boolean valid = ZonedDateTime.now()
			.isBefore(expirationDate);
		String detail = ""; //$NON-NLS-1$
		if (!valid) {
			detail = Messages.Netlicensing_validationResult_freeLicenseExpired0;
		}
		LicenseValidationResult result = new LicenseValidationResult(demoLicenseModel, null, valid, detail);
		logger.debug("Returning {}", result); //$NON-NLS-1$
		return result;
	}

}
