package eu.jsparrow.license.netlicensing.validation.impl;

import java.time.ZonedDateTime;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.LicenseValidationResult;

@SuppressWarnings("nls")
public class NetlicensingValidationResult extends LicenseValidationResult {

	private ZonedDateTime expirationTime;

	public NetlicensingValidationResult(LicenseModel model, boolean valid, ZonedDateTime expirationTime) {
		this(model, valid, "", expirationTime);
	}

	public NetlicensingValidationResult(LicenseModel model, boolean valid, String detail,
			ZonedDateTime expirationTime) {
		super(model, valid, detail);
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
