package eu.jsparrow.license.api;

import eu.jsparrow.license.api.exception.ValidationException;

public interface LicenseService {

	public LicenseValidationResult validate(LicenseModel model) throws ValidationException;
	
	public void checkIn(LicenseModel model) throws ValidationException;
	
	public LicenseValidationResult verifyKey(String key, String secret) throws ValidationException;
}
