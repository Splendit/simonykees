package eu.jsparrow.license.api;

import eu.jsparrow.license.api.exception.ValidationException;

/**
 * Service for customer registrations. Implementors provide functionality for
 * registering, activating and validating customer registrations.
 * 
 * @since 3.0.0
 *
 */
public interface RegistrationService {

	/**
	 * Creates a customer registration with the provided information.
	 * 
	 * @param email
	 *            customer's email address
	 * @param firstName
	 *            customer's first name
	 * @param lastName
	 *            customer's last name
	 * @param company
	 *            customer's functionality
	 * @param subscribe
	 *            customer's subscription
	 * @return {@code true} if the registration is created successfully or
	 *         {@code false} otherwise.
	 * @throws ValidationException
	 *             if the registration service cannot be reached.
	 */
	boolean register(String email, String firstName, String lastName, String company, boolean subscribe)
			throws ValidationException;

	/**
	 * Validates a customer registration against the hardware id and the
	 * registration's secret.
	 * 
	 * @param hardwareId
	 *            identifier for the customer machine.
	 * @param secret
	 *            secret key attached to the registration.
	 * @return {@code true} if the validation passes and {@code false}
	 *         otherwise.
	 */
	boolean validate(String hardwareId, String secret);

	/**
	 * Activates the customer registration with the customer activation key.  
	 * 
	 * @param activationKey the activation key delivered per email. 
	 * @return {@code true} if the activation is successful or {@code false} otherwise.
	 * @throws ValidationException if the activation service cannot be reached. 
	 */
	boolean activate(String activationKey) throws ValidationException;
}
