package eu.jsparrow.license.netlicensing.validation.impl;

import java.time.ZonedDateTime;

import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.api.LicenseValidationResult;

public class NetlicensingValidationResult extends LicenseValidationResult {

	private ZonedDateTime offlineExpirationTime;
	private Integer credit = 0;

	public NetlicensingValidationResult(LicenseType licenseType, String key, boolean valid, String detail,
			ZonedDateTime expirationDate) {
		super(licenseType, key, valid, detail, expirationDate);
	}

	public boolean isExpired() {
		return ZonedDateTime.now()
			.isAfter(offlineExpirationTime);
	}

	public ZonedDateTime getExpirationTime() {
		return offlineExpirationTime;
	}

	public Integer getCredit() {
		return this.credit;
	}

	public static class Builder {
		private LicenseType licenseType;
		private String key;
		private boolean valid;
		private String detail;
		private ZonedDateTime expirationDate;
		private ZonedDateTime offlineExpirationTime;
		private int credit;

		public Builder withLicenseType(LicenseType licenseType) {
			this.licenseType = licenseType;
			return this;
		}

		public Builder withKey(String key) {
			this.key = key;
			return this;
		}

		public Builder withValid(boolean valid) {
			this.valid = valid;
			return this;
		}

		public Builder withDetail(String detail) {
			this.detail = detail;
			return this;
		}

		public Builder withExpirationDate(ZonedDateTime expirationDate) {
			this.expirationDate = expirationDate;
			return this;
		}

		public Builder withOfflineExpirationTime(ZonedDateTime offlineExpirationTime) {
			this.offlineExpirationTime = offlineExpirationTime;
			return this;
		}
		
		public Builder withCredit(Integer credit) {
			this.credit = credit;
			return this;
		}

		public NetlicensingValidationResult build() {
			NetlicensingValidationResult result = new NetlicensingValidationResult(licenseType, key, valid, detail,
					expirationDate);
			result.offlineExpirationTime = this.offlineExpirationTime;
			result.credit = this.credit;
			return result;
		}
	}
}
