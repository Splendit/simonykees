package eu.jsparrow.registration.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import eu.jsparrow.license.api.RegistrationModel;

public class CustomerRegistrationModel implements RegistrationModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7570445276249127853L;

	private String email = ""; //$NON-NLS-1$
	private String key = ""; //$NON-NLS-1$
	private String firstName = ""; //$NON-NLS-1$
	private String lastName = ""; //$NON-NLS-1$
	private String company = ""; //$NON-NLS-1$
	private boolean subscribe = false;
	private String secret = ""; //$NON-NLS-1$

	public CustomerRegistrationModel() {

	}

	public CustomerRegistrationModel(String key, String email) {
		this.email = email;
		this.key = key;
	}

	public CustomerRegistrationModel(String key, String email, String firstName, String lastName, String company,
			boolean subscribe, String secret) {
		this(key, email);
		this.firstName = firstName;
		this.lastName = lastName;
		this.company = company;
		this.subscribe = subscribe;
		this.secret = secret;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getFirstName() {
		return firstName;
	}

	@Override
	public String getLastName() {
		return lastName;
	}

	@Override
	public String getCompany() {
		return company;
	}

	@Override
	public boolean hasSubscribed() {
		return subscribe;
	}

	@Override
	public String getSecret() {
		return secret;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
