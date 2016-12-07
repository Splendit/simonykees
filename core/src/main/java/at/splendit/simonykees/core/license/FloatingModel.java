package at.splendit.simonykees.core.license;

import java.time.Instant;
import java.util.HashMap;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

public class FloatingModel extends LicenseModel {

	private final String SESSION_ID_KEY = "sessionIdN"; //$NON-NLS-1$
	private final String ACTION_KEY = "actionN"; //$NON-NLS-1$
	private final String ACTION_CHECK_OUT_VAL = "checkOut"; //$NON-NLS-1$
	private final String ACTION_CHECK_IN_VAL = "checkIn"; //$NON-NLS-1$

	private String sessionId;

	public FloatingModel(String productNumber, String productModuleNumber, Instant expireDate, String sessionId) {
		super(productNumber, productModuleNumber, LicenseType.FLOATING, expireDate);
		setSessionId(sessionId);

	}

	public String getSessionId() {
		return this.sessionId;
	}
	
	private void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public ValidationParameters getValidationParameters() {

		HashMap<String, String> params = new HashMap<>();
		params.put(SESSION_ID_KEY, getSessionId());
		params.put(ACTION_KEY, ACTION_CHECK_OUT_VAL);

		String productModuleNumber = getProductModuleNumber();

		ValidationParameters validationParams = new ValidationParameters();
		validationParams.setProductModuleValidationParameters(productModuleNumber, params);

		return validationParams;
	}

	public ValidationParameters getCheckInValidationParameters() {
		HashMap<String, String> params = new HashMap<>();
		params.put(SESSION_ID_KEY, getSessionId());
		params.put(ACTION_KEY, ACTION_CHECK_IN_VAL);

		String productModuleNumber = getProductModuleNumber();

		ValidationParameters validationParams = new ValidationParameters();
		validationParams.setProductModuleValidationParameters(productModuleNumber, params);

		return validationParams;
	}

}
