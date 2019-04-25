package eu.jsparrow.license.netlicensing;

import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.api.LicensePersistence;
import eu.jsparrow.license.api.LicensePersistenceService;
import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.license.netlicensing.persistence.EndpointSecureStorePersistence;

/**
 * Implementation of {@link LicensePersistenceService} for saving and loading
 * encrypted license keys and local agent endpoints to/from the Eclipse Secure
 * Storage.
 * 
 * @since 3.5.0
 */
@Component(property = "licenseType=endpoint")
public class NetlicensingEndpointPersistenceService implements LicensePersistenceService<String> {

	private static final Logger logger = LoggerFactory.getLogger(NetlicensingEndpointPersistenceService.class);

	private LicensePersistence<String> persistence;

	public NetlicensingEndpointPersistenceService() {
		persistence = new EndpointSecureStorePersistence(SecurePreferencesFactory.getDefault());
	}

	@Override
	public String loadFromPersistence() throws PersistenceException {
		logger.debug("Loading endpoint from persistence"); //$NON-NLS-1$
		return persistence.load();
	}

	@Override
	public void saveToPersistence(String endpoint) throws PersistenceException {
		logger.debug("Saving endpoint to persistence"); //$NON-NLS-1$
		persistence.save(endpoint);

	}

}
