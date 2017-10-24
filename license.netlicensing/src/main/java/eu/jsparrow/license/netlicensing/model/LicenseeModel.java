package eu.jsparrow.license.netlicensing.model;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

/**
 * Representation of the licensee entity. Responsible for constructing the
 * validation parameters related to the licensee. It makes use of the license
 * model for getting parameters related to the license.
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public class LicenseeModel {

	private String licenseeName;
	private String licenseeNumber;
	private ValidationParameters validationParams;
	private LicenseModel licenseModel;
	private String productNumber;

	public LicenseeModel(String licenseeName, String licenseeNumber, LicenseModel licenseModel, String productNumber) {
		setLicenseeName(licenseeName);
		setLicenseeNumber(licenseeNumber);
		setLicenseModel(licenseModel);
		setProductNumber(productNumber);

		initValidationParameters(licenseeName);
	}

	private void initValidationParameters(String licenseeName) {
		LicenseModel model = getLicenseModel();
		String pNumber = getProductNumber();

		ValidationParameters params = new ValidationParameters();
		if (model != null) {
			params = model.getValidationParameters();
		}

		params.setLicenseeName(licenseeName);
		params.setProductNumber(pNumber);

		setValidationParameters(params);
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
