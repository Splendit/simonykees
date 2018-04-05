package eu.jsparrow.license.netlicensing.validation.impl;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.api.LicenseValidationResult;

public class NetlicensingLicenseCache {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
			.lookupClass());
	
	private static Map<String, NetlicensingValidationResult> entries = new HashMap<>();

	public void updateCache(String key, NetlicensingValidationResult newValidationResult) {
		logger.debug("Updating cache with key {} and validation result {}", key, newValidationResult); //$NON-NLS-1$
		NetlicensingValidationResult entry = entries.putIfAbsent(key, newValidationResult);
		if (entry == null) {
			logger.debug("Adding new entry {}", newValidationResult); //$NON-NLS-1$
			return;
		}
		logger.debug("Replacing existing entry with {}", newValidationResult); //$NON-NLS-1$
		entries.replace(key, newValidationResult);
	}

	public LicenseValidationResult get(String key) {
		logger.debug("Retrieving item with key {}", key); //$NON-NLS-1$
		NetlicensingValidationResult entry = entries.get(key);
		if (entry == null) {
			logger.debug("No item found"); //$NON-NLS-1$
			return null;
		}
		if (entry.isExpired()) {
			logger.debug("Expired item found"); //$NON-NLS-1$
			return null;
		}
		return entry;
	}

	Map<String, NetlicensingValidationResult> getEntries() {
		return entries;
	}

}
