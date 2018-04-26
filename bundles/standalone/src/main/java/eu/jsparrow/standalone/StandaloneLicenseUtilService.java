package eu.jsparrow.standalone;

public interface StandaloneLicenseUtilService {
	boolean validate(String key);

	/**
	 * Prints information (type, validity and expiration date) about license
	 * from which key is provided.
	 * 
	 * @param key
	 *            license key for which information is asked
	 */
	void licenseInfo(String key);

	void stop();
}
