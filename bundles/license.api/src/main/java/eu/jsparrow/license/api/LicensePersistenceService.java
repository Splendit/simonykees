package eu.jsparrow.license.api;

import eu.jsparrow.license.api.exception.PersistenceException;

public interface LicensePersistenceService {
	
	public LicenseModel loadFromPersistence() throws PersistenceException;

	public void saveToPersistence(LicenseModel model) throws PersistenceException;	
}
