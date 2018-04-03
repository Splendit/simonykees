package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import eu.jsparrow.license.netlicensing.cleanslate.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.cleanslate.exception.ValidationException;
import eu.jsparrow.license.netlicensing.cleanslate.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.model.NetlicensingLicenseType;
import eu.jsparrow.license.netlicensing.cleanslate.validation.LicenseValidation;

public class NetlicensingLicenseValidation implements LicenseValidation {

	private NetlicensingLicenseModel model;
	
	private NetlicensingLicenseCache licenseCache;

	private NetlicensingValidationParametersFactory parametersFactory;

	private NetlicensingValidationRequest validationRequest;

	public NetlicensingLicenseValidation(NetlicensingLicenseModel model) {
		this.model = model;
		this.licenseCache = NetlicensingLicenseCache.get();
		this.parametersFactory = new NetlicensingValidationParametersFactory();
		this.validationRequest = new NetlicensingValidationRequest(new ResponseEvaluator(model));
	}

	public NetlicensingLicenseValidation(NetlicensingLicenseModel model, NetlicensingLicenseCache cache,
			NetlicensingValidationParametersFactory parametersFactory, NetlicensingValidationRequest request) {
		this.model = model;
		this.licenseCache = cache;
		this.parametersFactory = parametersFactory;
		this.validationRequest = request;
	}

	@Override
	public LicenseValidationResult validate() {
		LicenseValidationResult licensingValidationResult = licenseCache.getValidationResultFor(model);
		if(licensingValidationResult != null) {
			return licensingValidationResult;
		}
		
		ValidationParameters validationParameters = parametersFactory.createValidationParameters(model);
		String licenseeNumber = model.getKey();
		licensingValidationResult = validationRequest.send(licenseeNumber,
				validationParameters);
		licenseCache.updateCache(licensingValidationResult);
		return licensingValidationResult;
	}

	@Override
	public void checkIn() throws ValidationException {
		if (model.getType() != NetlicensingLicenseType.FLOATING) {
			throw new ValidationException(String.format("Failed to check in license. Invalid license type '%s'", model.getType()));
		}
		ValidationParameters validationParameters = parametersFactory.createFloatingCheckingParameters(model);
		LicenseValidationResult checkInResult = validationRequest.send(model.getKey(), validationParameters);
		licenseCache.updateCache(checkInResult);
	}
}
