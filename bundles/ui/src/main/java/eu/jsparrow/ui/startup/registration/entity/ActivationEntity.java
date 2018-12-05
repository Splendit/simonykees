package eu.jsparrow.ui.startup.registration.entity;

public class ActivationEntity {

	private String license;

	public ActivationEntity(String license) {
		super();
		this.license = license;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}
	
}
