package eu.jsparrow.license.netlicensing.validation.impl;

import java.util.HashMap;
import java.util.Map;

import eu.jsparrow.license.api.LicenseValidationResult;

public class NetlicensingLicenseCache {

	private static Map<String, NetlicensingValidationResult> entries = new HashMap<>();

	NetlicensingLicenseCache() {
		// Use only for testing
	}

	public void updateCache(String key, NetlicensingValidationResult newValidationResult) {
		NetlicensingValidationResult entry = entries.putIfAbsent(key, newValidationResult);
		if (entry == null) {
			return;
		}
		entries.replace(key, newValidationResult);
	}

	public LicenseValidationResult get(String key) {
		NetlicensingValidationResult entry = entries.get(key);
		if (entry == null) {
			return null;
		}
		if (entry.isExpired()) {
			return null;
		}
		return entry;
	}

	Map<String, NetlicensingValidationResult> getEntries() {
		return entries;
	}

}
