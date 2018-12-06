package eu.jsparrow.registration.helper;

import eu.jsparrow.registration.model.RegistrationModel;

@SuppressWarnings("nls")
public class DummyRegistrationModel extends RegistrationModel {

	private static final long serialVersionUID = 1L;
	
	public DummyRegistrationModel() {
		super("sample@mail.com", "FirstName", "LastName", "Company", true);
	}
}
