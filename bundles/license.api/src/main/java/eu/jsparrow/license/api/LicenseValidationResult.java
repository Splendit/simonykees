package eu.jsparrow.license.api;

@SuppressWarnings("nls")
public class LicenseValidationResult {

	private boolean valid;

	private String detail;

	private LicenseModel model;

	public LicenseValidationResult() {
		this(null, false, null);
	}

	public LicenseValidationResult(LicenseModel model, boolean valid) {
		this(model, valid, "");
	}

	public LicenseValidationResult(LicenseModel model, boolean valid, String detail) {
		this.model = model;
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
	
	@Override
	public String toString() {
		return "LicenseValidationResult [valid=" + valid + ", detail=" + detail + ", model=" + model + "]";
	}

}
