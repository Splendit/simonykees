package eu.jsparrow.license.netlicensing.cleanslate;

import eu.jsparrow.license.netlicensing.cleanslate.exception.PersistenceException;
import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseModel;

public interface LicensePersistence {

	LicenseModel load() throws PersistenceException;

	void save(LicenseModel model) throws PersistenceException;
}
