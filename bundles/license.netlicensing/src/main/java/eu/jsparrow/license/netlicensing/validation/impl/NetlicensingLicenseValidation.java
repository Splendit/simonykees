package eu.jsparrow.license.netlicensing.validation.impl;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import eu.jsparrow.i18n.ExceptionMessages;
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

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private NetlicensingLicenseModel model;

	private NetlicensingLicenseCache licenseCache;

	private NetlicensingValidationParametersFactory parametersFactory;

	private NetlicensingValidationRequest validationRequest;

	public NetlicensingLicenseValidation(NetlicensingLicenseModel model) {
		ResponseEvaluator responseEvaluator = initState(model);
		this.validationRequest = new NetlicensingValidationRequest(responseEvaluator);
	}

	public NetlicensingLicenseValidation(NetlicensingLicenseModel model, String endpoint) {
		ResponseEvaluator responseEvaluator = initState(model);
		this.validationRequest = new NetlicensingValidationRequest(responseEvaluator, endpoint);
	}

	private ResponseEvaluator initState(NetlicensingLicenseModel model) {
		this.model = model;
		this.licenseCache = new NetlicensingLicenseCache();
		this.parametersFactory = new NetlicensingValidationParametersFactory();
		String licenseeNr = model.getKey();
		return new ResponseEvaluator(licenseeNr);
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

		/*
		 * If no license type is defined we need to send one validation call first to
		 * get the license type. We need to do this because NetLicensing doesn't return
		 * the correct validation result for unknown license types.
		 */
		if (model.getType() == LicenseType.NONE) {
			updateModelType();
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

	private void updateModelType() throws ValidationException {
		try {
			LicenseValidationResult result = validationRequest.send(model.getKey(),
					parametersFactory.createVerifyParameters(model));
			/*
			 * The validation result contains the actual license type so we update the
			 * license model with the license type so the next request will return a
			 * validation result specific to this license type.
			 */
			model = new NetlicensingLicenseModel(model.getKey(), model.getSecret(), model.getProductNr(),
					model.getModuleNr(), result.getLicenseType(), model.getName(), result.getExpirationDate());
		} catch (LinkageError | ClassCastException e) {
			/* SIM-1573 Feedback Improvement
				We were not able to reproduce the class path collision in the OSGi environment.
				So we are currently creating a new exception to improve the user interaction. 
			
			*/
			throw new ValidationException(ExceptionMessages.NetlicensingLicenseValidation_LinkageError,e);
		}

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

	@Override
	public void reserveQuantity(int quantity) throws ValidationException {
		if(model.getType() != LicenseType.PAY_PER_USE) {
			logger.warn("Can only reserve quantity in Pay-Per-Use license. Ignoring reserveQuantity call"); //$NON-NLS-1$
			return;
		}
		ValidationParameters parameters = parametersFactory.createPayPerUseReserveParameters(model, quantity);
		NetlicensingValidationResult licensingValidationResult = validationRequest.send(model.getKey(), parameters);
		String licenseeNumber = model.getKey();
		licenseCache.updateCache(licenseeNumber, licensingValidationResult);
	}
}
