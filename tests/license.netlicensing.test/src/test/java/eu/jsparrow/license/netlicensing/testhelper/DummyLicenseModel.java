package eu.jsparrow.license.netlicensing.testhelper;

import java.time.ZonedDateTime;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.LicenseType;

public class DummyLicenseModel implements LicenseModel{

	@Override
	public ZonedDateTime getExpirationDate() {
		return ZonedDateTime.now();
	}

	@Override
	public LicenseType getType() {
		return LicenseType.NONE;
	}
}