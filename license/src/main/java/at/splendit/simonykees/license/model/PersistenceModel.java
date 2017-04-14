package at.splendit.simonykees.license.model;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Optional;

import at.splendit.simonykees.license.LicenseType;

/**
 * A representation of the license data to be persisted. Responsible 
 * for serializing the data to and from string. 
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public class PersistenceModel implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static final String SEPARATOR_REGEX = ","; //$NON-NLS-1$
	private static final String KEY_VALUE_SEPARATOR_REGEX = "\\|"; //$NON-NLS-1$
	private static final String SEPARATOR = ","; //$NON-NLS-1$
	private static final String KEY_VALUE_SEPARATOR = "|"; //$NON-NLS-1$
	private static final String EMPTY_STRING = "";  //$NON-NLS-1$
	
	private static final String LICENSEE_NUMBER_KEY = "licensee-number"; //$NON-NLS-1$
	private static final String LICENSEE_NAME_KEY = "licensee-name"; //$NON-NLS-1$
	private static final String LAST_VALIDATION_STATUS_KEY = "last-validation-status"; //$NON-NLS-1$
	private static final String LICENSE_TYPE_KEY = "license-type"; //$NON-NLS-1$
	private static final String LAST_VALIDATION_TIMESTAMP_KEY = "last-validation-timestamp"; //$NON-NLS-1$
	private static final String DEMO_EXPIRATION_KEY = "demo-expiration"; //$NON-NLS-1$
	private static final String EXPIRATION_TIMESTAMP_KEY = "expiration-timestamp"; //$NON-NLS-1$
	private static final String SUBSCRIPTION_EXPIRES_KEY = "subscription-expires"; //$NON-NLS-1$
	private static final String SUBSCRIPTION_STATUS_KEY = "subscription-status"; //$NON-NLS-1$
	private static final String LAST_SUCCESS_TIMESTAMP = "last-successful-timestamp"; //$NON-NLS-1$
	private static final String LAST_SUCCESS_LICENSE_TYPE = "last-successful-model"; //$NON-NLS-1$

	private String licenseeNumber;
	private String licenseeName;
	private boolean lastValidationStatus;
	private LicenseType licenseType;
	private Instant lastValidationTimestamp;
	private ZonedDateTime demoExpirationDate;
	private ZonedDateTime expirationTimeStamp;
	private ZonedDateTime subscriptionExpirationDate;
	private boolean subscriptionStatus;
	private Instant lastSuccessTimestamp;
	private LicenseType lastSuccessLicenseType;

	public PersistenceModel(String licenseeNumber, String licenseeName, boolean lastValidationStatus,
			LicenseType licenseType, Instant lastValidationTimestamp, ZonedDateTime demoExpirationDate,
			ZonedDateTime expirationTimeStamp, ZonedDateTime subscriptionExpirationDate, 
			boolean subscriptionStatus, Instant lastSuccessTimestamp, LicenseType lastSuccessType) {

		setLicenseeName(licenseeName);
		setLicenseeNumber(licenseeNumber);
		setLastValidationStatus(lastValidationStatus);
		setLicenseType(licenseType);
		setLastValidationTimestamp(lastValidationTimestamp);
		setDemoExpirationDate(demoExpirationDate);
		setSubscriptionExpirationDate(subscriptionExpirationDate);
		setSubscriptionStatus(subscriptionStatus);
		setExpirationTimeStamp(expirationTimeStamp);
		setLastSuccessTimestamp(lastSuccessTimestamp);
		setLastSuccessLicenseType(lastSuccessType);
	}

	public Optional<LicenseType> getLastSuccessLicenseType() {
		return Optional.ofNullable(lastSuccessLicenseType);
	}
	
	private void setLastSuccessLicenseType(LicenseType lastSuccessType) {
		this.lastSuccessLicenseType = lastSuccessType;
	}
	
	public Optional<Instant> getLastSuccessTimestamp() {
		return Optional.ofNullable(lastSuccessTimestamp);
	}

	private void setLastSuccessTimestamp(Instant lastSuccessTimestamp) {
		this.lastSuccessTimestamp = lastSuccessTimestamp;
	}

	private void setExpirationTimeStamp(ZonedDateTime expirationTimeStamp) {
		this.expirationTimeStamp = expirationTimeStamp;	
	}
	
	public Optional<ZonedDateTime> getExpirationTimeStamp() {
		return Optional.ofNullable(expirationTimeStamp);
	}

	public Optional<String> getLicenseeNumber() {
		return Optional.ofNullable(licenseeNumber).filter(s -> !s.isEmpty());
	}

	private void setLicenseeNumber(String licenseeNumber) {
		this.licenseeNumber = licenseeNumber;
	}

	public Optional<String> getLicenseeName() {
		return Optional.ofNullable(licenseeName).filter(s -> !s.isEmpty());
	}

	private void setLicenseeName(String licenseeName) {
		this.licenseeName = licenseeName;
	}

	public Optional<Boolean> getLastValidationStatus() {
		return Optional.ofNullable(lastValidationStatus);
	}

	private void setLastValidationStatus(boolean lastValidationStatus) {
		this.lastValidationStatus = lastValidationStatus;
	}

	public Optional<LicenseType> getLicenseType() {
		return Optional.ofNullable(licenseType);
	}

	private void setLicenseType(LicenseType licenseType) {
		this.licenseType = licenseType;
	}

	public Optional<Instant> getLastValidationTimestamp() {
		return Optional.ofNullable(lastValidationTimestamp);
	}

	private void setLastValidationTimestamp(Instant lastValidationTimestamp) {
		this.lastValidationTimestamp = lastValidationTimestamp;
	}

	public Optional<ZonedDateTime> getDemoExpirationDate() {
		return Optional.ofNullable(demoExpirationDate);
	}

	private void setDemoExpirationDate(ZonedDateTime demoExpirationDate) {
		this.demoExpirationDate = demoExpirationDate;
	}

	public Optional<ZonedDateTime> getSubscriptionExpirationDate() {
		return Optional.ofNullable(subscriptionExpirationDate);
	}

	private void setSubscriptionExpirationDate(ZonedDateTime subscriptionExpirationDate) {
		this.subscriptionExpirationDate = subscriptionExpirationDate;
	}

	public Optional<Boolean> getSubscriptionStatus() {
		return Optional.ofNullable(subscriptionStatus);
	}

	private void setSubscriptionStatus(boolean subscriptionStatus) {
		this.subscriptionStatus = subscriptionStatus;
	}
	
	public static PersistenceModel fromString(String strPersistenceModel) {
		HashMap<String, String> data = new HashMap<>();
		String[] splitedByComma = strPersistenceModel.split(SEPARATOR_REGEX);
		for(String row : splitedByComma) {
			String [] splitedRow = row.split(KEY_VALUE_SEPARATOR_REGEX);
			String key = splitedRow[0];
			String val = EMPTY_STRING;
			if(splitedRow.length > 1) {
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
		String strLastSuccessTimestamp = data.get(LAST_SUCCESS_TIMESTAMP);
		String strLastSuccessLicenseType = data.get(LAST_SUCCESS_LICENSE_TYPE);
		
		boolean lastVal = false;
		if(lastValStr!=null && !lastValStr.isEmpty()) {
			lastVal = Boolean.valueOf(lastValStr);
		}
		
		LicenseType licenseType = null;
		if(strLicenseType!= null && !strLicenseType.isEmpty()) {
			licenseType = LicenseType.fromString(strLicenseType);
		}
		
		Instant lastValTimestamp = null;
		if(strLastValTimeStamp!= null && !strLastValTimeStamp.isEmpty()) {
			lastValTimestamp = Instant.parse(strLastValTimeStamp);
		}
		
		ZonedDateTime demoExpiration = null;
		if(strDemoExpiration!= null && !strDemoExpiration.isEmpty()) {
			demoExpiration = ZonedDateTime.parse(strDemoExpiration);
		}
		
		ZonedDateTime expirationTimestamp = null;
		if(strExpirationTimestamp!=null && !strExpirationTimestamp.isEmpty()) {
			expirationTimestamp = ZonedDateTime.parse(strExpirationTimestamp);
		}
		
		ZonedDateTime subscriptionExpires = null;
		if(strSubscriptionExpires!=null && !strSubscriptionExpires.isEmpty()) {
			subscriptionExpires = ZonedDateTime.parse(strSubscriptionExpires);
		}
		
		boolean subscriptionStatus = false;
		if(strSubscriptionStatus!= null && !strSubscriptionStatus.isEmpty()) {
			subscriptionStatus = Boolean.valueOf(strSubscriptionStatus);
		}
		
		Instant lastSuccessTimestamp = null;
		if(strLastSuccessTimestamp!=null && !strLastSuccessTimestamp.isEmpty()) {
			lastSuccessTimestamp = Instant.parse(strLastSuccessTimestamp);
		}
		
		LicenseType lastSuccessType = null;
		if(strLastSuccessLicenseType!=null && !strLastSuccessLicenseType.isEmpty()) {
			lastSuccessType = LicenseType.fromString(strLastSuccessLicenseType);
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
				subscriptionStatus, 
				lastSuccessTimestamp, 
				lastSuccessType);
		
		return persistenceModel;
	}
	
	@Override
	public String toString() {

		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(LICENSEE_NUMBER_KEY + KEY_VALUE_SEPARATOR + getLicenseeNumber().orElse(EMPTY_STRING) + SEPARATOR);
		stringBuffer.append(LICENSEE_NAME_KEY + KEY_VALUE_SEPARATOR + getLicenseeName().orElse(EMPTY_STRING) + SEPARATOR);
		
		stringBuffer.append(LAST_VALIDATION_STATUS_KEY + KEY_VALUE_SEPARATOR);
		getLastValidationStatus()
		.ifPresent(status -> stringBuffer.append(status.toString()));
		stringBuffer.append(SEPARATOR);
		
		stringBuffer.append(LICENSE_TYPE_KEY + KEY_VALUE_SEPARATOR);
		getLicenseType()
		.ifPresent(licenseType -> stringBuffer.append(licenseType.toString()));
		stringBuffer.append(SEPARATOR);
		
		stringBuffer.append(LAST_VALIDATION_TIMESTAMP_KEY + KEY_VALUE_SEPARATOR);
		getLastValidationTimestamp()
		.ifPresent(timeStamp -> stringBuffer.append(timeStamp.toString()));
		stringBuffer.append(SEPARATOR);
		
		stringBuffer.append(DEMO_EXPIRATION_KEY + KEY_VALUE_SEPARATOR);
		getDemoExpirationDate()
		.ifPresent(demoExpireDate -> stringBuffer.append(demoExpireDate));
		stringBuffer.append(SEPARATOR);
		
		stringBuffer.append(EXPIRATION_TIMESTAMP_KEY + KEY_VALUE_SEPARATOR);
		getExpirationTimeStamp()
		.ifPresent(expirationTimeStamp -> stringBuffer.append(expirationTimeStamp));
		stringBuffer.append(SEPARATOR);
		
		stringBuffer.append(SUBSCRIPTION_EXPIRES_KEY + KEY_VALUE_SEPARATOR);
		getSubscriptionExpirationDate()
		.ifPresent(subscriptionExpires -> stringBuffer.append(subscriptionExpires));
		stringBuffer.append(SEPARATOR);
		
		stringBuffer.append(SUBSCRIPTION_STATUS_KEY + KEY_VALUE_SEPARATOR);
		getSubscriptionStatus()
		.ifPresent(subscriptionStatus -> stringBuffer.append(subscriptionStatus.toString()));
		stringBuffer.append(SEPARATOR);
		
		stringBuffer.append(LAST_SUCCESS_TIMESTAMP + KEY_VALUE_SEPARATOR);
		getLastSuccessTimestamp()
		.ifPresent(lastSuccessfulTimestamp -> stringBuffer.append(lastSuccessfulTimestamp.toString()));
		stringBuffer.append(SEPARATOR);
		
		stringBuffer.append(LAST_SUCCESS_LICENSE_TYPE + KEY_VALUE_SEPARATOR);
		getLastSuccessLicenseType()
		.ifPresent(lastSuccessfulModel -> stringBuffer.append(lastSuccessfulModel.toString()));
		
		return stringBuffer.toString();
	}
	
	public void updateLicenseeCredential(String licenseeName, String licenseeNumber) {
		setLicenseeName(licenseeName);
		setLicenseeNumber(licenseeNumber);
	}

}
