package eu.jsparrow.registration;

import java.lang.invoke.MethodHandles;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.api.RegistrationService;
import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.registration.model.RegistrationModel;
import eu.jsparrow.registration.validation.RegisterValidation;

/**
 * An implementation of {@link RegistrationService} for handling customer
 * registrations on AWS services.
 * 
 * @since 3.0.0
 */
@Component
public class CustomerRegistrationService implements RegistrationService {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private RegisterValidation registrationValidation = new RegisterValidation();

	@Override
	public boolean validate(String hardwareId, String secret) {
		logger.debug("Validating registration"); //$NON-NLS-1$
		return registrationValidation.validate(hardwareId, secret);
	}

	@Override
	public boolean register(String email, String firstName, String lastName, String company, boolean subscribe)
			throws ValidationException {
		RegistrationModel model = new RegistrationModel(email, firstName, lastName, company, subscribe);
		logger.debug("Registering {}", model); //$NON-NLS-1$
		return registrationValidation.register(model);
	}

	@Override
	public boolean activate(String activationKey) throws ValidationException {
		logger.debug("Activating registration with key {}", activationKey); //$NON-NLS-1$
		return registrationValidation.activate(activationKey);
	}

}
