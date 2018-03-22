package eu.jsparrow.license.netlicensing.cleanslate.validation;

import eu.jsparrow.license.netlicensing.cleanslate.model.DemoLicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.DemoLicenseValidation;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.NetlicensingLicenseValidation;

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
