package eu.jsparrow.license.netlicensing.validation.impl;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseType;

@SuppressWarnings("nls")
public class NetlicensingValidationParametersFactory {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static final String SESSION_ID_KEY = "sessionId"; 
	private static final String ACTION_KEY = "action";
	private static final String ACTION_CHECK_OUT_VAL = "checkOut"; 
	private static final String ACTION_CHECK_IN_VAL = "checkIn"; 


	public ValidationParameters createValidationParameters(NetlicensingLicenseModel model) {
		logger.debug("Creating validation parameters for {}", model); 
		NetlicensingLicenseType type = model.getType();
		ValidationParameters parameters;
		String secret = model.getSecret();
		if (NetlicensingLicenseType.FLOATING == type) {
			logger.debug("License type is floating"); 
			parameters = createFloatingParameters(secret, ACTION_CHECK_OUT_VAL);
		} else {
			logger.debug("License type is node-locked"); 
			parameters = createNodeLockedParameters(secret);
		}
		parameters.setProductNumber(NetlicensingProperties.PRODUCT_NUMBER);
		parameters.setLicenseeName(model.getName());
		logger.debug("Returning parameters {}", parameters);
		return parameters;
	}

	public ValidationParameters createFloatingCheckInParameters(NetlicensingLicenseModel model) {
		ValidationParameters parameters = createFloatingParameters(model.getSecret(), ACTION_CHECK_IN_VAL);
		parameters.setProductNumber(NetlicensingProperties.PRODUCT_NUMBER);
		parameters.setLicenseeName(model.getName());
		return parameters;
	}

	protected ValidationParameters createFloatingParameters(String sessionId, String action) {
		logger.debug("Creating floating parameters for sessionId {} and action {}", sessionId, action); 
		ValidationParameters parameters = new ValidationParameters();
		HashMap<String, String> params = new HashMap<>();
		params.put(SESSION_ID_KEY, sessionId);
		params.put(ACTION_KEY, action);
		parameters.setProductModuleValidationParameters(NetlicensingProperties.FLOATING_PRODUCT_MODULE_NUMBER, params);
		return parameters;

	}

	public ValidationParameters createNodeLockedParameters(String secretKey) {
		logger.debug("Creating node locked for secret {}", secretKey); 
		ValidationParameters parameters = new ValidationParameters();
		parameters.setLicenseeSecret(secretKey);
		return parameters;
	}

}
