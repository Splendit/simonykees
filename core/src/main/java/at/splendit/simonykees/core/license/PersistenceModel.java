package at.splendit.simonykees.core.license;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Optional;

public class PersistenceModel implements Serializable {
	
	private static final String LICENSEE_NUMBER_KEY = "licensee-number"; //$NON-NLS-1$
	private static final String LICENSEE_NAME_KEY = "licensee-name"; //$NON-NLS-1$
	private static final String LAST_VALIDATION_STATUS_KEY = "last-validation-status"; //$NON-NLS-1$
	private static final String LICENSE_TYPE_KEY = "license-type"; //$NON-NLS-1$
	private static final String LAST_VALIDATION_TIMESTAMP_KEY = "last-validation-timestamp"; //$NON-NLS-1$
	private static final String DEMO_EXPIRATION_KEY = "demo-expiration"; //$NON-NLS-1$
	private static final String EXPIRATION_TIMESTAMP_KEY = "expiration-timestamp"; //$NON-NLS-1$
	private static final String SUBSCRIPTION_EXPIRES_KEY = "subscription-expires"; //$NON-NLS-1$
	private static final String SUBSCRIPTION_STATUS_KEY = "subscription-status"; //$NON-NLS-1$

	private String licenseeNumber;
	private String licenseeName;
	private boolean lastValidationStatus;
	private LicenseType licenseType;
	private Instant lastValidationTimestamp;
	private ZonedDateTime demoExpirationDate;
	private ZonedDateTime expirationTimeStamp;
	private ZonedDateTime subscriptionExpirationDate;
	private boolean subscriptionStatus;

	public PersistenceModel(String licenseeNumber, String licenseeName, boolean lastValidationStatus,
			LicenseType licenseType, Instant lastValidationTimestamp, ZonedDateTime demoExpirationDate,
			ZonedDateTime expirationTimeStamp, ZonedDateTime subscriptionExpirationDate, boolean subscriptionStatus) {

		setLicenseeName(licenseeName);
		setLicenseeNumber(licenseeNumber);
		setLastValidationStatus(lastValidationStatus);
		setLicenseType(licenseType);
		setLastValidationTimestamp(lastValidationTimestamp);
		setDemoExpirationDate(demoExpirationDate);
		setSubscriptionExpirationDate(subscriptionExpirationDate);
		setSubscriptionStatus(subscriptionStatus);
		setExpirationTimeStamp(expirationTimeStamp);
	}

	private void setExpirationTimeStamp(ZonedDateTime expirationTimeStamp) {
		this.expirationTimeStamp = expirationTimeStamp;	
	}
	
	public Optional<ZonedDateTime> getExpirationTimeStamp() {
		return Optional.of(expirationTimeStamp);
	}

	public Optional<String> getLicenseeNumber() {
		return Optional.of(licenseeNumber);
	}

	private void setLicenseeNumber(String licenseeNumber) {
		this.licenseeNumber = licenseeNumber;
	}

	public Optional<String> getLicenseeName() {
		return Optional.of(licenseeName);
	}

	private void setLicenseeName(String licenseeName) {
		this.licenseeName = licenseeName;
	}

	public Optional<Boolean> getLastValidationStatus() {
		return Optional.of(lastValidationStatus);
	}

	private void setLastValidationStatus(boolean lastValidationStatus) {
		this.lastValidationStatus = lastValidationStatus;
	}

	public Optional<LicenseType> getLicenseType() {
		return Optional.of(licenseType);
	}

	private void setLicenseType(LicenseType licenseType) {
		this.licenseType = licenseType;
	}

	public Optional<Instant> getLastValidationTimestamp() {
		return Optional.of(lastValidationTimestamp);
	}

	private void setLastValidationTimestamp(Instant lastValidationTimestamp) {
		this.lastValidationTimestamp = lastValidationTimestamp;
	}

	public Optional<ZonedDateTime> getDemoExpirationDate() {
		return Optional.of(demoExpirationDate);
	}

	private void setDemoExpirationDate(ZonedDateTime demoExpirationDate) {
		this.demoExpirationDate = demoExpirationDate;
	}

	public Optional<ZonedDateTime> getSubscriptionExpirationDate() {
		return Optional.of(subscriptionExpirationDate);
	}

	private void setSubscriptionExpirationDate(ZonedDateTime subscriptionExpirationDate) {
		this.subscriptionExpirationDate = subscriptionExpirationDate;
	}

	public Optional<Boolean> getSubscriptionStatus() {
		return Optional.of(subscriptionStatus);
	}

	private void setSubscriptionStatus(boolean subscriptionStatus) {
		this.subscriptionStatus = subscriptionStatus;
	}
	
