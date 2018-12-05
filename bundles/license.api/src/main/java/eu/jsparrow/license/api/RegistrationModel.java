package eu.jsparrow.license.api;

import java.io.Serializable;

public interface RegistrationModel extends Serializable {
	
	String getEmail();
	String getFirstName();
	String getLastName();
	String getCompany();
	String getKey();
	String getSecret();
	boolean hasSubscribed();
}
