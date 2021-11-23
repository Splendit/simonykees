package eu.jsparrow.license.netlicensing.validation;

import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.api.exception.ValidationException;

/**
 * Implementors validate the given licenses.
 */
public interface LicenseValidation {

	/**
	 * Perform a validation of this instance.
	 * 
	 * @return
	 * @throws ValidationException
	 */
	LicenseValidationResult validate() throws ValidationException;

	default void checkIn() throws ValidationException {
	}

	/**
	 * Reduces the given quantity from the available credit of a Pay-Per-Use
	 * license model. Has no effect in case the current license is not
	 * Pay-Per-Use.
	 * 
	 * @param quantity
	 *            quantity of credit to be reduced
	 * @throws ValidationException
	 */
	default void reserveQuantity(int quantity) throws ValidationException {
	}
}
