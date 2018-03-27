package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.SecurityMode;
import com.labs64.netlicensing.domain.vo.ValidationParameters;
import com.labs64.netlicensing.domain.vo.ValidationResult;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.service.LicenseeService;

import eu.jsparrow.license.netlicensing.cleanslate.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.cleanslate.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.persistence.SecureStoragePersistence;
import eu.jsparrow.license.netlicensing.cleanslate.validation.LicenseValidation;

public class NetlicensingLicenseValidation implements LicenseValidation {

	private static final String NETLICENSING_VALIDATION_BASE_URL = "https://go.netlicensing.io/core/v2/rest";
	private static final String FLOATING_PRODUCT_MODULE_NUMBER = ""; // LicenseManager.PRODUCT_MODULE_NUMBER
	private static final String REST_API_AUTHENTICATION_TOKEN = ""; // LicenseManager.PASS_APIKEY

	private NetlicensingLicenseCache licenseCache;
	private SecureStoragePersistence persistence;
	private NetlicensingLicenseModel model;
	private NetlicensingValidationParametersFactory parametersFactory;
	private Context restApiContext;
	private ResponseEvaluator responseEvaluator;

	public NetlicensingLicenseValidation(NetlicensingLicenseModel model) {
		this.model = model;
		this.licenseCache = getLicenseCacheInstance();
		this.parametersFactory = new NetlicensingValidationParametersFactory(FLOATING_PRODUCT_MODULE_NUMBER);
		this.restApiContext = createAPIContextCall();
		this.responseEvaluator = createResponseEvaluator(model);
	}

	@Override
	public LicenseValidationResult validate() {
		if (licenseCache.isInvalid()) {

			try {
				ValidationResult netLicensingRespone = executeNewValidation();
				LicenseValidationResult newValidationResult = responseEvaluator.evaluateResult(netLicensingRespone);
				licenseCache.updateCache(newValidationResult);
			} catch (NetLicensingException e) {
				// TODO check the exception type. Set the status properly. or throw a validation exception
				e.printStackTrace();
			}

		}
		return licenseCache.getLastResult();
	}

	protected ValidationResult executeNewValidation() throws NetLicensingException {

		NetlicensingLicenseModel license = getNetlicensingLicenseModel();
		String licenseeNumber = license.getKey();
		ValidationParameters validationParameters = parametersFactory.createValidationParameters(model);

		return sendValidationRequest(licenseeNumber, validationParameters, restApiContext);
	}

	protected ValidationResult sendValidationRequest(String licenseeNumber, ValidationParameters validationParameters,
			Context context) throws NetLicensingException {
		return LicenseeService.validate(context, licenseeNumber, validationParameters);
	}

	private Context createAPIContextCall() {
		Context context = new Context();
		context.setBaseUrl(NETLICENSING_VALIDATION_BASE_URL);
		context.setSecurityMode(SecurityMode.APIKEY_IDENTIFICATION);
		context.setApiKey(REST_API_AUTHENTICATION_TOKEN);
		return context;
	}

	private NetlicensingLicenseModel getNetlicensingLicenseModel() {
		return this.model;
	}

	protected ResponseEvaluator createResponseEvaluator(NetlicensingLicenseModel model) {
		return new ResponseEvaluator(model);
	}

	protected NetlicensingLicenseCache getLicenseCacheInstance() {
		return new NetlicensingLicenseCache();
	}
}
