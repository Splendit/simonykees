package eu.jsparrow.registration;

import java.lang.invoke.MethodHandles;

import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.api.LicensePersistence;
import eu.jsparrow.license.api.LicensePersistenceService;
import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.license.api.persistence.AESEncryption;
import eu.jsparrow.registration.persistence.RegistrationSecureStoragePersistence;

/**
 * Service for persisting customer registration. Provides functionality for
 * persisting a customer registration and loading it from the persistence
 * storage.
 * 
 * @since 3.0.0
 *
 */
@Component(property = "licenseType=registration")
public class CustomerRegistrationPersistenceService implements LicensePersistenceService<String> {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private LicensePersistence<String> persistence;

	CustomerRegistrationPersistenceService(RegistrationSecureStoragePersistence persistence) {
		this.persistence = persistence;
	}
	
	public CustomerRegistrationPersistenceService() {
		persistence = new RegistrationSecureStoragePersistence(SecurePreferencesFactory.getDefault(),
				new AESEncryption());
	}

	@Override
	public String loadFromPersistence() throws PersistenceException {
		logger.debug("Loading registration model from persistence"); //$NON-NLS-1$
		return persistence.load();
	}

	@Override
	public void saveToPersistence(String model) throws PersistenceException {
		logger.debug("Saving registration model '{}' ", model); //$NON-NLS-1$
		persistence.save(model);
	}

}
