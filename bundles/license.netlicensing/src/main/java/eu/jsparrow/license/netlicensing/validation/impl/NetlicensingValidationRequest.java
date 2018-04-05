package eu.jsparrow.license.netlicensing.validation.impl;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.labs64.netlicensing.domain.vo.*;
import com.labs64.netlicensing.exception.*;

import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.license.api.exception.ValidationException;

public class NetlicensingValidationRequest {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private Context restApiContext;

	private ResponseEvaluator responseEvaluator;

	private LicenseeServiceWrapper licenseeService;

	public NetlicensingValidationRequest(ResponseEvaluator responseEvaluator) {
		this(responseEvaluator, new LicenseeServiceWrapper());
		this.restApiContext = createAPIContextCall();

	}

	public NetlicensingValidationRequest(ResponseEvaluator responseEvaluator, LicenseeServiceWrapper licenseeService) {
		this.responseEvaluator = responseEvaluator;
		this.licenseeService = licenseeService;
	}

	public NetlicensingValidationResult send(String key, ValidationParameters validationParameters)
			throws ValidationException {
		logger.debug("Sending netlicensing request with key '{}' and {}", key, validationParameters); //$NON-NLS-1$
		try {
			ValidationResult netLicensingResponse = licenseeService.validate(restApiContext, key, validationParameters);
			return responseEvaluator.evaluateResult(netLicensingResponse);
		} catch (RestException e) {
			throw new ValidationException(ExceptionMessages.Netlicensing_validationException_failedtoConnectToServer,
					e);
		} catch (ServiceException e) {
			throw new ValidationException(ExceptionMessages.Netlicensing_validationException_productNumberNotExisting,
					e);
		} catch (NetLicensingException e) {
			throw new ValidationException(ExceptionMessages.Netlicensing_validationException_unknownError, e);
		}
	}

	private Context createAPIContextCall() {
		Context context = new Context();
		context.setBaseUrl(NetlicensingProperties.VALIDATION_BASE_URL);
		context.setSecurityMode(SecurityMode.APIKEY_IDENTIFICATION);
		context.setApiKey(NetlicensingProperties.API_KEY);
		return context;
	}

}
