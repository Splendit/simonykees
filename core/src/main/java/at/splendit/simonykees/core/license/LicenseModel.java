package at.splendit.simonykees.core.license;

import java.time.Instant;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

public abstract class LicenseModel {

	private LicenseType type;
	private Instant expireDate;
	private String productName;
	private String productModuleNumber;

	public LicenseModel(String productName, String productModuleNumber, LicenseType type, Instant expireDate) {
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

	public Instant getExpireDate() {
		return expireDate;
	}

	private void setExpireDate(Instant expireDate) {
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
