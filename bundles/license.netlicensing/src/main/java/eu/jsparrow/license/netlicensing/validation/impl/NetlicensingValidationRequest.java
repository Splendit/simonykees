package eu.jsparrow.license.netlicensing.validation.impl;

import java.lang.invoke.MethodHandles;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.labs64.netlicensing.domain.vo.*;
import com.labs64.netlicensing.exception.*;

import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.license.api.exception.ValidationException;

/**
 * Allows for setting up the connection to NetLicensing API and sending
 * validation requests. Makes use of {@link ResponseEvaluator} to construct the
 * validation result.
 *
 */
public class NetlicensingValidationRequest {

	private static final String DEFAULT_VALIDATION_BASE_URL = "https://go.netlicensing.io/core/v2"; //$NON-NLS-1$
	private static final String BASE_URL_SUFFIX = "/rest"; //$NON-NLS-1$

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private Context restApiContext;

	private ResponseEvaluator responseEvaluator;

	private LicenseeServiceWrapper licenseeService;

	public NetlicensingValidationRequest(ResponseEvaluator responseEvaluator) {
		this(responseEvaluator, DEFAULT_VALIDATION_BASE_URL);
	}

	public NetlicensingValidationRequest(ResponseEvaluator responseEvaluator, String validationBaseUrl) {
		this(responseEvaluator, new LicenseeServiceWrapper());
		this.restApiContext = createAPIContextCall(validationBaseUrl + BASE_URL_SUFFIX);
	}

	public NetlicensingValidationRequest(ResponseEvaluator responseEvaluator, LicenseeServiceWrapper licenseeService) {
		this.responseEvaluator = responseEvaluator;
		this.licenseeService = licenseeService;
	}

	public NetlicensingValidationResult send(String key, ValidationParameters validationParameters)
			throws ValidationException {
		String shortKey = StringUtils.abbreviate(key, 6);
		logger.debug("Sending netlicensing request with key '{}'", shortKey); //$NON-NLS-1$
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

	private Context createAPIContextCall(String validationBaseUrl) {
		Context context = new Context();
		context.setBaseUrl(validationBaseUrl);
		context.setSecurityMode(SecurityMode.APIKEY_IDENTIFICATION);
		context.setApiKey(NetlicensingProperties.API_KEY);
		return context;
	}

}
