package eu.jsparrow.ui.startup.registration.entity;

/**
 * An entity containing information for activating a customer registration.
 * 
 * @since 3.0.0
 *
 */
public class ActivationEntity {

	private String activationKey;

	public ActivationEntity(String license) {
		this.activationKey = license;
	}

	public String getActivationKey() {
		return activationKey;
	}

	public void setLicense(String license) {
		this.activationKey = license;
	}

}
