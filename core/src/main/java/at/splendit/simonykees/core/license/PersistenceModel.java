package at.splendit.simonykees.core.license;

import java.time.Instant;
import java.time.ZonedDateTime;

public class PersistenceModel {

	private String licenseeNumber;
	private String licenseeName;
	private boolean lastValidationStatus;
	private LicenseType licenseType;
	private Instant lastValidationTimestamp;
	private ZonedDateTime demoExpirationDate;
	private ZonedDateTime subscriptionExpirationDate;
	private boolean subscriptionStatus;
	
	

	public PersistenceModel(String licenseeNumber, String licenseeName, boolean lastValidationStatus,
			LicenseType licenseType, Instant lastValidationTimestamp, ZonedDateTime demoExpirationDate,
			ZonedDateTime subscriptionExpirationDate, boolean subscriptionStatus) {

		setLicenseeName(licenseeName);
		setLicenseeNumber(licenseeNumber);
		setLastValidationStatus(lastValidationStatus);
		setLicenseType(licenseType);
		setLastValidationTimestamp(lastValidationTimestamp);
		setDemoExpirationDate(demoExpirationDate);
		setSubscriptionExpirationDate(subscriptionExpirationDate);
		setSubscriptionStatus(subscriptionStatus);
	}

	public String getLicenseeNumber() {
		return licenseeNumber;
	}

	private void setLicenseeNumber(String licenseeNumber) {
		this.licenseeNumber = licenseeNumber;
	}

	public String getLicenseeName() {
		return licenseeName;
	}

	private void setLicenseeName(String licenseeName) {
		this.licenseeName = licenseeName;
	}

	public boolean getLastValidationStatus() {
		return lastValidationStatus;
	}

	private void setLastValidationStatus(boolean lastValidationStatus) {
		this.lastValidationStatus = lastValidationStatus;
	}

	public LicenseType getLicenseType() {
		return licenseType;
	}

	private void setLicenseType(LicenseType licenseType) {
		this.licenseType = licenseType;
	}

	public Instant getLastValidationTimestamp() {
		return lastValidationTimestamp;
	}

	private void setLastValidationTimestamp(Instant lastValidationTimestamp) {
		this.lastValidationTimestamp = lastValidationTimestamp;
	}

	public ZonedDateTime getDemoExpirationDate() {
		return demoExpirationDate;
	}

	private void setDemoExpirationDate(ZonedDateTime demoExpirationDate) {
		this.demoExpirationDate = demoExpirationDate;
	}

	public ZonedDateTime getSubscriptionExpirationDate() {
		return subscriptionExpirationDate;
	}

	private void setSubscriptionExpirationDate(ZonedDateTime subscriptionExpirationDate) {
		this.subscriptionExpirationDate = subscriptionExpirationDate;
	}

	public boolean isSubscriptionStatus() {
		return subscriptionStatus;
	}

	private void setSubscriptionStatus(boolean subscriptionStatus) {
		this.subscriptionStatus = subscriptionStatus;
	}

}
