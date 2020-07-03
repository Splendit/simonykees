package eu.jsparrow.license.api.persistence;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.license.api.LicensePersistence;
import eu.jsparrow.license.api.exception.PersistenceException;

/**
 * Implementor of {@link LicensePersistence}.
 * 
 */
public abstract class SecureStoragePersistence<T> implements LicensePersistence<T> {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static final String SECURE_PREFERENCES_KEY = "simonykees"; //$NON-NLS-1$

	private ISecurePreferences securePreferences;

	public SecureStoragePersistence(ISecurePreferences securePreferences) {
		this.securePreferences = securePreferences;
		
	}

	protected void saveToSecureStorage(byte[] data, String nodeKey) throws PersistenceException {
		logger.debug("Saving data to secure storage"); //$NON-NLS-1$
		ISecurePreferences simonykeesNode = securePreferences.node(SECURE_PREFERENCES_KEY);
		ISecurePreferences licenseNode = securePreferences.node(SECURE_PREFERENCES_KEY + "/" + nodeKey); //$NON-NLS-1$
		licenseNode.clear();
		
		try {
			simonykeesNode.flush();
			simonykeesNode.putByteArray(nodeKey, data, false);
			simonykeesNode.flush();
		} catch (IOException | StorageException e) {
			throw new PersistenceException(Messages.Netlicensing_persistenceError_failedToSave, e);
		}
	}

	protected byte[] loadFromSecureStorage(String nodeKey) throws PersistenceException {
		logger.debug("Loading data from secure storage"); //$NON-NLS-1$
		try {
			ISecurePreferences simonykeesNode = securePreferences.node(SECURE_PREFERENCES_KEY);
			return simonykeesNode.getByteArray(nodeKey, null);
		} catch (StorageException e) {
			throw new PersistenceException(Messages.Netlicensing_persistenceError_failedtoLoad,e);
		} catch(IllegalArgumentException e) {
			logger.error("Data from secure storate is corrupted.", e); //$NON-NLS-1$
			return null;
		}
	}

}