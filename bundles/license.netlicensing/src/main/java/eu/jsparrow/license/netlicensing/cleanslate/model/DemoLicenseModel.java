package eu.jsparrow.license.netlicensing.cleanslate.model;

import java.time.ZonedDateTime;

public class DemoLicenseModel implements LicenseModel {

	private ZonedDateTime expirationDate;

	public void setExpirationDate(ZonedDateTime expirationDate) {
		this.expirationDate = expirationDate;
	}

	public DemoLicenseModel(ZonedDateTime expirationDate) {
		this.expirationDate = expirationDate;
	}
	
	public ZonedDateTime getExpirationDate() {
		return expirationDate;
	}

}
