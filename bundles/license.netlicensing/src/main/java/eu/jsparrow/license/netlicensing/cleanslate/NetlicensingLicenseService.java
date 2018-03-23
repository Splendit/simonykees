package eu.jsparrow.license.netlicensing.cleanslate;

import org.eclipse.equinox.security.storage.SecurePreferencesFactory;

import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.model.ValidationException;
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
	public LicenseValidationResult updateLicense(String licenceKey) {
		
		return null;
	}

	@Override
	public LicenseValidationResult validateLicense() throws ValidationException {
		LicenseModel model = persistence.load();
		
		LicenseValidation validation = validationFactory.create(model);
		
		return validation.validate();
	}

}
