package eu.jsparrow.license.netlicensing.validation;

import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.api.exception.ValidationException;

/**
 * Implementors validate the given licenses. 
 */
public interface LicenseValidation {
	
	/**
	 * Perform a validation of this instance. 
	 * @return 
	 * @throws ValidationException
	 */
	LicenseValidationResult validate() throws ValidationException;
	
	default void checkIn() throws ValidationException {}

	default void reserveQuantity(int quantity) throws ValidationException {}
}
