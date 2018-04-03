package eu.jsparrow.license.netlicensing.validation;

import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.api.exception.ValidationException;

public interface LicenseValidation {
	
	LicenseValidationResult validate() throws ValidationException;
	
	default void checkIn() throws ValidationException {}

}
