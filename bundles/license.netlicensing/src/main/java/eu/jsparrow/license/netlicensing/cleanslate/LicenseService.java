package eu.jsparrow.license.netlicensing.cleanslate;

public interface LicenseService {
	
	LicenseValidationResult updateLicense(String licenceKey);
	
	LicenseValidationResult validateLicense();

}
