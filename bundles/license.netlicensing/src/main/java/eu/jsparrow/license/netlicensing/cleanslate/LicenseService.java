package eu.jsparrow.license.netlicensing.cleanslate;

import eu.jsparrow.license.netlicensing.cleanslate.exception.PersistenceException;
import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseModel;

public interface LicenseService {
	
	public LicenseValidationResult validateLicense(LicenseModel model);

	public LicenseModel loadFromPersistence() throws PersistenceException;

	public void saveToPersistence(LicenseModel model) throws PersistenceException;
}
