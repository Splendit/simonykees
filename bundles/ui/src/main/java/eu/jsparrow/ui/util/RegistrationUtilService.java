package eu.jsparrow.ui.util;

public interface RegistrationUtilService {

	void register(String email, String firstName, String lastName, String company, boolean subscribe);
	
	void activateRegistration(String key);
	
	boolean isActiveRegistration();
}
