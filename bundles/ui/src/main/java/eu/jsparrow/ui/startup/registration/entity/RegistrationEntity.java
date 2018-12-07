package eu.jsparrow.ui.startup.registration.entity;

public class RegistrationEntity {

	private String firstName;
	private String lastName;
	private String email;
	private String company;
	private boolean agreeToNewsletter;

	public RegistrationEntity(String firstName, String lastName, String email, String company,
			boolean agreeToNewsletter) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.company = company;
		this.agreeToNewsletter = agreeToNewsletter;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public boolean isAgreeToNewsletter() {
		return agreeToNewsletter;
	}

	public void setAgreeToNewsletter(boolean agreeToNewsletter) {
		this.agreeToNewsletter = agreeToNewsletter;
	}

}
