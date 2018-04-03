package eu.jsparrow.license.netlicensing;

import eu.jsparrow.license.netlicensing.exception.PersistenceException;
import eu.jsparrow.license.netlicensing.model.LicenseModel;

public interface LicensePersistence {

	LicenseModel load() throws PersistenceException;

	void save(LicenseModel model) throws PersistenceException;
}
