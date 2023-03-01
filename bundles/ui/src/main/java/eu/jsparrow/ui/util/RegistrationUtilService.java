package eu.jsparrow.ui.util;

import eu.jsparrow.license.api.RegistrationService;
import eu.jsparrow.ui.startup.registration.entity.ActivationEntity;
import eu.jsparrow.ui.startup.registration.entity.RegistrationEntity;

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
	 * @param registrationEntity
	 *            entity containing information for customer registration.
	 * @return {@code true} if the registration was successful and {@code false}
	 *         otherwise.
	 */
	boolean register(RegistrationEntity registrationEntity);

	/**
	 * Activates a customer registration.
	 * 
	 * @param activationEntity
	 *            entity containing information for activating customer
	 *            registration.
	 * @return {@code true} if the activation is successful and {@code false}
	 *         otherwise
	 */
	boolean activateRegistration(ActivationEntity activationEntity);
}
