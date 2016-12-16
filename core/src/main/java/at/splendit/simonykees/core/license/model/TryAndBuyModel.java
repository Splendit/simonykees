package at.splendit.simonykees.core.license.model;

import java.time.ZonedDateTime;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import at.splendit.simonykees.core.license.LicenseType;

public class TryAndBuyModel extends LicenseModel {

	private String secretKey;
	
	public TryAndBuyModel(ZonedDateTime expireDate, String secretKey) {
		super(LicenseType.TRY_AND_BUY, expireDate);
		setSecretKey(secretKey);
	}

	@Override
	public ValidationParameters getValidationParameters() {
		ValidationParameters validationParams = new ValidationParameters();
		validationParams.setLicenseeSecret(getSecretKey());
		return validationParams;
	}

	public String getSecretKey() {
		return secretKey;
	}

	private void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

}
