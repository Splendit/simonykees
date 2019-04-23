package eu.jsparrow.license.netlicensing.persistence;

import java.lang.invoke.MethodHandles;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.license.api.persistence.SecureStoragePersistence;

/**
 * 
 * Implementor of {@link SecureStoragePersistence} for persisting and loading
 * license server URL.
 * 
 * @since 3.5.0
 *
 */
public class EndpointSecureStorePersistence extends SecureStoragePersistence<String> {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static final String NODE_KEY = "endpoint"; //$NON-NLS-1$

	public EndpointSecureStorePersistence(ISecurePreferences securePreferences) {
		super(securePreferences);
	}

	@Override
	public String load() throws PersistenceException {
		byte[] bytes = loadFromSecureStorage(NODE_KEY);
		if (bytes == null) {
			logger.debug("No endpoint url was found in storage"); //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}
		return new String(bytes);
	}

	@Override
	public void save(String endpoint) throws PersistenceException {
		byte[] bytes = endpoint.getBytes();
		saveToSecureStorage(bytes, NODE_KEY);
	}

}
