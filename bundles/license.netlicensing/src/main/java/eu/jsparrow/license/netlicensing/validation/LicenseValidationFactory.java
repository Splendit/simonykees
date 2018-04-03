package eu.jsparrow.license.netlicensing.validation;

import eu.jsparrow.license.netlicensing.model.*;
import eu.jsparrow.license.netlicensing.validation.impl.DemoLicenseValidation;
import eu.jsparrow.license.netlicensing.validation.impl.NetlicensingLicenseValidation;

public class LicenseValidationFactory {
	
	public LicenseValidation create(LicenseModel model) {
		if(model instanceof NetlicensingLicenseModel) {
			return new NetlicensingLicenseValidation((NetlicensingLicenseModel)model);
		}
		else if(model instanceof DemoLicenseModel) {
			return new DemoLicenseValidation((DemoLicenseModel)model);
		}
		return null;
	}

}
