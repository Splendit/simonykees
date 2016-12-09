package at.splendit.simonykees.core.license;

import java.time.ZonedDateTime;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

public abstract class LicenseModel {

	private LicenseType type;
	private ZonedDateTime expireDate;
	private String productModuleNumber;

	public LicenseModel(String productModuleNumber, LicenseType type, ZonedDateTime expireDate) {
		setType(type);
		setExpireDate(expireDate);
		setProductModuleNumber(productModuleNumber);
	}

	protected abstract ValidationParameters getValidationParameters();

	public LicenseType getType() {
		return type;
	}

	private void setType(LicenseType type) {
		this.type = type;
	}

	public ZonedDateTime getExpireDate() {
		return expireDate;
	}

	private void setExpireDate(ZonedDateTime expireDate) {
		this.expireDate = expireDate;
	}

	protected String getProductModuleNumber() {
		return this.productModuleNumber;
	}
	
	private void setProductModuleNumber(String productModuleNumber){
		this.productModuleNumber = productModuleNumber;
	}

}
