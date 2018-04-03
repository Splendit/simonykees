package eu.jsparrow.license.api;

import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.license.api.exception.ValidationException;

public interface LicenseService {

	public LicenseValidationResult validate(LicenseModel model) throws ValidationException;
	
	public void checkIn(LicenseModel model) throws ValidationException;

	public LicenseModel loadFromPersistence() throws PersistenceException;

	public void saveToPersistence(LicenseModel model) throws PersistenceException;
	
}
