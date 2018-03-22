package eu.jsparrow.license.netlicensing;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;

import com.labs64.netlicensing.domain.vo.Composition;
import com.labs64.netlicensing.domain.vo.ValidationResult;

/**
 * Responsible for parsing the result of a validation call. Detects the proper
 * license type and status.
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public class ResponseParser implements LicenseChecker {

	private static final String PRODUCT_MODULE_NUMBER_KEY = "productModuleNumber"; //$NON-NLS-1$
	private static final String FEATURE_NAME_KEY = "featureName"; //$NON-NLS-1$
	private static final String PRODUCT_MODULE_NAME_KEY = "productModuleName";//$NON-NLS-1$
	private static final String LICENSING_MODEL_KEY = "licensingModel"; //$NON-NLS-1$
	private static final String EXPIRATION_TIME_STAMP_KEY = "expirationTimestamp"; //$NON-NLS-1$
	private static final String EVALUATION_EXPIRES_DATE_KEY = "evaluationExpires"; //$NON-NLS-1$
	private static final String VALID_KEY = "valid"; //$NON-NLS-1$
	private static final String SUBSCRIPTION_EXPIRES_KEY = "expires"; //$NON-NLS-1$
	private LicenseType licenseType;
	private boolean licenseModelStatus;
	private Instant timestamp;
	private String licenseeName;
	private String productModuleNumber;
	private String productModuleName;
	private ZonedDateTime evaluationExpiresDate;
	private ZonedDateTime expirationTimeStamp;
	private ZonedDateTime subscriptionExpiresDate;
	private boolean subscriptionStatus;
	private LicenseStatus licenseStatus;
	private ValidationAction validationAction;

	public ResponseParser(ValidationResult validationResult, Instant timestamp, String licenseeName,
			ValidationAction validationAction) {
		setTimestamp(timestamp);
		setValidationAction(validationAction);
		extractValidationData(validationResult);
		setLicenseeName(licenseeName);
		setLicenseStatus(calcLicenseStatus());
	}

	private void extractValidationData(ValidationResult validationResult) {
		// first extract the subscription license
		extractSubscription(validationResult);
		// then check for a valid floating license...
		extractValidationData(validationResult, LicenseType.FLOATING);
		if (!isValid()) {
			// if no floating license is found, check for a node locked
			// license...
			extractValidationData(validationResult, LicenseType.NODE_LOCKED);
			if (!isValid()) {
				// if no node locked license, check for try and buy license...
				extractValidationData(validationResult, LicenseType.TRY_AND_BUY);
				/*
				 * finally, if no valid license is found, but there is a valid
				 * subscription, it must be the case that there is a floating
				 * license which is running out of sessions
				 */
				if (!isValid() && getSubscriptionStatus()) {
					extractValidationData(validationResult, LicenseType.FLOATING);
				}
			}
		}
	}

	/**
	 * Checks if the given validation result contains information about the
	 * given license type. If it does, a state is created out of that
	 * information.
	 * 
	 * @param validationResult
	 *            received from NetLicensing
	 * @param licenseType
	 *            type of the license to check for
	 */
	private void extractValidationData(ValidationResult validationResult, LicenseType licenseType) {
		Map<String, Composition> validations = validationResult.getValidations();

		String nodeLockedFeatureKey = extractNodeLockedFeatureKey(validationResult);

		validations.forEach((compKey, composition) -> {

			Map<String, Composition> properties = composition.getProperties();
			String receivedTypeStr = properties.get(LICENSING_MODEL_KEY)
				.getValue();
			LicenseType receivedType = LicenseType.fromString(receivedTypeStr);

			if (licenseType.equals(receivedType)) {
				String pmNumber = compKey;
				setLicenseType(licenseType);
				setProductModuleNumber(pmNumber);
				properties.forEach((key, value) -> {

					boolean valid;
					if (key.equals(VALID_KEY)) {
						valid = Boolean.valueOf(value.getValue());
						seLicenseModelStatus(valid);
					} else if (key.equals(nodeLockedFeatureKey)) {
						Map<String, Composition> featureKeyValues = value.getProperties();
						Composition featureStatus = featureKeyValues.get(VALID_KEY);
						valid = Boolean.valueOf(featureStatus.getValue());
						seLicenseModelStatus(valid);
					} else if (key.equals(PRODUCT_MODULE_NAME_KEY)) {
						setProductModuleName(value.getValue());
					} else if (key.equals(PRODUCT_MODULE_NUMBER_KEY)) {
						setProductModuleNumber(value.getValue());
					} else if (key.equals(EXPIRATION_TIME_STAMP_KEY)) {
						ZonedDateTime date = ZonedDateTime.parse(value.getValue());
						setExpirationTimeStamp(date);
					} else if (key.equals(EVALUATION_EXPIRES_DATE_KEY)) {
						ZonedDateTime date = ZonedDateTime.parse(value.getValue());
						setEvaluationExpiresDate(date);
					}

				});
			}
		});
	}

	private String extractNodeLockedFeatureKey(ValidationResult validationResult) {
		String nodeLockedFeatureKey = ""; //$NON-NLS-1$
		Map<String, Composition> validations = validationResult.getValidations();

		for (Map.Entry<String, Composition> compEntry : validations.entrySet()) {
			Composition composition = compEntry.getValue();
			Map<String, Composition> properties = composition.getProperties();

			String receivedTypeStr = properties.get(LICENSING_MODEL_KEY)
				.getValue();
			LicenseType receivedType = LicenseType.fromString(receivedTypeStr);

			if (LicenseType.NODE_LOCKED == receivedType) {

				for (Map.Entry<String, Composition> entry : properties.entrySet()) {
					String key = entry.getKey();
					Composition value = entry.getValue();
					if (!value.getProperties()
						.isEmpty()) {
						Map<String, Composition> featureProperties = value.getProperties();
						if (featureProperties.containsKey(VALID_KEY)
								&& featureProperties.containsKey(FEATURE_NAME_KEY)) {
							nodeLockedFeatureKey = key;
						}
					}
				}
			}
		}

		return nodeLockedFeatureKey;
	}

	/**
	 * Similar to {@link extractValidationData(ValidationResult, LicenseType)}
	 * except that it checks only of the subscription license type.
	 * 
	 * @param validationResult
	 */
	private void extractSubscription(ValidationResult validationResult) {
		Map<String, Composition> validations = validationResult.getValidations();

		validations.forEach((compKey, composition) -> {

			Map<String, Composition> properties = composition.getProperties();
			String receivedTypeStr = properties.get(LICENSING_MODEL_KEY)
				.getValue();
			LicenseType receivedType = LicenseType.fromString(receivedTypeStr);

			if (receivedType.equals(LicenseType.SUBSCRIPTION)) {

				properties.forEach((key, value) -> {

					switch (key) {
					case VALID_KEY:
						boolean valid = Boolean.parseBoolean(value.getValue());
						setSubscriptionStatus(valid);
						break;
					case SUBSCRIPTION_EXPIRES_KEY:
						ZonedDateTime date = ZonedDateTime.parse(value.getValue());
						setSubscriptionExpiresDate(date);
						break;
					}

				});
			}
		});
	}

	public static boolean parseLicenseeValidation(ValidationResult validationResult) {
		boolean isValid = false;
		Map<String, Composition> validations = validationResult.getValidations();
		for (Map.Entry<String, Composition> entry : validations.entrySet()) {
			Composition composition = entry.getValue();
			Map<String, Composition> properties = composition.getProperties();
			if (properties.containsKey(VALID_KEY)) {
				Composition value = properties.get(VALID_KEY);
				isValid = Boolean.valueOf(value.getValue());
				if (isValid) {
					break;
				}
			}
		}

		return isValid;
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
	public boolean isValid() {
		boolean status = false;

		if (getType() != null) {
			if (getType() == LicenseType.TRY_AND_BUY) {
				/*
				 * In case of TRY_AND_BUY type, the license is valid if the
				 * corresponding valid field is true
				 */
				status = this.licenseModelStatus;
			} else {
				/*
				 * For other license models, the validity is a conjunction of
				 * the license model validity and the subscription validity.
				 */
				status = this.licenseModelStatus && this.subscriptionStatus;
			}
		}

		return status;
	}

	@Override
	public Instant getValidationTimeStamp() {
		return this.timestamp;
	}

	private void setLicenseType(LicenseType licenseType) {
		this.licenseType = licenseType;
	}

	private void seLicenseModelStatus(boolean status) {
		this.licenseModelStatus = status;
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

	@Override
	public ZonedDateTime getExpirationDate() {
		return subscriptionExpiresDate;
	}

	private void setValidationAction(ValidationAction validationAction) {
		this.validationAction = validationAction;
	}

	@Override
	public LicenseStatus getLicenseStatus() {
		return licenseStatus;
	}

	private void setLicenseStatus(LicenseStatus licenseStatus) {
		this.licenseStatus = licenseStatus;

	}

	/**
	 * Calculates the license status based on the extracted type and validity of
	 * license. Note that {@link LicenseStatus} is to be used only as a
	 * descriptor. The validity of the license is NOT calculated.
	 * 
	 * @return computed license status.
	 */
	private LicenseStatus calcLicenseStatus() {
		LicenseType type = getType();
		LicenseStatus status;
		
		switch (type) {
		case TRY_AND_BUY:
			if (isValid()) {
				status = LicenseStatus.FREE_REGISTERED;
			} else {
				status = LicenseStatus.FREE_EXPIRED;
			}
			break;

		case NODE_LOCKED:
			if (isValid()) {
				status = LicenseStatus.NODE_LOCKED_REGISTERED;
			} else {
				status = LicenseStatus.NODE_LOCKED_EXPIRED;
			}
			break;

		case FLOATING:
			ValidationAction action = getValidationAction();
			if (isValid()) {
				status = LicenseStatus.FLOATING_CHECKED_OUT;
			} else if (action == ValidationAction.CHECK_OUT && getSubscriptionStatus()) {
				status = LicenseStatus.FLOATING_OUT_OF_SESSION;
			} else if (action == ValidationAction.CHECK_IN && getSubscriptionStatus()) {
				status = LicenseStatus.FLOATING_CHECKED_IN;
			} else {
				status = LicenseStatus.FLOATING_EXPIRED;
			}
			break;

		default:
			status = LicenseStatus.NONE;
			break;
		}

		return status;
	}

	private ValidationAction getValidationAction() {
		return this.validationAction;
	}
}
