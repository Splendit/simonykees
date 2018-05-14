package eu.jsparrow.license.netlicensing;

import java.lang.invoke.MethodHandles;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.LicenseService;
import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.license.netlicensing.validation.LicenseValidation;
import eu.jsparrow.license.netlicensing.validation.LicenseValidationFactory;

/**
 * Implementor of {@link LicenseService} using <a href="http://www.netlicensing.io">NetLicensing</a>
 */
@Component
@SuppressWarnings("nls")
public class NetlicensingLicenseService implements LicenseService {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private LicenseValidationFactory validationFactory;

	public NetlicensingLicenseService() {
		this.validationFactory = new LicenseValidationFactory();
	}

	@Override
	public LicenseValidationResult validate(LicenseModel model) throws ValidationException {
		logger.debug("Validating {}", model);
		LicenseValidation validation = validationFactory.create(model);
		return validation.validate();
	}

	@Override
	public void checkIn(LicenseModel licenseModel) throws ValidationException {
		logger.debug("Checkin {}", licenseModel);
		LicenseValidation validation = validationFactory.create(licenseModel);

		validation.checkIn();
	}

}
