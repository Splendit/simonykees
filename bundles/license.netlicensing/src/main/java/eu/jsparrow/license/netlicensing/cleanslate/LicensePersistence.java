package eu.jsparrow.license.netlicensing.cleanslate;

import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseModel;

public interface LicensePersistence {

	LicenseModel load();
	
	void save(LicenseModel model);
}
