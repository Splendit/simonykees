package eu.jsparrow.registration.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import eu.jsparrow.license.api.RegistrationModel;

public class CustomerRegistrationModel implements RegistrationModel {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7570445276249127853L;
	
	private String email;
	private String key;
	private String firstName = "";
	private String lastName = "";
	private String company = "";
	
	public CustomerRegistrationModel(String email, String key) {
		this.email = email;
		this.key = key;
	}
	
	public CustomerRegistrationModel(String email, String key, String firstName, String lastName, String company) {
		this.email = email;
		this.key = key;
		this.firstName = firstName;
		this.lastName = lastName;
		this.company = company;
	}
	
	public String getEmail() {
		return email;
	}

	public String getKey() {
		return key;
	}
	
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

	@Override
	public String getFirstName() {
		return "";
	}

	@Override
	public String getLastName() {
		return "";
	}

	@Override
	public String getCompany() {
		return "";
	}

}
