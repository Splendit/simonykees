package eu.jsparrow.license.netlicensing.model;

import java.time.ZonedDateTime;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import eu.jsparrow.license.netlicensing.LicenseType;

/**
 * A representation of the Try and Buy license. Responsible for 
 * generating validation parameters relevant to Try and Buy license model.
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
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
