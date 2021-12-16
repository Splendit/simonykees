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

	/**
	 * Verifies if the given secret is valid for the license model.
	 * 
	 * @param model
	 *            model to be verified
	 * @param secret
	 *            expected secret key.
	 * @return if the license is node locked and the given secret key matches
	 *         with the model's secret.
	 */
	public boolean verifySecretKey(LicenseModel model, String secret);

	/**
	 * Reduces the provided quantity in the available credit of the given
	 * license. Has no effect if the given license is not a Pay-Per-Use license
	 * model.
	 * 
	 * @param model
	 *            an instance of a Pay-Per-Use license model.
	 * @param quantity
	 *            the quantity to be reduced
	 * @param endpoint
	 *            the license server url. Use empty for default.
	 * @throws ValidationException
	 *             if the validation could not be performed
	 */
	public void reserveQuantity(LicenseModel model, int quantity, String endpoint) throws ValidationException;

}
