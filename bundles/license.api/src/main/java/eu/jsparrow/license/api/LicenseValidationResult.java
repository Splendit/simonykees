package eu.jsparrow.license.api;

@SuppressWarnings("nls")
public class LicenseValidationResult {

	private boolean valid;

	private String detail;

	private LicenseModel model;
	
	private String key;

	public LicenseValidationResult() {
		this(null, null, false, null);
	}

	public LicenseValidationResult(LicenseModel model, String key, boolean valid) {
		this(model, key, valid, "");
	}

	public LicenseValidationResult(LicenseModel model, String key, boolean valid, String detail) {
		this.model = model;
		this.key = key;
		this.valid = valid;
		this.detail = detail;
	}

	public boolean isValid() {
		return valid;
	}

	public String getDetail() {
		return detail;
	}

	public LicenseModel getModel() {
		return model;
	}

	public void setModel(LicenseModel model) {
		this.model = model;
	}
	
	public String getKey() {
		return key;
	}

	@Override
	public String toString() {
		return "LicenseValidationResult [valid=" + valid + ", detail=" + detail + ", model=" + model + "]";
	}

}
