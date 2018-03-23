package eu.jsparrow.license.netlicensing.cleanslate;

import eu.jsparrow.license.netlicensing.cleanslate.model.ValidationException;

public interface LicenseService {
	
	LicenseValidationResult updateLicense(String licenceKey);
	
	LicenseValidationResult validateLicense() throws ValidationException;

}
