package eu.jsparrow.license.netlicensing.testhelper;

import java.time.ZonedDateTime;

import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;

public class NetlicensingLicenseModelFactory {

	public static NetlicensingLicenseModel create() {
		return new NetlicensingLicenseModel("key", "secret", LicenseType.NODE_LOCKED, "name", ZonedDateTime.now());
	}

	public static NetlicensingLicenseModel create(LicenseType type) {
		return new NetlicensingLicenseModel("key", "secret", type, "name", ZonedDateTime.now());
	}

}
