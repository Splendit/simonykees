package eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model;

public abstract class LicensingModel {
	
	public static final String VALID_KEY = "valid";  //$NON-NLS-1$
	
	private boolean valid;

	protected LicensingModel(boolean valid) {
		this.valid = valid;
	}
	
	public boolean isValid() {
		return valid;
	}
}
