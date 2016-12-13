package at.splendit.simonykees.core.license;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;

import com.labs64.netlicensing.domain.vo.Composition;
import com.labs64.netlicensing.domain.vo.ValidationResult;

public class LicenseCheckerImpl implements LicenseChecker {
	//TODO: rename this class. find a proper name for it.

	private LicenseType licenseType;
	private boolean status;
	private Instant timestamp;
	private String licenseeName;
	private String productModuleNumber;
	private String productModuleName;
	private ZonedDateTime evaluationExpiresDate;
	private ZonedDateTime expirationTimeStamp;
	private ZonedDateTime subscriptionExpiresDate;
	private boolean subscriptionStatus;

	private final String PRODUCT_MODULE_NUMBER_KEY = "productModuleNumber"; //$NON-NLS-1$
	private final String PRODUCT_MODULE_NAME_KEY = "productModuleName";//$NON-NLS-1$
	private final String LICENSING_MODEL_KEY = "licensingModel"; //$NON-NLS-1$
	private final String EXPIRATION_TIME_STAMP_KEY = "expirationTimestamp"; //$NON-NLS-1$
	private final String EVALUATION_EXPIRES_DATE_KEY = "evaluationExpires"; //$NON-NLS-1$
	private final String VALID_KEY = "valid"; //$NON-NLS-1$
	private final String SUBSCRIPTION_EXPIRES_KEY = "expires"; //$NON-NLS-1$


	public LicenseCheckerImpl(ValidationResult validationResult, Instant timestamp, String licenseeName) {
		setTimestamp(timestamp);
		extractValidationData(validationResult);
		setLicenseeName(licenseeName);
	}
	
	private void extractValidationData(ValidationResult validationResult) {
		extractSubscription(validationResult);
		extractValidationData(validationResult, LicenseType.FLOATING);
		if(!getStatus()){
			extractValidationData(validationResult, LicenseType.NODE_LOCKED);
			if(!getStatus()){
				extractValidationData(validationResult, LicenseType.TRY_AND_BUY);
			}
		}	
	}

	private void extractValidationData(ValidationResult validationResult, LicenseType licenseType) {
		Map<String, Composition> validations = validationResult.getValidations();
		
		validations.forEach((compKey, composition)-> {
			
			Map<String, Composition> properties = composition.getProperties();
			String receivedTypeStr = properties.get(LICENSING_MODEL_KEY).getValue();
			LicenseType receivedType = LicenseType.fromString(receivedTypeStr);

			if(licenseType.equals(receivedType)) {
				String productModuleNumber = compKey;
				setLicenseType(licenseType);
				setProductModuleNumber(productModuleNumber);
				properties.forEach((key, value) -> {

					switch (key) {
					case VALID_KEY:
						boolean valid = Boolean.valueOf(value.getValue());
						setStatus(valid);
						break;
					case PRODUCT_MODULE_NAME_KEY:
						setProductModuleName(value.getValue());
						break;
					case PRODUCT_MODULE_NUMBER_KEY:
						setProductModuleNumber(value.getValue());
						break;
					case EXPIRATION_TIME_STAMP_KEY:
						ZonedDateTime expirationTimeStamp = ZonedDateTime.parse(value.getValue());
						setExpirationTimeStamp(expirationTimeStamp);
						break;
					case EVALUATION_EXPIRES_DATE_KEY:
						ZonedDateTime evaluationExpiresDate = ZonedDateTime.parse(value.getValue());
						setEvaluationExpiresDate(evaluationExpiresDate);
						break;
					}
					
				});
			}
		});
	}
	
	private void extractSubscription(ValidationResult validationResult){
		Map<String, Composition> validations = validationResult.getValidations();
		
		validations.forEach((compKey, composition)-> {
			
			Map<String, Composition> properties = composition.getProperties();
			String receivedTypeStr = properties.get(LICENSING_MODEL_KEY).getValue();
			LicenseType receivedType = LicenseType.fromString(receivedTypeStr);

			if(receivedType.equals(LicenseType.SUBSCRIPTION)) {

				properties.forEach((key, value) -> {

					switch (key) {
					case VALID_KEY:
						boolean valid = Boolean.valueOf(value.getValue());
						setSubscriptionStatus(valid);
						break;
					case SUBSCRIPTION_EXPIRES_KEY:
						ZonedDateTime subscriptionExpiresDate = ZonedDateTime.parse(value.getValue());
						setSubscriptionExpiresDate(subscriptionExpiresDate);
						break;
					}
					
				});
			}
		});
	}
	
	private void setSubscriptionExpiresDate(ZonedDateTime subscriptionExpiresDate) {
		this.subscriptionExpiresDate = subscriptionExpiresDate;
	}

	private void setSubscriptionStatus(boolean status) {
		this.subscriptionStatus = status; 
		
	}

	private void setProductModuleNumber(String value) {
		this.productModuleNumber = value;
	}

	private void setProductModuleName(String value) {
		this.productModuleName = value;		
	}

	private void setEvaluationExpiresDate(ZonedDateTime evaluationExpiresDate) {
		this.evaluationExpiresDate = evaluationExpiresDate;
	}

	private void setExpirationTimeStamp(ZonedDateTime expirationTimeStamp) {
		this.expirationTimeStamp = expirationTimeStamp;
	}

	@Override
	public LicenseType getType() {
		return this.licenseType;
	}

	@Override
	public boolean getStatus() {
		return this.status;
	}

	@Override
	public Instant getValidationTimeStamp() {
		return this.timestamp;
	}

	private void setLicenseType(LicenseType licenseType) {
		this.licenseType = licenseType;
	}

	private void setStatus(boolean status) {
		this.status = status;
	}

	private void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}
	
	private void setLicenseeName(String licenseeName) {
		this.licenseeName = licenseeName;
	}

	@Override
	public String getLicenseeName() {
		return this.licenseeName;
	}

	public String getProductModulNumber() {
		return productModuleNumber;
	}

	public String getProductModulName() {
		return productModuleName;
	}
	
	public ZonedDateTime getEvaluationExpiresDate() {
		return evaluationExpiresDate;
	}
	
	public ZonedDateTime getExpirationTimeStamp() {
		return this.expirationTimeStamp;
	}
	
	public boolean getSubscriptionStatus() {
		return subscriptionStatus;
	}
	
	public ZonedDateTime getSubscriptionExpiresDate() {
		return subscriptionExpiresDate;
	}
}
