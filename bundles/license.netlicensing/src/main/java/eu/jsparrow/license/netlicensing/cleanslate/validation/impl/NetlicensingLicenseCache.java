package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import java.time.Duration;
import java.time.ZonedDateTime;

import eu.jsparrow.license.netlicensing.cleanslate.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.cleanslate.model.NetlicensingLicenseModel;

public class NetlicensingLicenseCache {

	private static final Duration EXPIRATION_DURATION = Duration.ofHours(1);

	private static NetlicensingLicenseCache instance;

	private ZonedDateTime lastUpdate;

	private LicenseValidationResult cachedValidationResult;

	private NetlicensingLicenseCache() {

	}

	public static NetlicensingLicenseCache get() {
		if (instance == null) {
			instance = new NetlicensingLicenseCache();
		}
		return instance;
	}
	
	public boolean requiresNewRequest(NetlicensingLicenseModel model) {
		if(model.getExpirationDate() != null && model.getExpirationDate().isBefore(ZonedDateTime.now())) {
			return true;
		}
		if(model.getExpirationDate().isBefore(ZonedDateTime.now())) {
			return true;
		}
		return false;
		
	}
	public boolean isInvalid() {
		if (lastUpdate == null) {
			return true;
		}
		NetlicensingLicenseModel model = (NetlicensingLicenseModel) cachedValidationResult.getModel();
		if(model.getExpirationDate() == null) {
			return true;
		}
		if(model.getExpirationDate().isBefore(ZonedDateTime.now())) {
			return true;
		}
		return false;
	}

	public LicenseValidationResult getLastResult() {
		return cachedValidationResult;
	}

	public void updateCache(LicenseValidationResult newValidationResult) {
		this.cachedValidationResult = newValidationResult;
		lastUpdate = ZonedDateTime.now();
	}

	// Only used for tests
	void setLastUpdate(ZonedDateTime lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

}
