package eu.jsparrow.registration;

import org.osgi.service.component.annotations.Component;

import eu.jsparrow.license.api.RegistrationModel;
import eu.jsparrow.license.api.RegistrationModelFactoryService;
import eu.jsparrow.registration.model.CustomerRegistrationModel;

/**
 * Factory for creating {@link RegistrationModel}s.
 * 
 * @since 3.0.0
 *
 */
@Component
public class CustomerRegistrationModelFactoryService implements RegistrationModelFactoryService {

	@Override
	public RegistrationModel createRegistrationModel() {
		return new CustomerRegistrationModel();
	}

	@Override
	public RegistrationModel createRegistrationModel(String key, String email) {
		return new CustomerRegistrationModel(key, email);
	}

	@Override
	public RegistrationModel createRegistrationModel(String key, String email, String firstName, String lastName,
			String company, boolean subscribe) {
		
		return new CustomerRegistrationModel(key, email, firstName, lastName, company, subscribe);
	}

}
