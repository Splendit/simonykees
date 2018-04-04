package eu.jsparrow.license.netlicensing.validation.impl;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.api.LicenseValidationResult;

@SuppressWarnings("nls")
public class NetlicensingLicenseCache {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
			.lookupClass());
	
	private static Map<String, NetlicensingValidationResult> entries = new HashMap<>();

	public void updateCache(String key, NetlicensingValidationResult newValidationResult) {
		logger.debug("Updating cache with key {} and validation result {}", key, newValidationResult);
		NetlicensingValidationResult entry = entries.putIfAbsent(key, newValidationResult);
		if (entry == null) {
			logger.debug("Adding new entry {}", newValidationResult);
			return;
		}
		logger.debug("Replacing existing entry with {}", newValidationResult);
		entries.replace(key, newValidationResult);
	}

	public LicenseValidationResult get(String key) {
		logger.debug("Retrieving item with key {}", key);
		NetlicensingValidationResult entry = entries.get(key);
		if (entry == null) {
			logger.debug("No item found");
			return null;
		}
		if (entry.isExpired()) {
			logger.debug("Expired item found");
			return null;
		}
		return entry;
	}

	Map<String, NetlicensingValidationResult> getEntries() {
		return entries;
	}

}
