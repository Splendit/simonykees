package eu.jsparrow.license.netlicensing.validation.impl;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.validation.LicenseValidation;

/**
 * Implementor of {@link LicenseValidation} for NetLicensing. Validating a
 * license using NetLicensing involves making a request to the NetLicensing API.
 * 
 */
public class NetlicensingLicenseValidation implements LicenseValidation {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private NetlicensingLicenseModel model;

	private NetlicensingLicenseCache licenseCache;

	private NetlicensingValidationParametersFactory parametersFactory;

	private NetlicensingValidationRequest validationRequest;

	public NetlicensingLicenseValidation(NetlicensingLicenseModel model) {
		this.model = model;
		this.licenseCache = new NetlicensingLicenseCache();
		this.parametersFactory = new NetlicensingValidationParametersFactory();
		this.validationRequest = new NetlicensingValidationRequest(new ResponseEvaluator(model.getKey()));
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
		logger.debug("Validating netlicensing license"); //$NON-NLS-1$

		String licenseeNumber = model.getKey();
		LicenseValidationResult result = licenseCache.get(licenseeNumber);
		if (result != null) {
			logger.debug("Found existing result {}", result); //$NON-NLS-1$
			return result;
		}
		// If no license type is defined we need to send one validation call
		// first to get the license type. We need to do this because
		// NetLicensing doesn't return the correct validation result for unknown
		// license types. 
		if (model.getType() == LicenseType.NONE) {
			model = getModelTypeByRequest();
		}
		ValidationParameters validationParameters = parametersFactory.createValidationParameters(model);
		NetlicensingValidationResult licensingValidationResult = validationRequest.send(licenseeNumber,
				validationParameters);
		if (licensingValidationResult.isValid()) {
			licenseCache.updateCache(licenseeNumber, licensingValidationResult);
		}
		logger.debug("Returning {}", licensingValidationResult); //$NON-NLS-1$
		return licensingValidationResult;
	}

	private NetlicensingLicenseModel getModelTypeByRequest() throws ValidationException {
		LicenseValidationResult result = validationRequest.send(model.getKey(), parametersFactory.createVerifyParameters(model.getSecret()));
		model = new NetlicensingLicenseModel(model.getKey(), model.getSecret(), result.getLicenseType(), model.getName(),
				result.getExpirationDate());
		return model;
	}

	@Override
	public void checkIn() throws ValidationException {
		if (model.getType() != LicenseType.FLOATING) {
			logger.warn("Can only check in floating licenses. Ignoring check-in call"); //$NON-NLS-1$
			return;
		}
		ValidationParameters validationParameters = parametersFactory.createFloatingCheckInParameters(model);
		validationRequest.send(model.getKey(), validationParameters);
	}
}
