package eu.jsparrow.standalone;

import eu.jsparrow.standalone.exceptions.StandaloneException;

public interface StandaloneLicenseUtilService {

	/**
	 * Validates the license key.
	 * 
	 * @param key
	 *            the license key to be validated
	 * @param validationBaseUrl
	 *            the base url of the license server.
	 * @return if the license is valid.
	 */
	boolean validate(String key, String validationBaseUrl) throws StandaloneException;

	/**
	 * Prints information (type, validity and expiration date) about license
	 * from which key is provided.
	 * 
	 * @param key
	 *            license key for which information is asked
	 * @param validationBaseUrl
	 */
	void licenseInfo(String key, String validationBaseUrl) throws StandaloneException;

	void stop(String validationBaseUrl);
}