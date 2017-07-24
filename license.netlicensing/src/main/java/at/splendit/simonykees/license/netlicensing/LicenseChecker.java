package at.splendit.simonykees.license.netlicensing;

import java.time.Instant;
import java.time.ZonedDateTime;

/**
 * Provides information about the current status of the license.
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public interface LicenseChecker {
	
	/**
	 * Returns the type of the license as defined in {@link LicenseType}.
	 * @return the type of the current license
	 */
	LicenseType getType();
	
	/**
	 * Returns true if the license is valid and the licensee is 
	 * allowed to use the product.
	 * @return boolean that represents the validation state of the license
	 */
	boolean isValid();
	
	/**
	 * Returns a time-stamp of the last validation.
	 * @return time-stamp as {@link Instant}
	 */
	Instant getValidationTimeStamp();
	
	/**
	 * Returns the name of the licensee.
	 * @return String that contains licensee number
	 */
	String getLicenseeName();
	
	/**
	 * @return an {@link LicenseStatus} indicating the current
	 * status of the license validation.
	 */
	LicenseStatus getLicenseStatus();
	
	/**
	 * @return license expiration date.
	 */
	ZonedDateTime getExpirationDate();
	
}
