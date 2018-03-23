package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import java.util.HashMap;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseType;
import eu.jsparrow.license.netlicensing.cleanslate.model.NetlicensingLicenseModel;

public class NetlicensingValidationParametersFactory {

	private static final String SESSION_ID_KEY = "sessionId"; //$NON-NLS-1$
	private static final String ACTION_KEY = "action"; //$NON-NLS-1$
	private static final String ACTION_CHECK_OUT_VAL = "checkOut"; //$NON-NLS-1$
	private static final String ACTION_CHECK_IN_VAL = "checkIn"; //$NON-NLS-1$

	private String floatingProductModule;

	public NetlicensingValidationParametersFactory(String floatingProductModule) {
		this.floatingProductModule = floatingProductModule;
	}

	public ValidationParameters createValidationParameters(NetlicensingLicenseModel model) {
		LicenseType type = model.getType();
		if (LicenseType.FLOATING == type) {
			return createFloatingParameters(model, ACTION_CHECK_OUT_VAL);
		} else {
			return createNodeLockedParameters(model);
		}
	}
	
	public ValidationParameters createFloatingCheckingParameters(NetlicensingLicenseModel model) {
		return createFloatingParameters(model, ACTION_CHECK_IN_VAL);
	}

	protected ValidationParameters createFloatingParameters(NetlicensingLicenseModel model, String action) {
		ValidationParameters parameters = new ValidationParameters();
		String secretKey = model.getSecret();
		HashMap<String, String> params = new HashMap<>();
		params.put(SESSION_ID_KEY, secretKey);
		params.put(ACTION_KEY, action);
		parameters.setProductModuleValidationParameters(floatingProductModule, params);
		return parameters;

	}

	public ValidationParameters createNodeLockedParameters(NetlicensingLicenseModel model) {
		ValidationParameters parameters = new ValidationParameters();
		String secretKey = model.getSecret();
		parameters.setLicenseeSecret(secretKey);
		return parameters;
	}

}
