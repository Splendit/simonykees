package at.splendit.simonykees.license.api;

/**
 * provides a common interface for license validation as declarative service
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
public interface LicenseValidationService {
	/**
	 * starts the heart beat of the validation process
	 */
	void startValidation();

	/**
	 * stops the validation process
	 */
	void stopValidation();

	/**
	 * checks, if a valid license is present
	 * 
	 * @return true, if a valid license is present, false otherwise
	 */
	boolean isValid();

	/**
	 * updates the license key
	 * 
	 * @param licenseKey
	 * @param licenseName
	 * @return ?
	 */
	boolean updateLicenseeNumber(String licenseKey, String licenseName);

	/**
	 * produces human readable license information
	 * 
	 * @return human readable license information
	 */
	String getDisplayableLicenseInformation();

	/**
	 * produces human readable information about the status of the license
	 * 
	 * @return human readable status information
	 */
	String getLicenseStautsUserMessage();
}
