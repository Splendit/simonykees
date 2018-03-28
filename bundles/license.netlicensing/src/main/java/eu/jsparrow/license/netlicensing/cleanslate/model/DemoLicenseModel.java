package eu.jsparrow.license.netlicensing.cleanslate.model;

import java.time.ZonedDateTime;

public class DemoLicenseModel implements LicenseModel {

	private static final long serialVersionUID = 5753428747671948588L;
	
	private ZonedDateTime expirationDate;
	
	public DemoLicenseModel() {
		expirationDate = ZonedDateTime.now().plusDays(5);
	}

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
