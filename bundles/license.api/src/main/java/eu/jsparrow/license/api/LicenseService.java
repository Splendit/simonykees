package eu.jsparrow.license.api;

import eu.jsparrow.license.api.exception.ValidationException;

/**
 * Implementors can be used to perform various actions related to licenses.
 */
public interface LicenseService {

	/**
	 * Validates the given {@link LicenseModel}.
	 * 
	 * @param model
	 *            the license model to validate.
	 * @param endpoint
	 *            the license server url. Use empty for default.
	 * @return a {@link LicenseValidationResult}
	 * @throws ValidationException
	 *             if the validation could not be performed
	 */
	public LicenseValidationResult validate(LicenseModel model, String endpoint) throws ValidationException;

	/**
	 * Check in the given {@link LicenseModel}
	 * 
	 * @param model
	 *            the license model to check in
	 * @param endpoint
	 *            the license server url. Use empty for default.
	 * @throws ValidationException
	 *             if the checkIn could not be performed
	 */
	public void checkIn(LicenseModel model, String endpoint) throws ValidationException;

}
