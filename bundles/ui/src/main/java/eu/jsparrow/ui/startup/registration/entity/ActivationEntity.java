package eu.jsparrow.ui.startup.registration.entity;

/**
 * An entity containing information for activating a customer registration.
 * 
 * @since 3.0.0
 *
 */
public class ActivationEntity {

	private String license;

	public ActivationEntity(String license) {
		this.license = license;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

}
