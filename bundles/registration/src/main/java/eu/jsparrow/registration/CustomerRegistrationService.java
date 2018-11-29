package eu.jsparrow.registration;

import org.osgi.service.component.annotations.Component;

import eu.jsparrow.license.api.RegistrationModel;
import eu.jsparrow.license.api.RegistrationService;

@Component
public class CustomerRegistrationService implements RegistrationService {

	@Override
	public boolean validate(RegistrationModel model)  {
		return true;
	}

	@Override
	public void register(RegistrationModel model)  {

	}

}
