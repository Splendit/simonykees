package at.splendit.simonykees.core.license;

import java.time.Instant;
import java.util.Map;

import com.labs64.netlicensing.domain.vo.Composition;
import com.labs64.netlicensing.domain.vo.ValidationResult;

public class LicenseCheckerImpl implements LicenseChecker {

	private LicenseType licenseType;
	private boolean status;
	private Instant timestamp;
	private String sessionId;
	private String licenseeName;

	// TODO: check if the following keys match with the keys of the validation
	// properties
	private final String LICENSING_MODEL_KEY = "productModuleNumber";
	private final String SESSION_ID_KEY = "sessionId";
	private final String VALID_KEY = "valid";

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
				case SESSION_ID_KEY:
					setSessionId(value.getValue());
					break;
				case VALID_KEY:
					boolean valid = Boolean.valueOf(value.getValue());
					setStatus(valid);
					break;
				}
				
			});
		});
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

	@Override
	public String getFloatingSessionId() {
		return this.sessionId;
	}

	private void setLicenseType(LicenseType licenseType) {
		this.licenseType = licenseType;
	}

	private void setSessionId(String sessionId) {
		this.sessionId = sessionId;
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
}
