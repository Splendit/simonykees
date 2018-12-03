package eu.jsparrow.license.api;

public interface RegistrationModelFactoryService {
	
	RegistrationModel createRegistrationModel();
	
	RegistrationModel createRegistrationModel(String key, String email);
	
	RegistrationModel createRegistrationModel(String key, String email, String firstName, String lastName, String company, boolean subscribe);

}
