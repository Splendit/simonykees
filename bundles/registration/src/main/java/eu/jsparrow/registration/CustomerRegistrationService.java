package eu.jsparrow.registration;

import org.osgi.service.component.annotations.Component;

import eu.jsparrow.license.api.RegistrationModel;
import eu.jsparrow.license.api.RegistrationService;
import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.registration.validation.RegistrationValidation;

@Component
public class CustomerRegistrationService implements RegistrationService {
	
	private RegistrationValidation registrationValidation = new RegistrationValidation();

	@Override
	public boolean validate(RegistrationModel model) throws ValidationException {
		return registrationValidation.validate(model);
	}

	@Override
	public boolean register(RegistrationModel model) throws ValidationException {
		return registrationValidation.register(model);
	}
	
	@Override
	public boolean activate(RegistrationModel model) throws ValidationException {
		return registrationValidation.activate(model);
	}

}
