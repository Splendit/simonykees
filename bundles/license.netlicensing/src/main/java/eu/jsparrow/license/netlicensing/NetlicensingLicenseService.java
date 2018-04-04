package eu.jsparrow.license.netlicensing;

import java.lang.invoke.MethodHandles;

import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.api.*;
import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.license.netlicensing.persistence.AESEncryption;
import eu.jsparrow.license.netlicensing.persistence.SecureStoragePersistence;
import eu.jsparrow.license.netlicensing.validation.LicenseValidation;
import eu.jsparrow.license.netlicensing.validation.LicenseValidationFactory;

@Component
@SuppressWarnings("nls")
public class NetlicensingLicenseService implements LicenseService {
	
	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
			.lookupClass());

	private LicensePersistence persistence;
	
	private LicenseValidationFactory validationFactory;

	public NetlicensingLicenseService() {
		this.persistence = new SecureStoragePersistence(SecurePreferencesFactory.getDefault(), new AESEncryption());
		this.validationFactory = new LicenseValidationFactory();
	}

	@Override
	public LicenseValidationResult validate(LicenseModel model) throws ValidationException {
		logger.debug("Validating {}", model);
		LicenseValidation validation = validationFactory.create(model);
		return validation.validate();
	}

	@Override
	public LicenseModel loadFromPersistence() throws PersistenceException {
		logger.debug("Loading model from persistence");
		return persistence.load();
	}

	@Override
	public void saveToPersistence(LicenseModel model) throws PersistenceException {
		logger.debug("Saving {}", model);
		persistence.save(model);
	}

	@Override
	public void checkIn(LicenseModel licenseModel) throws ValidationException{
		logger.debug("Checkin {}", licenseModel);
		LicenseValidation validation = validationFactory.create(licenseModel);

		validation.checkIn();
	}

}
