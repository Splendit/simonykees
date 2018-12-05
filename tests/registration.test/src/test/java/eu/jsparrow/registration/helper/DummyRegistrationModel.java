package eu.jsparrow.registration.helper;

import eu.jsparrow.license.api.RegistrationModel;

@SuppressWarnings("nls")
public class DummyRegistrationModel implements RegistrationModel {

	private static final long serialVersionUID = 1L;
	
	private String key = "key";
	private String secret = "veeery secret";
	
	public DummyRegistrationModel() {
	
	}
	
	public DummyRegistrationModel(String key, String secret) {
		this.key = key;
		this.secret = secret;
	}

	@Override
	public String getEmail() {
		return "sample@mail.com";
	}

	@Override
	public String getFirstName() {
		return "FirstName";
	}

	@Override
	public String getLastName() {
		return "LastName";
	}

	@Override
	public String getCompany() {
		return "Company";
	}

	@Override
	public String getKey() {
		return key;
	}
	
	@Override
	public String getSecret() {
		return secret;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public void setSecret(String secret) {
		this.secret = secret;
	}
	
	@Override
	public boolean hasSubscribed() {
		return true;
	}

}
