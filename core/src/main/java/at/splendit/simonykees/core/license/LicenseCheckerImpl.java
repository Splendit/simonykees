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
	private ZonedDateTime expires;

	// TODO: check if the following keys match with the keys of the validation properties
	private final String PRODUCT_MODULE_NUMBER_KEY = "productModuleNumber"; //$NON-NLS-1$
	private final String PRODUCT_MODULE_NAME_KEY = "productModuleName";//$NON-NLS-1$
	private final String LICENSING_MODEL_KEY = "licensingModel"; //$NON-NLS-1$
	private final String EXPIRES_KEY = "expires"; //$NON-NLS-1$
	private final String VALID_KEY = "valid"; //$NON-NLS-1$

	public LicenseCheckerImpl(ValidationResult validationResult, Instant timestamp, String licenseeName) {
		setTimestamp(timestamp);
		extractValidationData(validationResult);
		setLicenseeName(licenseeName);
	}

	private void extractValidationData(ValidationResult validationResult) {
		Map<String, Composition> validations = validationResult.getValidations();
		validations.values().forEach(composition -> {
			composition.getProperties().forEach((key, value) -> {

				switch (key) {
				case LICENSING_MODEL_KEY:
					LicenseType type = LicenseType.fromString(value.getValue());
					setLicenseType(type);
					break;
				case VALID_KEY:
					boolean valid = Boolean.valueOf(value.getValue());
					setStatus(valid);
					break;
				case EXPIRES_KEY:
					ZonedDateTime expireDate = ZonedDateTime.parse(value.getValue());
					setExpireDate(expireDate);
					break;
				case PRODUCT_MODULE_NUMBER_KEY:
					setProductModuleNumber(value.getValue());
					break;
				case PRODUCT_MODULE_NAME_KEY:
					setProductModuleName(value.getValue());
					break;
				}
				
			});
		});
	}

	private void setProductModuleName(String value) {
		this.productModuleName = value;
		
	}

	private void setProductModuleNumber(String value) {
		this.productModuleNumber = value;
		
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
	
	private void setExpireDate(ZonedDateTime expireDate) {
		this.expires = expireDate;
	}

	@Override
	public String getLicenseeName() {
		return this.licenseeName;
	}

	public ZonedDateTime getExprieDate() {
		return expires;
	}

	public String getProductModulNumber() {
		return productModuleNumber;
	}

	public String getProductModulName() {
		return productModuleName;
	}
}
