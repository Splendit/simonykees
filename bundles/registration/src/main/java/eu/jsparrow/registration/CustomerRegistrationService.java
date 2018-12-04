package eu.jsparrow.registration;

import java.lang.invoke.MethodHandles;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.api.RegistrationModel;
import eu.jsparrow.license.api.RegistrationService;
import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.registration.validation.RegisterValidation;

/**
 * Service for customer registrations. Provides functionality for registering,
 * activating and validating customer registrations.
 * 
 * @since 3.0.0
 */
@Component
public class CustomerRegistrationService implements RegistrationService {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private RegisterValidation registrationValidation = new RegisterValidation();

	@Override
	public boolean validate(RegistrationModel model) throws ValidationException {
		logger.debug("Validating registration {}", model); //$NON-NLS-1$
		return registrationValidation.validate(model);
	}

	@Override
	public boolean register(RegistrationModel model) throws ValidationException {
		logger.debug("Registering {}", model); //$NON-NLS-1$
		return registrationValidation.register(model);
	}

	@Override
	public boolean activate(RegistrationModel model) throws ValidationException {
		logger.debug("Activating registration {}", model); //$NON-NLS-1$
		return registrationValidation.activate(model);
	}

}
