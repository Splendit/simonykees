package at.splendit.simonykees.core.License;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

public class LicenseeEntity {
	public static final String PRODUCT_NUMBER = "test-01";

	private String licenseeName;
	private String licenseeNumber;
	private ValidationParameters validationParams;

	public LicenseeEntity(String licenseeName, String licenseeNumber) {
		setLicenseeName(licenseeName);
		setLicenseeNumber(licenseeNumber);
		initValidationParameters(licenseeName);
		
	}
	
	private void initValidationParameters(String licenseeName) {
		this.validationParams = new ValidationParameters();
		validationParams.setLicenseeName(licenseeName);
		validationParams.setProductNumber(PRODUCT_NUMBER);
	}

	public String getLicenseeName() {
		return licenseeName;
	}

	private void setLicenseeName(String licenseeName) {
		this.licenseeName = licenseeName;
	}

	public String getLicenseeNumber() {
		return licenseeNumber;
	}

	private void setLicenseeNumber(String licenseeNumber) {
		this.licenseeNumber = licenseeNumber;
	}

	public ValidationParameters getValidationParams() {
		return validationParams;
	}

}
