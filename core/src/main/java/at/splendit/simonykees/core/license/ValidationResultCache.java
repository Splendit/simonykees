package at.splendit.simonykees.core.license;

import java.time.Instant;
import java.util.HashMap;

import com.labs64.netlicensing.domain.vo.ValidationResult;

/**
 * A cash implementation for storing the result of a single validation call.
 * 
 * @author ardit.ymeri
 *
 */
public class ValidationResultCache {
	private static ValidationResultCache instance;
	
	private final String TIME_STAMP_KEY = "time-stamp";
	private final String VALIDATION_RESULT_KEY = "validation-result";
	private HashMap<String, Object> cacheHashMap = new HashMap<>();


	private ValidationResultCache() {

	}
	
	public void updateCachedResult(ValidationResult validationResult, Instant timestamp) {
		cacheHashMap.put(TIME_STAMP_KEY, timestamp);
		cacheHashMap.put(VALIDATION_RESULT_KEY, validationResult);
	}
	
	public ValidationResult getCachedValidationResult() {
		// TODO: throw an exception if cache is empty
		return (ValidationResult) cacheHashMap.get(VALIDATION_RESULT_KEY);
	}
	
	public Instant getValidationTimestamp() {
		// TODO: throw an exception if cache is empty
		return (Instant) cacheHashMap.get(TIME_STAMP_KEY);
	}

	public synchronized static ValidationResultCache getInstance() {
		if (instance == null) {
			instance = new ValidationResultCache();
		}
		return instance;
	}
}
