package at.splendit.simonykees.core.license.model;

import java.time.ZonedDateTime;
import java.util.HashMap;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import at.splendit.simonykees.core.license.LicenseType;

public class FloatingModel extends LicenseModel {

	private final String SESSION_ID_KEY = "sessionId"; //$NON-NLS-1$
	private final String ACTION_KEY = "action"; //$NON-NLS-1$
	private final String ACTION_CHECK_OUT_VAL = "checkOut"; //$NON-NLS-1$
	private final String ACTION_CHECK_IN_VAL = "checkIn"; //$NON-NLS-1$

	private String sessionId;
	private String productModuleNumber;

	public FloatingModel(String productModuleNumber, ZonedDateTime expireDate, String sessionId) {
		super(LicenseType.FLOATING, expireDate);
		setSessionId(sessionId);
		setProductModuleNumber(productModuleNumber);

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
	
	public String getProductModuleNumber() {
		return this.productModuleNumber;
	}
	
	private void setProductModuleNumber(String productModuleNumber){
		this.productModuleNumber = productModuleNumber;
	}


}
