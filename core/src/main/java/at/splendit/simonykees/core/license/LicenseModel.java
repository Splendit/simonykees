package at.splendit.simonykees.core.license;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

public abstract class LicenseModel {

	private LicenseType type;
	private Long expireDate;
	private String productName;
	private String productModuleNumber;

	public LicenseModel(String productName, String productModuleNumber, LicenseType type, Long expireDate) {
		setType(type);
		setExpireDate(expireDate);
		setProductName(productName);
		setProductModuleNumber(productModuleNumber);
	}

	protected abstract ValidationParameters getValidationParameters();

	public LicenseType getType() {
		return type;
	}

	private void setType(LicenseType type) {
		this.type = type;
	}

	public Long getExpireDate() {
		return expireDate;
	}

	private void setExpireDate(Long expireDate) {
		this.expireDate = expireDate;
	}

	protected String getProudctName() {
		return this.productName;
	}
	
	private void setProductName(String productName){
		this.productName = productName;
	}

	protected String getProductModuleNumber() {
		return this.productModuleNumber;
	}
	
	private void setProductModuleNumber(String productModuleNumber){
		this.productModuleNumber = productModuleNumber;
	}

}
