package eu.jsparrow.license.netlicensing.cleanslate;

import java.time.ZonedDateTime;

import eu.jsparrow.license.netlicensing.cleanslate.model.*;

public class LicenseModelFactory {

	public DemoLicenseModel createDemoLicenseModel() {
		return new DemoLicenseModel();
	}

	public NetlicensingLicenseModel createNewNodeLockedModel(String key, String secret) {
		return new NetlicensingLicenseModel(NetlicensingLicenseType.NODE_LOCKED, key, secret);
	}

	public NetlicensingLicenseModel createNewFloatingModel(String key, String secret) {
		return new NetlicensingLicenseModel(NetlicensingLicenseType.FLOATING, key, secret);
	}

	public NetlicensingLicenseModel createNewNetlicensingModel(NetlicensingLicenseType type, String key, String name,
			String product, String secret, ZonedDateTime expireDate, ZonedDateTime offlineExpireDate) {
		return new NetlicensingLicenseModel(type, key, name, product, secret, expireDate, offlineExpireDate);
	}

}
