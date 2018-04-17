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
	 * @return a {@link LicenseValidationResult}
	 * @throws ValidationException
	 *             if the validation could not be performed
	 */
	public LicenseValidationResult validate(LicenseModel model) throws ValidationException;

	/**
	 * Check in the given {@link LicenseModel}
	 * 
	 * @param model
	 *            the license model to check in
	 * @throws ValidationException
	 *             if the checkIn could not be performed
	 */
	public void checkIn(LicenseModel model) throws ValidationException;

	/**
	 * Validate the given key and secret. This method is used if we don't know
	 * if we have a floating or node locked license yet.
	 * 
	 * @param key the key to validate
	 * @param secret the secret to validate
	 * @return a license validation result
	 * @throws ValidationException if the validate could not be performed
	 */
	public LicenseValidationResult verifyKey(String key, String secret) throws ValidationException;
}
