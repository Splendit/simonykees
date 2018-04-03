package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import java.util.HashMap;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import eu.jsparrow.license.netlicensing.cleanslate.model.NetlicensingLicenseType;
import eu.jsparrow.license.netlicensing.cleanslate.model.NetlicensingLicenseModel;

public class NetlicensingValidationParametersFactory {

	private static final String SESSION_ID_KEY = "sessionId"; //$NON-NLS-1$
	private static final String ACTION_KEY = "action"; //$NON-NLS-1$
	private static final String ACTION_CHECK_OUT_VAL = "checkOut"; //$NON-NLS-1$
	private static final String ACTION_CHECK_IN_VAL = "checkIn"; //$NON-NLS-1$

	public ValidationParameters createValidationParameters(NetlicensingLicenseModel model) {
		NetlicensingLicenseType type = model.getType();
		ValidationParameters parameters;
		String secret = model.getSecret();
		if (NetlicensingLicenseType.FLOATING == type) {
			parameters = createFloatingParameters(secret, ACTION_CHECK_OUT_VAL);
		} else {
			parameters = createNodeLockedParameters(secret);
		}
		parameters.setProductNumber(model.getProduct());
		parameters.setLicenseeName(model.getName());
		return parameters;
	}
	
	public ValidationParameters createFloatingCheckingParameters(NetlicensingLicenseModel model) {
		ValidationParameters parameters = createFloatingParameters(model.getSecret(), ACTION_CHECK_IN_VAL);
		parameters.setProductNumber(model.getProduct());
		parameters.setLicenseeName(model.getName());
		return parameters;
	}

	protected ValidationParameters createFloatingParameters(String sessionId, String action) {
		ValidationParameters parameters = new ValidationParameters();
		HashMap<String, String> params = new HashMap<>();
		params.put(SESSION_ID_KEY, sessionId);
		params.put(ACTION_KEY, action);
		parameters.setProductModuleValidationParameters(NetlicensingProperties.FLOATING_PRODUCT_MODULE_NUMBER, params);
		return parameters;

	}

	public ValidationParameters createNodeLockedParameters(String secretKey) {
		ValidationParameters parameters = new ValidationParameters();
		parameters.setLicenseeSecret(secretKey);
		return parameters;
	}

}
