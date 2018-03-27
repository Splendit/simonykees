package eu.jsparrow.license.netlicensing.cleanslate.persistence;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.netlicensing.cleanslate.LicensePersistence;
import eu.jsparrow.license.netlicensing.cleanslate.exception.PersistenceException;
import eu.jsparrow.license.netlicensing.cleanslate.exception.ValidationException;
import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseModel;

@SuppressWarnings("nls")
public class SecureStoragePersistence implements LicensePersistence {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static final String ALGORITHM = "AES";

	private static final String TRANSFORMATION = "AES";

	private static final String KEY = "SOME_SECRET_KEY_";

	private static final String SECURE_PREFERENCES_KEY = "simonykees";

	private static final String NODE_KEY = "credentials";

	private ISecurePreferences securePreferences;
	
	private IEncryption encryption;

	public SecureStoragePersistence(ISecurePreferences securePreferences, IEncryption encryption) {
		this.securePreferences = securePreferences;
		this.encryption = encryption;
	}
	
	@Override
	public LicenseModel load() throws PersistenceException {
		byte[] encryptedModel = loadFromSecureStorage();
		return ModelSerializer.deserialize(encryption.decrypt(encryptedModel));
	}

	@Override
	public void save(LicenseModel model) throws PersistenceException {
		byte[] modelAsBytes = ModelSerializer.serialize(model);
		saveToSecureStorage(encryption.encrypt(modelAsBytes));
	}

	private void saveToSecureStorage(byte[] data) throws PersistenceException {
		ISecurePreferences simonykeesNode = securePreferences.node(SECURE_PREFERENCES_KEY);
		simonykeesNode.clear();
		try {
			simonykeesNode.flush();
			simonykeesNode.putByteArray(NODE_KEY, data, false);
			simonykeesNode.flush();
		} catch (IOException | StorageException e) {
			logger.error("Failed to write to secure storage", e);
			throw new PersistenceException(e);
		}
	}

	private byte[] loadFromSecureStorage() throws PersistenceException {
		try {
			ISecurePreferences simonykeesNode = securePreferences.node(SECURE_PREFERENCES_KEY);
			return simonykeesNode.getByteArray(NODE_KEY, new byte[0]);
		} catch (StorageException e) {
			logger.error("Failed to read from secure storage", e);
			throw new PersistenceException(e);
		}
	}

}