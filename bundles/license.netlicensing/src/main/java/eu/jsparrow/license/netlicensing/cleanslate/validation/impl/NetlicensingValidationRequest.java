package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import com.labs64.netlicensing.domain.vo.*;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.service.LicenseeService;

import eu.jsparrow.license.netlicensing.cleanslate.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseStatus;
import eu.jsparrow.license.netlicensing.cleanslate.validation.ValidationStatus;

public class NetlicensingValidationRequest {

	private static final String NETLICENSING_VALIDATION_BASE_URL = "https://go.netlicensing.io/core/v2/rest";
	private static final String REST_API_AUTHENTICATION_TOKEN = ""; // LicenseManager.PASS_APIKEY

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

	public LicenseValidationResult send(String key, ValidationParameters validationParameters) {
		try {
			ValidationResult netLicensingResponse = licenseeService.validate(restApiContext, key, validationParameters);
			return responseEvaluator.evaluateResult(netLicensingResponse);
		} catch (NetLicensingException e) {
			// TODO: Distinguish between no connection and licensee doesnt exist
			ValidationStatus validationStatus = new ValidationStatus(false, LicenseStatus.CONNECTION_FAILURE);
			return new LicenseValidationResult(responseEvaluator.getLicensingModel(), validationStatus);
		}
	}

	private Context createAPIContextCall() {
		Context context = new Context();
		context.setBaseUrl(NETLICENSING_VALIDATION_BASE_URL);
		context.setSecurityMode(SecurityMode.APIKEY_IDENTIFICATION);
		context.setApiKey(REST_API_AUTHENTICATION_TOKEN);
		return context;
	}

	
}