	public static PersistenceModel fromString(String strPersistenceModel) {
		HashMap<String, String> data = new HashMap<>();
		String[] splitedByComma = strPersistenceModel.split(",");
		for(String row : splitedByComma) {
			String [] splitedRow = row.split(":");
			String key = splitedRow[0];
			String val = "";
			if(splitedRow.length > 0) {
				val = splitedRow[1];
			}
			data.put(key, val);
		}
		
		String licenseeNumber = data.get(LICENSEE_NUMBER_KEY);
		String licenseeName = data.get(LICENSEE_NAME_KEY);
		String lastValStr = data.get(LAST_VALIDATION_STATUS_KEY);
		String strLicenseType = data.get(LICENSE_TYPE_KEY);
		String strLastValTimeStamp = data.get(LAST_VALIDATION_TIMESTAMP_KEY);
		String strDemoExpiration = data.get(DEMO_EXPIRATION_KEY);
		String strExpirationTimestamp = data.get(EXPIRATION_TIMESTAMP_KEY);
		String strSubscriptionExpires = data.get(SUBSCRIPTION_EXPIRES_KEY);
		String strSubscriptionStatus = data.get(SUBSCRIPTION_STATUS_KEY);
		
		boolean lastVal = false;
		if(!lastValStr.isEmpty()) {
			lastVal = Boolean.valueOf(lastValStr);
		}
		
		LicenseType licenseType = null;
		if(!strLicenseType.isEmpty()) {
			licenseType = LicenseType.fromString(strLicenseType);
		}
		
		Instant lastValTimestamp = null;
		if(!strLastValTimeStamp.isEmpty()) {
			lastValTimestamp = Instant.parse(strLastValTimeStamp);
		}
		
		ZonedDateTime demoExpiration = null;
		if(!strDemoExpiration.isEmpty()) {
			demoExpiration = ZonedDateTime.parse(strDemoExpiration);
		}
		
		ZonedDateTime expirationTimestamp = null;
		if(!strExpirationTimestamp.isEmpty()) {
			expirationTimestamp = ZonedDateTime.parse(strExpirationTimestamp);
		}
		
		ZonedDateTime subscriptionExpires = null;
		if(!strSubscriptionExpires.isEmpty()) {
			subscriptionExpires = ZonedDateTime.parse(strSubscriptionExpires);
		}
		
		boolean subscriptionStatus = false;
		if(!strSubscriptionStatus.isEmpty()) {
			subscriptionStatus = Boolean.valueOf(strSubscriptionStatus);
		}
		
		PersistenceModel persistenceModel = new PersistenceModel(
				licenseeNumber,
				licenseeName, 
				lastVal, 
				licenseType, 
				lastValTimestamp, 
				demoExpiration, 
				expirationTimestamp,
				subscriptionExpires, 
				subscriptionStatus);
		
		return persistenceModel;
	}
	
	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(LICENSEE_NUMBER_KEY + ":" + getLicenseeNumber().orElse("") + ",");
		stringBuffer.append(LICENSEE_NAME_KEY + ":" + getLicenseeName().orElse("") + ",");
		
		stringBuffer.append(LAST_VALIDATION_STATUS_KEY + ":");
		getLastValidationStatus()
		.ifPresent(status -> stringBuffer.append(status.toString()));
		stringBuffer.append(",");
		
		stringBuffer.append(LICENSE_TYPE_KEY + ":");
		getLicenseType()
		.ifPresent(licenseType -> stringBuffer.append(licenseType.toString()));
		stringBuffer.append(",");
		
		stringBuffer.append(LAST_VALIDATION_TIMESTAMP_KEY + ":");
		getLastValidationStatus()
		.ifPresent(lastValStatus -> stringBuffer.append(lastValStatus.toString()));
		stringBuffer.append(",");
		
		stringBuffer.append(DEMO_EXPIRATION_KEY + ":");
		getDemoExpirationDate()
		.ifPresent(demoExpireDate -> stringBuffer.append(demoExpireDate.toString()));
		stringBuffer.append(",");
		
		stringBuffer.append(EXPIRATION_TIMESTAMP_KEY + ":");
		getExpirationTimeStamp()
		.ifPresent(expirationTimeStamp -> stringBuffer.append(expirationTimeStamp.toString()));
		stringBuffer.append(",");
		
		stringBuffer.append(SUBSCRIPTION_EXPIRES_KEY + ":");
		getSubscriptionExpirationDate()
		.ifPresent(subscriptionExpires -> stringBuffer.append(subscriptionExpires.toString()));
		stringBuffer.append(",");
		
		stringBuffer.append(SUBSCRIPTION_STATUS_KEY + ":");
		getSubscriptionStatus()
		.ifPresent(subscriptionStatus -> stringBuffer.append(subscriptionStatus.toString()));
		stringBuffer.append(",");
		
		return stringBuffer.toString();
	}

}
