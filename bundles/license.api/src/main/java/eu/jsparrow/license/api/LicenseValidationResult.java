package eu.jsparrow.license.api;

import java.time.ZonedDateTime;
import java.util.Optional;

import eu.jsparrow.license.api.util.AnnotationToStringBuilder;
import eu.jsparrow.license.api.util.Shorten;

/**
 * This class represents a validation result produced by {@link LicenseService}.
 */
public class LicenseValidationResult {

	private boolean valid;
	private String detail;
	private LicenseType licenseType;
	protected Integer credit;

	@Shorten
	private String key;

	private ZonedDateTime expirationDate;

	public LicenseValidationResult(LicenseType licenseType, String key, boolean valid, String detail,
			ZonedDateTime expirationDate) {
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

	public Optional<Integer> getCredit() {
		return Optional.ofNullable(this.credit);
	}

	@Override
	public String toString() {
		return new AnnotationToStringBuilder(this).toString();
	}

}
