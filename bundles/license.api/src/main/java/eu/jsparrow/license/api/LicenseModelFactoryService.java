package eu.jsparrow.license.api;

import java.time.ZonedDateTime;

/**
 * Implementors provide methods for creating new {@link LicenseModel}s·
 */
public interface LicenseModelFactoryService {
	/**
	 * Create a new demo license model.
	 * 
	 * @return a license model with {@link LicenseType} DEMO
	 */
	public LicenseModel createDemoLicenseModel();

	/**
	 * Create a new node locked license model.
	 * 
	 * @param key
	 *            the key to be used for the model
	 * @param secret
	 *            the secret to be used for the model
	 * @return a license model with type NODE_LOCKED
	 */
	public LicenseModel createNewNodeLockedModel(String key, String secret, String productNr, String moduleNr);

	/**
	 * Create a new floating license model.
	 * 
	 * @param key
	 *            the key to be used for the model
	 * @param secret
	 *            the secret to be used for the model
	 * @return a license model with type FLOATING
	 */
	public LicenseModel createNewFloatingModel(String key, String secret, String productNr, String moduleNr);

	/**
	 * Create a new license model with the given type.
	 * 
	 * @param name
	 *            the name of the license model
	 * @param key
	 *            the key of the license model
	 * 
	 * @param secret
	 *            the secret of the model
	 * @param type
	 *            the {@link LicenseType} to use
	 * @param expireDate
	 *            the expiration date of the model
	 * @return a license model the the given type
	 */
	public LicenseModel createNewModel(String key, String secret, String productNr, String moduleNr, LicenseType type,
			String name, ZonedDateTime expireDate);
}
