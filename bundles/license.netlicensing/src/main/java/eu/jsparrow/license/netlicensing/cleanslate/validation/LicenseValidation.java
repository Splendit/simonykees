package eu.jsparrow.license.netlicensing.cleanslate.validation;

import eu.jsparrow.license.netlicensing.cleanslate.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.cleanslate.exception.ValidationException;

public interface LicenseValidation {
	
	LicenseValidationResult validate() throws ValidationException;
	
	default void checkIn() throws ValidationException {}

}
