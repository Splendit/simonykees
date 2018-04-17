package eu.jsparrow.license.api;

import java.time.ZonedDateTime;
/**
 * Implementors provide methods for creating new {@link LicenseModel}s·
 */
public interface LicenseModelFactoryService {
	/**
	 * Create a new demo license model.
	 * @return a license model with {@link LicenseType} DEMO
	 */
	public LicenseModel createDemoLicenseModel();

	/**
	 * Create a new license model with a expiration date.
	 * @param expirationDate expiration date of the license
	 * @return a license model with type DEMO
	 */
	public LicenseModel createDemoLicenseModel(ZonedDateTime expirationDate);

	/**
	 * Create a new node locked license model.
	 * 
	 * @param key the key to be used for the model
	 * @param secret the secret to be used for the model
	 * @return a license model with type NODE_LOCKED
	 */
	public LicenseModel createNewNodeLockedModel(String key, String secret);

	/**
	 * Create a new floating license model.
	 * 
	 * @param key the key to be used for the model
	 * @param secret the secret to be used for the model
	 * @return a license model with type FLOATING
	 */
	public LicenseModel createNewFloatingModel(String key, String secret);

	/**
	 * Create a new license model with the given type.
	 * 
	 * @param type the {@link LicenseType} to use
	 * @param key the key of the license model
	 * @param name the name of the license model
	 * @param secret the secret of the model
	 * @param expireDate the expiration date of the model
	 * @return a license model the the given type
	 */
	public LicenseModel createNewModel(LicenseType type, String key, String name,
			String secret, ZonedDateTime expireDate);
}
