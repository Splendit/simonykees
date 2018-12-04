package eu.jsparrow.registration.persistence;

import java.lang.invoke.MethodHandles;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.api.RegistrationModel;
import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.license.api.persistence.IEncryption;
import eu.jsparrow.license.api.persistence.SecureStoragePersistence;
import eu.jsparrow.registration.model.CustomerRegistrationModel;

/**
 * Contains functionality for encrypting, storing and retrieving a
 * {@link RegistrationModel} into {@link ISecurePreferences}.
 * 
 * @since 3.0.0
 *
 */
public class RegistrationSecureStoragePersistence extends SecureStoragePersistence<RegistrationModel> {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static final String NODE_KEY = "registration-model"; //$NON-NLS-1$

	public RegistrationSecureStoragePersistence(ISecurePreferences securePreferences, IEncryption encryption) {
		super(securePreferences, encryption);
	}

	@Override
	public RegistrationModel load() throws PersistenceException {
		byte[] encryptedModel = loadFromSecureStorage(NODE_KEY);
		if (encryptedModel == null) {
			logger.warn("Could not find registration data in storage, saving and returning default registration"); //$NON-NLS-1$
			RegistrationModel defaultRegistration = new CustomerRegistrationModel();
			save(defaultRegistration);
			return defaultRegistration;
		}
		return ModelSerializer.deserialize(encryption.decrypt(encryptedModel));
	}

	@Override
	public void save(RegistrationModel model) throws PersistenceException {
		byte[] modelAsBytes = ModelSerializer.serialize(model);
		saveToSecureStorage(encryption.encrypt(modelAsBytes), NODE_KEY);
	}
}
