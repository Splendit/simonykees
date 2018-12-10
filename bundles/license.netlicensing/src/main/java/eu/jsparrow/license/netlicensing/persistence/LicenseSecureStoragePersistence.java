package eu.jsparrow.license.netlicensing.persistence;

import java.lang.invoke.MethodHandles;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.LicensePersistence;
import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.license.api.persistence.IEncryption;
import eu.jsparrow.license.api.persistence.SecureStoragePersistence;
import eu.jsparrow.license.netlicensing.NetlicensingLicenseModelFactoryService;

/**
 * Implementor of {@link LicensePersistence}.
 * 
 */
public class LicenseSecureStoragePersistence extends SecureStoragePersistence<LicenseModel> {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static final String NODE_KEY = "license-model"; //$NON-NLS-1$

	public LicenseSecureStoragePersistence(ISecurePreferences securePreferences, IEncryption encryption) {
		super(securePreferences, encryption);
	}

	@Override
	public LicenseModel load() throws PersistenceException {
		byte[] encryptedModel = loadFromSecureStorage(NODE_KEY);
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
		saveToSecureStorage(encryption.encrypt(modelAsBytes), NODE_KEY);
	}

}