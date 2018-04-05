package eu.jsparrow.license.netlicensing.validation.impl;

import java.time.ZonedDateTime;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.LicenseValidationResult;

@SuppressWarnings("nls")
public class NetlicensingValidationResult extends LicenseValidationResult {

	private ZonedDateTime expirationTime;

	public NetlicensingValidationResult(LicenseModel model, String key, boolean valid, ZonedDateTime expirationTime) {
		this(model, key, valid, "", expirationTime);
	}

	public NetlicensingValidationResult(LicenseModel model, String key, boolean valid, String detail,
			ZonedDateTime expirationTime) {
		super(model, key, valid, detail);
		this.expirationTime = expirationTime;
	}

	public boolean isExpired() {
		return ZonedDateTime.now()
			.isAfter(expirationTime);
	}

	public ZonedDateTime getExpirationTime() {
		return expirationTime;
	}

	public void setExpirationTime(ZonedDateTime expirationTime) {
		this.expirationTime = expirationTime;
	}

}
