package eu.jsparrow.license.netlicensing.validation.impl;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseType;
import eu.jsparrow.license.netlicensing.validation.LicenseValidation;

public class NetlicensingLicenseValidation implements LicenseValidation {

	private NetlicensingLicenseModel model;

	private NetlicensingLicenseCache licenseCache;

	private NetlicensingValidationParametersFactory parametersFactory;

	private NetlicensingValidationRequest validationRequest;

	public NetlicensingLicenseValidation(NetlicensingLicenseModel model) {
		this.model = model;
		this.licenseCache = new NetlicensingLicenseCache();
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
	public LicenseValidationResult validate() throws ValidationException {
		String licenseeNumber = model.getKey();
		LicenseValidationResult result = licenseCache.get(licenseeNumber);
		if (result != null) {
			return result;
		}

		ValidationParameters validationParameters = parametersFactory.createValidationParameters(model);
		NetlicensingValidationResult licensingValidationResult = validationRequest.send(licenseeNumber,
				validationParameters);
		if (licensingValidationResult.isValid()) {
			licenseCache.updateCache(licenseeNumber, licensingValidationResult);
		}
		return licensingValidationResult;
	}

	@Override
	public void checkIn() throws ValidationException {
		if (model.getType() != NetlicensingLicenseType.FLOATING) {
			throw new ValidationException(
					String.format("Failed to check in license. Invalid license type '%s'", model.getType()));
		}
		ValidationParameters validationParameters = parametersFactory.createFloatingCheckingParameters(model);
		LicenseValidationResult checkInResult = validationRequest.send(model.getKey(), validationParameters);
	}
}
