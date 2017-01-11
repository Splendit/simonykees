package at.splendit.simonykees.core.license;

import java.time.Instant;
import java.util.HashMap;

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
	
	private final String TIME_STAMP_KEY = "time-stamp"; //$NON-NLS-1$
	private final String VALIDATION_RESULT_KEY = "validation-result"; //$NON-NLS-1$
	private final String IS_EMPTY_KEY = "is-empty"; //$NON-NLS-1$
	private final String LICENSEE_NAME = "licensee-name"; //$NON-NLS-1$
	private final String ACTION_KEY = "action"; //$NON-NLS-1$
	private final String LICENSEE_NUMBER = "licensee-number"; //$NON-NLS-1$
	private HashMap<String, Object> cacheHashMap = new HashMap<>();


	private ValidationResultCache() {
		cacheHashMap.put(IS_EMPTY_KEY, true);
	}
	
	public void reset() {
		cacheHashMap.remove(TIME_STAMP_KEY);
		cacheHashMap.remove(VALIDATION_RESULT_KEY);
		cacheHashMap.remove(LICENSEE_NAME);
		cacheHashMap.remove(LICENSEE_NUMBER);
		cacheHashMap.remove(ACTION_KEY);
		cacheHashMap.put(IS_EMPTY_KEY, true);
	}
	
	public void updateCachedResult(ValidationResult validationResult, String licenseeName, String licenseeNumber, Instant timestamp, ValidationAction action) {
		cacheHashMap.put(TIME_STAMP_KEY, timestamp);
		cacheHashMap.put(VALIDATION_RESULT_KEY, validationResult);
		cacheHashMap.put(IS_EMPTY_KEY, false);
		cacheHashMap.put(LICENSEE_NAME, licenseeName);
		cacheHashMap.put(LICENSEE_NUMBER, licenseeNumber);
		cacheHashMap.put(ACTION_KEY, action);
	}
	
	public ValidationResult getCachedValidationResult() {
		// TODO: throw an exception if cache is empty
		return (ValidationResult) cacheHashMap.get(VALIDATION_RESULT_KEY);
	}
	
	public Instant getValidationTimestamp() {
		// TODO: throw an exception if cache is empty
		return (Instant) cacheHashMap.get(TIME_STAMP_KEY);
	}
	
	public Boolean isEmpty() {
		return (Boolean) cacheHashMap.get(IS_EMPTY_KEY);
	}
	
	public String getLicenseName() {
		return (String) cacheHashMap.get(LICENSEE_NAME);
	}
	
	public String getLicenseeNumber() {
		return (String) cacheHashMap.get(LICENSEE_NUMBER);
	}
	
	public ValidationAction getValidatioAction() {
		return (ValidationAction)cacheHashMap.get(ACTION_KEY);
	}

	public synchronized static ValidationResultCache getInstance() {
		if (instance == null) {
			instance = new ValidationResultCache();
		}
		return instance;
	}
}
