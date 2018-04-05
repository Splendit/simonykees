package eu.jsparrow.license.netlicensing.testhelper;

import java.time.ZonedDateTime;

import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseType;

public class NetlicensingLicenseModelFactory {

	public static NetlicensingLicenseModel create() {
		return new NetlicensingLicenseModel(NetlicensingLicenseType.NODE_LOCKED, "key", "name", "secret", ZonedDateTime.now());
	}

	public static NetlicensingLicenseModel create(NetlicensingLicenseType type) {
		return new NetlicensingLicenseModel(type, "key", "name", "secret", ZonedDateTime.now());
	}

}
