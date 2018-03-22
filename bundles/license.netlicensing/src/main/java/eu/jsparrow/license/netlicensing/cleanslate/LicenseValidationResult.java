package eu.jsparrow.license.netlicensing.cleanslate;

import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.validation.ValidationStatus;

public interface LicenseValidationResult {

	LicenseModel getType();
	
	ValidationStatus getStatus();
}
