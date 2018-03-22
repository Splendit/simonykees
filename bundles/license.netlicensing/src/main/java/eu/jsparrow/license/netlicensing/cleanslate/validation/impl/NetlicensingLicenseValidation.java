package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import com.labs64.netlicensing.domain.vo.ValidationResult;

import eu.jsparrow.license.netlicensing.cleanslate.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.cleanslate.SecureStoragePersistence;
import eu.jsparrow.license.netlicensing.cleanslate.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.validation.LicenseValidation;

public class NetlicensingLicenseValidation implements LicenseValidation{
	
	private NetlicensingLicenseCache licenseCache;
	
	private SecureStoragePersistence persistence;
	
	private NetlicensingLicenseModel model;

	public NetlicensingLicenseValidation(NetlicensingLicenseModel model) {
		
	}

	@Override
	public LicenseValidationResult validate() {
		if(licenseCache.isInvalid()) {
			LicenseValidationResult newValidationResult = executeNewValidation();	
			
			licenseCache.updateCache(newValidationResult);
		}
		LicenseValidationResult result = licenseCache.getLastResult();
		

		return null;
	}

	private LicenseValidationResult executeNewValidation() {
		ValidationResult result;
		
		return null;
	}
}
