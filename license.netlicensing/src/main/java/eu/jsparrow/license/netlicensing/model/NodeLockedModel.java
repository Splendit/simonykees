package eu.jsparrow.license.netlicensing.model;

import java.time.ZonedDateTime;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import eu.jsparrow.license.netlicensing.LicenseType;

/**
 * A representation of the Node Locked license. Responsible for constructing 
 * validation parameters related to the Node Locked license.
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public class NodeLockedModel extends LicenseModel {

	private String secretKey;
	
	public NodeLockedModel(ZonedDateTime expireDate, String secretKey) {
		super(LicenseType.NODE_LOCKED, expireDate);
		setSecretKey(secretKey);
	}

	@Override
	public ValidationParameters getValidationParameters() {
		
		String key = getSecretKey();
		
		ValidationParameters validationParams = new ValidationParameters();
		validationParams.setLicenseeSecret(key);
		
		return validationParams;
	}

	private String getSecretKey() {
		return secretKey;
	}

	private void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

}
