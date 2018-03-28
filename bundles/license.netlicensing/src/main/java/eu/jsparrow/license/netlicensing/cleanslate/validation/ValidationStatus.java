package eu.jsparrow.license.netlicensing.cleanslate.validation;

	
public class ValidationStatus {
	private boolean valid;
	
	private String info;
	
	public boolean isValid() {
		return valid;
	}

	public String getInfo() {
		return info;
	}

	public ValidationStatus(boolean valid) {
		this.valid = valid;
	}
	
	public ValidationStatus(boolean valid, String info) {
		this(valid);
		this.info = info;
	}

}
