package at.splendit.simonykees.core.license;

import java.time.ZonedDateTime;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

public class TryAndBuyModel extends LicenseModel {

	private String secretKey;
	
	public TryAndBuyModel(String productModuleNumber, ZonedDateTime expireDate, String secretKey) {
		super(productModuleNumber, LicenseType.TRY_AND_BUY, expireDate);
		setSecretKey(secretKey);
	}

	@Override
	protected ValidationParameters getValidationParameters() {
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
