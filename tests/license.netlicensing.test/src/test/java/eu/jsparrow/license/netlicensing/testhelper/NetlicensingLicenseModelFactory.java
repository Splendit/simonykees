package eu.jsparrow.license.netlicensing.testhelper;

import java.time.ZonedDateTime;

import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;

@SuppressWarnings("nls")
public class NetlicensingLicenseModelFactory {

	private NetlicensingLicenseModelFactory() {
		// private constructor to hide the implicit public one
	}

	public static NetlicensingLicenseModel create() {
		return new NetlicensingLicenseModel("key", "secret", "product", "module", LicenseType.NODE_LOCKED, "name",
				ZonedDateTime.now());
	}

	public static NetlicensingLicenseModel create(LicenseType type) {
		return new NetlicensingLicenseModel("key", "secret", "product", "module", type, "name", ZonedDateTime.now());
	}

}
