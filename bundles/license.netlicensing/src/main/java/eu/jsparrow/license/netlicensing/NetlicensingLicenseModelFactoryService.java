package eu.jsparrow.license.netlicensing;

import java.time.ZonedDateTime;

import org.osgi.service.component.annotations.Component;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.LicenseModelFactoryService;
import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.netlicensing.model.*;

@Component
public class NetlicensingLicenseModelFactoryService implements LicenseModelFactoryService {

	public LicenseModel createDemoLicenseModel() {
		return new DemoLicenseModel();
	}
	
	public LicenseModel createDemoLicenseModel(ZonedDateTime expirationDate) {
		return new DemoLicenseModel(expirationDate);
	}

	public LicenseModel createNewNodeLockedModel(String key, String secret) {
		return new NetlicensingLicenseModel(LicenseType.NODE_LOCKED, key, secret);
	}

	public LicenseModel createNewFloatingModel(String key, String secret) {
		return new NetlicensingLicenseModel(LicenseType.FLOATING, key, secret);
	}

	public LicenseModel createNewModel(LicenseType type, String key, String name,
			String secret, ZonedDateTime expireDate) {
		return new NetlicensingLicenseModel(type, key, name, secret, expireDate);
	}

}
