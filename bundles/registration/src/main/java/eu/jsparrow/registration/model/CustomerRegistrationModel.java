package eu.jsparrow.registration.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class CustomerRegistrationModel implements Serializable {

	private static final long serialVersionUID = -7570445276249127853L;

	private String email = ""; //$NON-NLS-1$
	private String firstName = ""; //$NON-NLS-1$
	private String lastName = ""; //$NON-NLS-1$
	private String company = ""; //$NON-NLS-1$
	private boolean subscribe = false;

	public CustomerRegistrationModel() {

	}

	public CustomerRegistrationModel(String email, String firstName, String lastName, String company,
			boolean subscribe) {
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.company = company;
		this.subscribe = subscribe;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getCompany() {
		return company;
	}

	public boolean hasSubscribed() {
		return subscribe;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
