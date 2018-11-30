package eu.jsparrow.registration;

import org.osgi.service.component.annotations.Component;

import eu.jsparrow.license.api.RegistrationModel;
import eu.jsparrow.license.api.RegistrationService;
import eu.jsparrow.license.api.exception.ValidationException;

@Component
public class CustomerRegistrationService implements RegistrationService {

	@Override
	public boolean validate(RegistrationModel model) throws ValidationException {
		return true;
	}

	@Override
	public boolean register(RegistrationModel model) throws ValidationException {
		return true;
	}

}
