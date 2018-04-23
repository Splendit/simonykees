package eu.jsparrow.license.netlicensing.validation.impl;

import java.time.ZonedDateTime;

import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.api.LicenseValidationResult;

@SuppressWarnings("nls")
public class NetlicensingValidationResult extends LicenseValidationResult {

	private ZonedDateTime offlineExpirationTime;

	public NetlicensingValidationResult(LicenseType licenseType, String key, boolean valid,
			ZonedDateTime expirationDate, ZonedDateTime offlineExpirationTime) {
		this(licenseType, key, valid, "", expirationDate, offlineExpirationTime);
	}

	public NetlicensingValidationResult(LicenseType licenseType, String key, boolean valid, String detail,
			ZonedDateTime expirationDate, ZonedDateTime offlineExpirationTime) {
		super(licenseType, key, valid, detail, expirationDate);
		this.offlineExpirationTime = offlineExpirationTime;
	}

	public boolean isExpired() {
		return ZonedDateTime.now()
			.isAfter(offlineExpirationTime);
	}

	public ZonedDateTime getExpirationTime() {
		return offlineExpirationTime;
	}

	public void setExpirationTime(ZonedDateTime expirationTime) {
		this.offlineExpirationTime = expirationTime;
	}

}
