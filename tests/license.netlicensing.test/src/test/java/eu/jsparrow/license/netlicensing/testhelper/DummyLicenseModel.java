package eu.jsparrow.license.netlicensing.testhelper;

import java.time.ZonedDateTime;

import eu.jsparrow.license.api.LicenseModel;

public class DummyLicenseModel implements LicenseModel{

	@Override
	public ZonedDateTime getExpirationDate() {
		return ZonedDateTime.now();
	}
}