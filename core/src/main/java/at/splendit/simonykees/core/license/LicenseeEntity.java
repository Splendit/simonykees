package at.splendit.simonykees.core.license;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

public class LicenseeEntity {

	private String licenseeName;
	private String licenseeNumber;
	private ValidationParameters validationParams;
	private LicenseModel licenseModel;
	private String productNumber;

	public LicenseeEntity(String licenseeName, String licenseeNumber, LicenseModel licenseModel, String productNumber) {
		setLicenseeName(licenseeName);
		setLicenseeNumber(licenseeNumber);
		setLicenseModel(licenseModel);
		setProductNumber(productNumber);
		
		initValidationParameters(licenseeName);
	}
	
	private void initValidationParameters(String licenseeName) {
		LicenseModel licenseModel = getLicenseModel();
		String productNumber = getProductNumber();
		
		ValidationParameters validationParams = licenseModel.getValidationParameters();
		validationParams.setLicenseeName(licenseeName);
		validationParams.setProductNumber(productNumber);
		
		setValidationParameters(validationParams);
	}

	private void setValidationParameters(ValidationParameters validationParams) {
		this.validationParams = validationParams;
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

	public LicenseModel getLicenseModel() {
		return licenseModel;
	}

	private void setLicenseModel(LicenseModel licenseModel) {
		this.licenseModel = licenseModel;
	}

	private String getProductNumber() {
		return productNumber;
	}

	private void setProductNumber(String productNumber) {
		this.productNumber = productNumber;
	}

}
