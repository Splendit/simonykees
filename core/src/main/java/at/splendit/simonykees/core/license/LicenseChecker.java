package at.splendit.simonykees.core.license;

import java.time.Instant;

/**
 * Provides information about the current status of the license.
 */
public interface LicenseChecker {
	
	/**
	 * Returns the type of the license as defined in {@link LicenseType}.
	 */
	LicenseType getType();
	
	/**
	 * Returns if the status of the license is valid.
	 */
	boolean getStatus();
	
	/**
	 * Returns a time-stamp of the last validation.
	 */
	Instant getValidationTimeStamp();
	
	/**
	 * Returns the name of the licensee.
	 */
	String getLicenseeName();
	
	
}
