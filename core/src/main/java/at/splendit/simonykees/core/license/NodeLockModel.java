package at.splendit.simonykees.core.license;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

public class NodeLockModel extends LicenseModel {

	private String secretKey;
	
	public NodeLockModel(String productName, String productModuleNumber, Long expireDate, String secretKey) {
		super(productName, productModuleNumber, LicenseType.NODE_LOCKED, expireDate);
		setSecretKey(secretKey);
	}

	@Override
	protected ValidationParameters getValidationParameters() {
		
		String secretKey = getSecretKey();
		
		ValidationParameters validationParams = new ValidationParameters();
		validationParams.setLicenseeSecret(secretKey);
		
		return validationParams;
	}

	private String getSecretKey() {
		return secretKey;
	}

	private void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

}
