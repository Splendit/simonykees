package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import eu.jsparrow.license.netlicensing.cleanslate.model.NetlicensingLicenseModel;

public class NetlicensingValidationParametersFactory {

	public ValidationParameters createValidationParameters(NetlicensingLicenseModel model) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ValidationParameters createFloatingParameters(NetlicensingLicenseModel model) {
		ValidationParameters parameters = new ValidationParameters();
		String secretKey = model.getSecret();
		
	}
	
	public ValidationParameters createNodeLockedParameters(NetlicensingLicenseModel model) {
		ValidationParameters parameters = new ValidationParameters();
		String secretKey = model.getSecret();
		parameters.setLicenseeSecret(secretKey);
		return parameters;
	}

}
