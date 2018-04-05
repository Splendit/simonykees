package eu.jsparrow.license.netlicensing.validation.impl;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;

public class NetlicensingValidationParametersFactory {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static final String SESSION_ID_KEY = "sessionId";  //$NON-NLS-1$
	private static final String ACTION_KEY = "action"; //$NON-NLS-1$
	private static final String ACTION_CHECK_OUT_VAL = "checkOut";  //$NON-NLS-1$
	private static final String ACTION_CHECK_IN_VAL = "checkIn";  //$NON-NLS-1$

	public ValidationParameters createValidationParameters(NetlicensingLicenseModel model) {
		logger.debug("Creating validation parameters for {}", model);  //$NON-NLS-1$
		LicenseType type = model.getType();
		ValidationParameters parameters;
		String secret = model.getSecret();
		if (LicenseType.FLOATING == type) {
			logger.debug("License type is floating");  //$NON-NLS-1$
			parameters = createFloatingParameters(secret, ACTION_CHECK_OUT_VAL);
		} else {
			logger.debug("License type is node-locked");  //$NON-NLS-1$
			parameters = createNodeLockedParameters(secret);
		}
		parameters.setProductNumber(model.getProduct());
		parameters.setLicenseeName(model.getName());
		logger.debug("Returning parameters {}", parameters); //$NON-NLS-1$
		return parameters;
	}

	public ValidationParameters createFloatingCheckingParameters(NetlicensingLicenseModel model) {
		ValidationParameters parameters = createFloatingParameters(model.getSecret(), ACTION_CHECK_IN_VAL);
		parameters.setProductNumber(model.getProduct());
		parameters.setLicenseeName(model.getName());
		return parameters;
	}

	protected ValidationParameters createFloatingParameters(String sessionId, String action) {
		logger.debug("Creating floating parameters for sessionId {} and action {}", sessionId, action);  //$NON-NLS-1$
		ValidationParameters parameters = new ValidationParameters();
		HashMap<String, String> params = new HashMap<>();
		params.put(SESSION_ID_KEY, sessionId);
		params.put(ACTION_KEY, action);
		parameters.setProductModuleValidationParameters(NetlicensingProperties.FLOATING_PRODUCT_MODULE_NUMBER, params);
		return parameters;

	}

	public ValidationParameters createNodeLockedParameters(String secretKey) {
		logger.debug("Creating node locked for secret {}", secretKey);  //$NON-NLS-1$
		ValidationParameters parameters = new ValidationParameters();
		parameters.setLicenseeSecret(secretKey);
		return parameters;
	}

}
