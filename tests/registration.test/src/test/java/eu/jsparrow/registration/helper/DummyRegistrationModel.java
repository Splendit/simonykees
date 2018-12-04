package eu.jsparrow.registration.helper;

import eu.jsparrow.license.api.RegistrationModel;

public class DummyRegistrationModel implements RegistrationModel {

	private static final long serialVersionUID = 1L;

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
		return "key";
	}

	@Override
	public boolean hasSubscribed() {
		return true;
	}

}
