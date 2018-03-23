package eu.jsparrow.license.netlicensing.cleanslate;

import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.model.ValidationException;

public interface LicensePersistence {

	LicenseModel load() throws ValidationException;
	
	void save(LicenseModel model) throws ValidationException;
}
