package eu.jsparrow.license.netlicensing.testhelper;

import java.time.ZonedDateTime;

import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;

public class NetlicensingLicenseModelFactory {

	public static NetlicensingLicenseModel create() {
		return new NetlicensingLicenseModel(LicenseType.NODE_LOCKED, "key", "name", "secret",
				ZonedDateTime.now());
	}

	public static NetlicensingLicenseModel create(LicenseType type) {
		return new NetlicensingLicenseModel(type, "key", "name", "secret", ZonedDateTime.now());
	}

}
