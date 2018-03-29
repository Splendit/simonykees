package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import eu.jsparrow.license.netlicensing.cleanslate.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.cleanslate.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.validation.LicenseValidation;

public class NetlicensingLicenseValidation implements LicenseValidation {

	private static final String FLOATING_PRODUCT_MODULE_NUMBER = ""; // LicenseManager.PRODUCT_MODULE_NUMBER

	private NetlicensingLicenseModel model;
	
	private NetlicensingLicenseCache licenseCache;

	private NetlicensingValidationParametersFactory parametersFactory;

	private NetlicensingValidationRequest validationRequest;

	public NetlicensingLicenseValidation(NetlicensingLicenseModel model) {
		this.model = model;
		this.licenseCache = NetlicensingLicenseCache.get();
		this.parametersFactory = new NetlicensingValidationParametersFactory(FLOATING_PRODUCT_MODULE_NUMBER);
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
		if (!licenseCache.isInvalid()) {
			return licenseCache.getLastResult();
		}

		ValidationParameters validationParameters = parametersFactory.createValidationParameters(model);
		String licenseeNumber = model.getKey();
		LicenseValidationResult licensingValidationResult = validationRequest.send(licenseeNumber,
				validationParameters);
		licenseCache.updateCache(licensingValidationResult);
		return licensingValidationResult;
	}
}
