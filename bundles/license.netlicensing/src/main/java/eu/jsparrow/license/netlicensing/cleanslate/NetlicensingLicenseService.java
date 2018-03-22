package eu.jsparrow.license.netlicensing.cleanslate;

import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.validation.LicenseValidation;
import eu.jsparrow.license.netlicensing.cleanslate.validation.LicenseValidationFactory;

public class NetlicensingLicenseService implements LicenseService {

	private LicensePersistence persistence;
	private LicenseValidationFactory validationFactory;
	
	public NetlicensingLicenseService() {
		this.persistence = new SecureStoragePersistence();
		this.validationFactory = new LicenseValidationFactory();
	}
	
	@Override
	public LicenseValidationResult updateLicense(String licenceKey) {
		
		return null;
	}

	@Override
	public LicenseValidationResult validateLicense() {
		LicenseModel model = persistence.load();
		
		LicenseValidation validation = validationFactory.create(model);
		
		return validation.validate();
	}

}
