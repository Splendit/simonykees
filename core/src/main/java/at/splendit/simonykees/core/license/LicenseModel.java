package at.splendit.simonykees.core.license;

import java.time.ZonedDateTime;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

public abstract class LicenseModel {

	private LicenseType type;
	private ZonedDateTime expireDate;

	protected LicenseModel(LicenseType type, ZonedDateTime expireDate) {
		setType(type);
		setExpireDate(expireDate);
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

}
