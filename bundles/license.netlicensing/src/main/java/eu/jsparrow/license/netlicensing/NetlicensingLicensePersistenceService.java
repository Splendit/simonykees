package eu.jsparrow.license.netlicensing;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.LicensePersistence;
import eu.jsparrow.license.api.LicensePersistenceService;
import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.license.api.persistence.AESEncryption;
import eu.jsparrow.license.netlicensing.persistence.LicenseSecureStoragePersistence;

/**
 * Implementor of {@link LicensePersistenceService} using {@link ISecurePreferences}.
 *
 */
@Component
public class NetlicensingLicensePersistenceService implements LicensePersistenceService<LicenseModel> {

	private static final Logger logger = LoggerFactory.getLogger(NetlicensingLicensePersistenceService.class);

	private LicensePersistence<LicenseModel> persistence;
	
	public NetlicensingLicensePersistenceService() {
		this.persistence = new LicenseSecureStoragePersistence(SecurePreferencesFactory.getDefault(), new AESEncryption());
	}
	
	@Override
	public LicenseModel loadFromPersistence() throws PersistenceException {
		logger.debug("Loading model from persistence"); //$NON-NLS-1$
		return persistence.load();
	}

	@Override
	public void saveToPersistence(LicenseModel model) throws PersistenceException {
		logger.debug("Saving '{}'", model); //$NON-NLS-1$
		persistence.save(model);
	}

}
