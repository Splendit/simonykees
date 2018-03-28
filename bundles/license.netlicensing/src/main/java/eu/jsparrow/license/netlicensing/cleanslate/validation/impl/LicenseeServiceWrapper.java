package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import com.labs64.netlicensing.domain.vo.*;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.service.LicenseeService;

public class LicenseeServiceWrapper {

	public ValidationResult validate(Context context, String key, ValidationParameters parameters)
			throws NetLicensingException {
		return LicenseeService.validate(context, key, parameters);
	}
}
