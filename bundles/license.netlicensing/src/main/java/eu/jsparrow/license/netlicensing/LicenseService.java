package eu.jsparrow.license.netlicensing;

import eu.jsparrow.license.netlicensing.exception.PersistenceException;
import eu.jsparrow.license.netlicensing.exception.ValidationException;
import eu.jsparrow.license.netlicensing.model.LicenseModel;

public interface LicenseService {

	public LicenseValidationResult validate(LicenseModel model) throws ValidationException;
	
	public void checkIn(LicenseModel model) throws ValidationException;

	public LicenseModel loadFromPersistence() throws PersistenceException;

	public void saveToPersistence(LicenseModel model) throws PersistenceException;
	
}
