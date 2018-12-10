package eu.jsparrow.ui.util;

import eu.jsparrow.license.api.RegistrationService;

/**
 * Implementors of this class provide functions for customer registrations. This
 * is a helper interface, implementors are only supposed to be used in the UI.
 * 
 * @since 3.0.0
 *
 */
public interface RegistrationUtilService {

	/**
	 * Uses the {@link RegistrationService} for registering a customer.
	 * 
	 * @param email
	 *            customers email address
	 * @param firstName
	 *            customer's first name
	 * @param lastName
	 *            customer's last name
	 * @param company
	 *            customer's company
	 * @param subscribe
	 *            customer's subscription
	 * @return {@code true} if the registration was successful and {@code false}
	 *         otherwise.
	 */
	boolean register(String email, String firstName, String lastName, String company, boolean subscribe);

	/**
	 * Activates a customer registration.
	 * 
	 * @param key
	 *            activation key which customer delivers via email
	 * @return {@code true} if the activation is successful and {@code false}
	 *         otherwise
	 */
	boolean activateRegistration(String key);

	/**
	 * Checks if customers has an active registration.
	 * 
	 * @return {@code true} if the activation is successful and {@code false}
	 *         otherwise.
	 */
	boolean isActiveRegistration();
}
