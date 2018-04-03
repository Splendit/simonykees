package eu.jsparrow.license.netlicensing.cleanslate;

import org.eclipse.equinox.security.storage.SecurePreferencesFactory;

import eu.jsparrow.license.netlicensing.cleanslate.exception.PersistenceException;
import eu.jsparrow.license.netlicensing.cleanslate.exception.ValidationException;
import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.persistence.AESEncryption;
import eu.jsparrow.license.netlicensing.cleanslate.persistence.SecureStoragePersistence;
import eu.jsparrow.license.netlicensing.cleanslate.validation.LicenseValidation;
import eu.jsparrow.license.netlicensing.cleanslate.validation.LicenseValidationFactory;

public class NetlicensingLicenseService implements LicenseService {

	private LicensePersistence persistence;
	private LicenseValidationFactory validationFactory;

	public NetlicensingLicenseService() {
		this.persistence = new SecureStoragePersistence(SecurePreferencesFactory.getDefault(), new AESEncryption());
		this.validationFactory = new LicenseValidationFactory();
	}

	@Override
	public LicenseValidationResult validate(LicenseModel model) throws ValidationException {
		LicenseValidation validation = validationFactory.create(model);

		return validation.validate();
	}

	@Override
	public LicenseModel loadFromPersistence() throws PersistenceException {
		return persistence.load();
	}

	@Override
	public void saveToPersistence(LicenseModel model) throws PersistenceException {
		persistence.save(model);
	}

	@Override
	public void checkIn(LicenseModel licenseModel) throws ValidationException{
		LicenseValidation validation = validationFactory.create(licenseModel);

		// TODO: Move this out of validation, or refactor validation
		// License management (checkin, checkout, invalidate...) is not the same as validation.
		validation.checkIn();
	}

}
