package eu.jsparrow.license.netlicensing.validation;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.netlicensing.model.DemoLicenseModel;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.validation.impl.*;

/**
 * This class provides {@link LicenseValidation} instances based on
 * {@link LicenseModel}.
 */
public class LicenseValidationFactory {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	/**
	 * Returns a {@link LicenseValidation} for the given {@link LicenseModel}
	 * 
	 * @param model
	 *            the model to get a validation for
	 * @return the validation for the given model
	 */

	public LicenseValidation create(LicenseModel model) {
		return create(model, ""); //$NON-NLS-1$
	}
	
	@SuppressWarnings("nls")
	public LicenseValidation create(LicenseModel model, String endpoint) {
		logger.debug("Create new validation for model {}", model);
		if (model instanceof NetlicensingLicenseModel) {
			logger.debug("Creating new validation {}", NetlicensingLicenseValidation.class);
			return new NetlicensingLicenseValidation((NetlicensingLicenseModel) model, endpoint);
		} else if (model instanceof DemoLicenseModel) {
			logger.debug("Creating new validation {}", NetlicensingLicenseValidation.class);
			return new DemoLicenseValidation((DemoLicenseModel) model);
		}
		logger.debug("No validation created for model, returning null");
		return null;
	}

}
