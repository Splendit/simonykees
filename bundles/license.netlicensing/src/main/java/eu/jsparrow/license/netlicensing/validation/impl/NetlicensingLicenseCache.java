package eu.jsparrow.license.netlicensing.validation.impl;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.api.LicenseValidationResult;

/**
 * This class saves {@link NetlicensingValidationResult}s for a specific license
 * key and serves them if they are not expired. This is used to reduce the
 * number of actual requests to the NetLicensing remote API.
 */
public class NetlicensingLicenseCache {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static Map<String, NetlicensingValidationResult> entries = new HashMap<>();

	/**
	 * Update the cache with a new validation result for the given key. If an
	 * entry with this key is already present it will be replaced. If not, a new
	 * entry will be added.
	 * 
	 * @param key
	 *            key to save the result with.
	 * @param newValidationResult
	 *            the validation result to save.
	 */
	public void updateCache(String key, NetlicensingValidationResult newValidationResult) {
		String shortKey = StringUtils.abbreviate(key, 6);
		logger.debug("Updating cache with key {} and validation result {}", shortKey, newValidationResult); //$NON-NLS-1$
		NetlicensingValidationResult entry = entries.putIfAbsent(key, newValidationResult);
		if (entry == null) {
			logger.debug("Adding new entry {}", newValidationResult); //$NON-NLS-1$
			return;
		}
		logger.debug("Replacing existing entry with {}", newValidationResult); //$NON-NLS-1$
		entries.replace(key, newValidationResult);
	}

	/**
	 * Get the cached validation result for a given key.
	 * 
	 * @param key
	 *            the licensee key to get a validation result for.
	 * @return <code>null</code> if no entry is present for this key. The
	 *         validation result for this key otherwise.
	 */
	public LicenseValidationResult get(String key) {
		String shortKey = StringUtils.abbreviate(key, 6);
		logger.debug("Retrieving result with key {}",  shortKey); //$NON-NLS-1$
		NetlicensingValidationResult entry = entries.get(key);
		if (entry == null) {
			logger.debug("No results found"); //$NON-NLS-1$
			return null;
		}
		if (entry.isExpired()) {
			logger.debug("Expired result found"); //$NON-NLS-1$
			return null;
		}
		return entry;
	}

	Map<String, NetlicensingValidationResult> getEntries() {
		return entries;
	}

}
