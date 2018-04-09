package eu.jsparrow.license.netlicensing;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.LicensePersistenceService;
import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.license.netlicensing.persistence.AESEncryption;
import eu.jsparrow.license.netlicensing.persistence.SecureStoragePersistence;

/**
 * Implementor of {@link LicensePersistenceService} using {@link ISecurePreferences}.
 *
 */
@Component
public class NetlicensingLicensePersistenceService implements LicensePersistenceService {

	private static final Logger logger = LoggerFactory.getLogger(NetlicensingLicensePersistenceService.class);

	private LicensePersistence persistence;
	
	public NetlicensingLicensePersistenceService() {
		this.persistence = new SecureStoragePersistence(SecurePreferencesFactory.getDefault(), new AESEncryption());
	}
	
	@Override
	public LicenseModel loadFromPersistence() throws PersistenceException {
		logger.debug("Loading model from persistence");
		return persistence.load();
	}

	@Override
	public void saveToPersistence(LicenseModel model) throws PersistenceException {
		logger.debug("Saving {}", model);
		persistence.save(model);
	}

}
