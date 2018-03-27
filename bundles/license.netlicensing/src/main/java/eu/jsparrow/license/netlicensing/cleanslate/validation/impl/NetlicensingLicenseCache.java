package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import java.time.Duration;
import java.time.ZonedDateTime;

import eu.jsparrow.license.netlicensing.cleanslate.LicenseValidationResult;

public class NetlicensingLicenseCache {

	private static final Duration EXPIRATION_DURATION = Duration.ofHours(1);
	
	private ZonedDateTime lastUpdate; 
	
	private LicenseValidationResult cachedValidationResult;
	
	public boolean isInvalid() {
		if(lastUpdate == null) {
			return true;
		}
		return ZonedDateTime.now().isAfter(lastUpdate.plus(EXPIRATION_DURATION));
	}

	public LicenseValidationResult getLastResult() {
		return cachedValidationResult;
	}

	public void updateCache(LicenseValidationResult newValidationResult) {
		this.cachedValidationResult = newValidationResult;
		lastUpdate = ZonedDateTime.now();
	}
	
	//Only used for tests
	void setLastUpdate(ZonedDateTime lastUpdate) {
		this.lastUpdate = lastUpdate; 
	}

}
