package at.splendit.simonykees.core.license;

import java.time.Instant;

import com.labs64.netlicensing.domain.vo.ValidationResult;

/**
 * A cash implementation for storing the result of a single validation call.
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public class ValidationResultCache {
	private static ValidationResultCache instance;
	
	private ValidationResult validationResult;
	private String licenseeName;
	private String licenseeNumber;
	private Instant timestamp;
	private ValidationAction action;
	private boolean isEmpty;


	private ValidationResultCache() {
		isEmpty = true;
	}
	
	public void reset() {
		validationResult = null;
		licenseeName = null;
		licenseeNumber = null;
		timestamp = null;
		action = null;
		isEmpty = true;
	}
	
	public void updateCachedResult(ValidationResult validationResult, String licenseeName, String licenseeNumber, Instant timestamp, ValidationAction action) {
		this.validationResult = validationResult;
		this.licenseeName = licenseeName;
		this.licenseeNumber = licenseeNumber;
		this.timestamp = timestamp;
		this.action = action;
		this.isEmpty = false;
	}
	
	public ValidationResult getCachedValidationResult() {
		// TODO: throw an exception if cache is empty
		return validationResult;
	}
	
	public Instant getValidationTimestamp() {
		// TODO: throw an exception if cache is empty
		return timestamp;
	}
	
	public Boolean isEmpty() {
		return isEmpty;
	}
	
	public String getLicenseName() {
		return licenseeName;
	}
	
	public String getLicenseeNumber() {
		return licenseeNumber;
	}
	
	public ValidationAction getValidatioAction() {
		return action;
	}

	public synchronized static ValidationResultCache getInstance() {
		if (instance == null) {
			instance = new ValidationResultCache();
		}
		return instance;
	}
}
