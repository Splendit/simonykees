package eu.jsparrow.license.netlicensing;

import java.time.ZonedDateTime;

import eu.jsparrow.license.netlicensing.model.*;

public class LicenseModelFactory {

	public DemoLicenseModel createDemoLicenseModel() {
		return new DemoLicenseModel();
	}
	
	public DemoLicenseModel createDemoLicenseModel(ZonedDateTime expirationDate) {
		return new DemoLicenseModel(expirationDate);
	}

	public NetlicensingLicenseModel createNewNodeLockedModel(String key, String secret) {
		return new NetlicensingLicenseModel(NetlicensingLicenseType.NODE_LOCKED, key, secret);
	}

	public NetlicensingLicenseModel createNewFloatingModel(String key, String secret) {
		return new NetlicensingLicenseModel(NetlicensingLicenseType.FLOATING, key, secret);
	}

	public NetlicensingLicenseModel createNewNetlicensingModel(NetlicensingLicenseType type, String key, String name,
			String product, String secret, ZonedDateTime expireDate) {
		return new NetlicensingLicenseModel(type, key, name, secret, expireDate);
	}

}
