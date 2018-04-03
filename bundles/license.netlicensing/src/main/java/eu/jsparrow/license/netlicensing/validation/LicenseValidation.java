package eu.jsparrow.license.netlicensing.validation;

import eu.jsparrow.license.netlicensing.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.exception.ValidationException;

public interface LicenseValidation {
	
	LicenseValidationResult validate() throws ValidationException;
	
	default void checkIn() throws ValidationException {}

}
