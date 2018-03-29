package eu.jsparrow.license.netlicensing.cleanslate.testhelper;

import java.time.ZonedDateTime;

import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseModel;

public class DummyLicenseModel implements LicenseModel{

	@Override
	public ZonedDateTime getExpirationDate() {
		return ZonedDateTime.now();
	}
}