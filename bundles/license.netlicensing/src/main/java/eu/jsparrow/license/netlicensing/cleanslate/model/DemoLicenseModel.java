package eu.jsparrow.license.netlicensing.cleanslate.model;

import java.time.ZonedDateTime;

public class DemoLicenseModel implements LicenseModel {

	private ZonedDateTime expireDate;

	public DemoLicenseModel(ZonedDateTime expireDate) {
		setExpireDate(expireDate);
	}

	private void setExpireDate(ZonedDateTime expireDate2) {
		this.expireDate = expireDate2;
	}

	public ZonedDateTime getExpireDate() {
		return expireDate;
	}
}
