package eu.jsparrow.license.netlicensing;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.exception.PersistenceException;

public interface LicensePersistence {

	LicenseModel load() throws PersistenceException;

	void save(LicenseModel model) throws PersistenceException;
}
