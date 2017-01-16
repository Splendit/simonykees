package at.splendit.simonykees.core.license;

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
	 */
	LicenseType getType();
	
	/**
	 * Returns true if the license is valid and the licensee is 
	 * allowed to use the product.
	 */
	boolean isValid();
	
	/**
	 * Returns a time-stamp of the last validation.
	 */
	Instant getValidationTimeStamp();
	
	/**
	 * Returns the name of the licensee.
	 */
	String getLicenseeName();
	
	/**
	 * Returns an {@link LicenseStatus} indicating the current
	 * status of the license validation.
	 */
	LicenseStatus getLicenseStatus();
	
	/**
	 * Returns license expiration date.
	 */
	ZonedDateTime getExpirationDate();
	
}
