package eu.jsparrow.license.netlicensing;

import java.time.ZonedDateTime;

import org.osgi.service.component.annotations.Component;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.LicenseModelFactoryService;
import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.netlicensing.model.*;

@Component
public class NetlicensingLicenseModelFactoryService implements LicenseModelFactoryService {

	@Override
	public LicenseModel createDemoLicenseModel() {
		return new DemoLicenseModel();
	}

	@Override
	public LicenseModel createDemoLicenseModel(ZonedDateTime expirationDate) {
		return new DemoLicenseModel();
	}

	@Override
	public LicenseModel createNewNodeLockedModel(String key, String secret, String productNr, String moduleNr) {
		return new NetlicensingLicenseModel(key, secret, productNr, moduleNr, LicenseType.NODE_LOCKED);
	}

	@Override
	public LicenseModel createNewFloatingModel(String key, String secret, String productNr, String moduleNr) {
		return new NetlicensingLicenseModel(key, secret, productNr, moduleNr, LicenseType.FLOATING);
	}

	@Override
	public LicenseModel createNewModel(String key, String secret, String productNr, String moduleNr, LicenseType type,
			String name, ZonedDateTime expireDate) {
		return new NetlicensingLicenseModel(key, secret, productNr, moduleNr, type, name, expireDate);
	}

}
