package eu.jsparrow.license.netlicensing.validation.impl;

import java.time.ZonedDateTime;

import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;

public class NetlicensingLicenseCache {

	private static NetlicensingLicenseCache instance;

	private LicenseValidationResult cachedValidationResult;

	NetlicensingLicenseCache() {
		// Use only for testing
	}

	public static NetlicensingLicenseCache get() {
		if (instance == null) {
			instance = new NetlicensingLicenseCache();
		}
		return instance;
	}
	
	private boolean validationResultIsUpToDate(NetlicensingLicenseModel model) {
		if(model.getOfflineExpireDate() == null) {
			return false;
		}
		return model.getOfflineExpireDate().isAfter(ZonedDateTime.now());
	}
	
	public LicenseValidationResult getValidationResultFor(NetlicensingLicenseModel model) {
		if(!validationResultIsUpToDate(model)) {
			return null;
		}
		return cachedValidationResult;
	}

	public void updateCache(LicenseValidationResult newValidationResult) {
		if(newValidationResult.isValid()) {
			// Only cache valid results!
			this.cachedValidationResult = newValidationResult;
		}
	}
}
