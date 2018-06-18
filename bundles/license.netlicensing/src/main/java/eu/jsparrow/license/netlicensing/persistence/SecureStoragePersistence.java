package eu.jsparrow.license.netlicensing.persistence;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.license.netlicensing.NetlicensingLicenseModelFactoryService;
import eu.jsparrow.license.netlicensing.LicensePersistence;

/**
 * Implementor of {@link LicensePersistence}.
 * 
 */
public class SecureStoragePersistence implements LicensePersistence {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static final String SECURE_PREFERENCES_KEY = "simonykees"; //$NON-NLS-1$

	private static final String NODE_KEY = "license-model"; //$NON-NLS-1$

	private ISecurePreferences securePreferences;
	
	private IEncryption encryption;

	public SecureStoragePersistence(ISecurePreferences securePreferences, IEncryption encryption) {
		this.securePreferences = securePreferences;
		this.encryption = encryption;
	}

	@Override
	public LicenseModel load() throws PersistenceException {
		byte[] encryptedModel = loadFromSecureStorage();
		if(encryptedModel == null) {
			logger.warn("Could not find existing license in storage, saving and returning default license"); //$NON-NLS-1$
			LicenseModel defaultModel = new NetlicensingLicenseModelFactoryService().createDemoLicenseModel();
			save(defaultModel);
			return defaultModel;
		}
		return ModelSerializer.deserialize(encryption.decrypt(encryptedModel));
	}

	@Override
	public void save(LicenseModel model) throws PersistenceException {
		byte[] modelAsBytes = ModelSerializer.serialize(model);
		saveToSecureStorage(encryption.encrypt(modelAsBytes));
	}

	private void saveToSecureStorage(byte[] data) throws PersistenceException {
		logger.debug("Saving data to secure storage"); //$NON-NLS-1$
		ISecurePreferences simonykeesNode = securePreferences.node(SECURE_PREFERENCES_KEY);
		simonykeesNode.clear();
		try {
			simonykeesNode.flush();
			simonykeesNode.putByteArray(NODE_KEY, data, false);
			simonykeesNode.flush();
		} catch (IOException | StorageException e) {
			throw new PersistenceException(Messages.Netlicensing_persistenceError_failedToSave, e);
		}
	}

	private byte[] loadFromSecureStorage() throws PersistenceException {
		logger.debug("Loading data from secure storage"); //$NON-NLS-1$
		try {
			ISecurePreferences simonykeesNode = securePreferences.node(SECURE_PREFERENCES_KEY);
			return simonykeesNode.getByteArray(NODE_KEY, null);
		} catch (StorageException e) {
			throw new PersistenceException(Messages.Netlicensing_persistenceError_failedtoLoad,e);
		}
	}

}