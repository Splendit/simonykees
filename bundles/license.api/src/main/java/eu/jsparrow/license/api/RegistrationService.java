package eu.jsparrow.license.api;

public interface RegistrationService {
	
	void register(RegistrationModel registrationModel);
	
	boolean validate(RegistrationModel registrationModel);

}
