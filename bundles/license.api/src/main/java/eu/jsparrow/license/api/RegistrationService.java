package eu.jsparrow.license.api;

import eu.jsparrow.license.api.exception.ValidationException;

public interface RegistrationService {
	
	boolean register(RegistrationModel registrationModel) throws ValidationException;
	
	boolean validate(RegistrationModel registrationModel, String secret);
	
	boolean activate(RegistrationModel registrationModel) throws ValidationException;

}
