package eu.jsparrow.license.api;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Implementors represent a License Model. License models are the representation
 * of an actual license. These models may be validated using a
 * {@link LicenseService} or persisted using a
 * {@link LicensePersistenceService}.
 */
public interface LicenseModel extends Serializable {

	/**
	 * Get the expiration date of this license model.
	 * 
	 * @return the license expiration date
	 */
	public ZonedDateTime getExpirationDate();

	/**
	 * Return the license type of this license model.
	 * 
	 * @return the license type
	 * @see LicenseType
	 */
	public LicenseType getType();
}
