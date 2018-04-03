package eu.jsparrow.license.netlicensing.cleanslate;

import eu.jsparrow.license.netlicensing.cleanslate.exception.PersistenceException;
import eu.jsparrow.license.netlicensing.cleanslate.exception.ValidationException;
import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseModel;

public interface LicenseService {

	public LicenseValidationResult validate(LicenseModel model) throws ValidationException;
	
	public void checkIn(LicenseModel model) throws ValidationException;

	public LicenseModel loadFromPersistence() throws PersistenceException;

	public void saveToPersistence(LicenseModel model) throws PersistenceException;
	
}
