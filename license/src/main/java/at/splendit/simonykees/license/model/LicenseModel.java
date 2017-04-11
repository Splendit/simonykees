package at.splendit.simonykees.license.model;

import java.time.ZonedDateTime;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import at.splendit.simonykees.license.LicenseType;

/**
 * A super class for all license models. Enforces construction of 
 * validation parameters. 
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public abstract class LicenseModel {

	private LicenseType type;
	private ZonedDateTime expireDate;

	protected LicenseModel(LicenseType type, ZonedDateTime expireDate) {
		setType(type);
		setExpireDate(expireDate);
	}

	public abstract ValidationParameters getValidationParameters();

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
