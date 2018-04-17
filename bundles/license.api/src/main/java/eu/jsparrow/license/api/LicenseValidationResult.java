package eu.jsparrow.license.api;

import java.time.ZonedDateTime;

/**
 * This class represents a validation result produced by {@link LicenseService}.
 */
public class LicenseValidationResult {

	private boolean valid;
	private String detail;
	private LicenseType licenseType;
	private String key;
	private ZonedDateTime expirationDate;

	public LicenseValidationResult() {
		this(null, null, false, null, null);
	}

	public LicenseValidationResult(LicenseType licenseType, String key, boolean valid, ZonedDateTime expirationDate) {
		this(licenseType, key, valid, "", expirationDate); //$NON-NLS-1$
	}

	public LicenseValidationResult(LicenseType licenseType, String key, boolean valid, String detail, ZonedDateTime expirationDate) {
		this.licenseType = licenseType;
		this.key = key;
		this.valid = valid;
		this.detail = detail;
		this.expirationDate = expirationDate;
	}

	public boolean isValid() {
		return valid;
	}

	public String getDetail() {
		return detail;
	}

	public LicenseType getLicenseType() {
		return licenseType;
	}

	public void setLicenseType(LicenseType model) {
		this.licenseType = model;
	}
	
	public String getKey() {
		return key;
	}
	
	public ZonedDateTime getExpirationDate() {
		return this.expirationDate;
	}

	@Override
	public String toString() {
		return "LicenseValidationResult [valid=" + valid + ", detail=" + detail + ", type=" + licenseType + ", expirationDate=" + expirationDate + "]";
	}

}
