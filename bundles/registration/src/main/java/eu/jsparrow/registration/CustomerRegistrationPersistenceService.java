package eu.jsparrow.registration;

import java.lang.invoke.MethodHandles;

import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.api.LicensePersistence;
import eu.jsparrow.license.api.LicensePersistenceService;
import eu.jsparrow.license.api.RegistrationModel;
import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.license.api.persistence.AESEncryption;
import eu.jsparrow.registration.persistence.RegistrationSecureStoragePersistence;

@Component
public class CustomerRegistrationPersistenceService implements LicensePersistenceService<RegistrationModel> {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private LicensePersistence<RegistrationModel> persistence;

	public CustomerRegistrationPersistenceService() {
		persistence = new RegistrationSecureStoragePersistence(SecurePreferencesFactory.getDefault(),
				new AESEncryption());
	}

	@Override
	public RegistrationModel loadFromPersistence() throws PersistenceException {
		logger.debug("Loading registration model from persistence"); //$NON-NLS-1$
		return persistence.load();
	}

	@Override
	public void saveToPersistence(RegistrationModel model) throws PersistenceException {
		logger.debug("Saving registration model '{}' ", model); //$NON-NLS-1$
		persistence.save(model);
	}

}
