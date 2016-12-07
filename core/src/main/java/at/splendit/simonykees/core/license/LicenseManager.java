package at.splendit.simonykees.core.License;

import java.time.Instant;

import com.labs64.netlicensing.domain.vo.ValidationResult;

/**
 * Manager of the validation process. Starts the validation process, caches the
 * result of the validation, and provides relevant information of the validation
 * results.
 * 
 * @author ardit.ymeri
 *
 */
public class LicenseManager {

	private final String LICENSEE_NAME = "test-licensee-name"; // to be provided as a parameter.
	private final String LICENSEE_NUMBER = "test-licensee-number"; // to be provided as a parameter.
	private final boolean DO_VALIDATE = true;
	private final long VALIDATE_INTERVAL = 5; // validation interval in seconds.
	
	private static LicenseManager instance;
	private LicenseChecker licenseChecker;
	
	private SchedulerEntity schedulerEntity;
	private LicenseeEntity licensee;
	

	private LicenseManager() {
		// TODO: throw an exception if the instance is not null...
		initManager();
	}

	public synchronized static LicenseManager getInstance() {
		if (instance == null) {
			instance = new LicenseManager();
		}
		return instance;
	}
	
	private void initManager() {
		schedulerEntity = new SchedulerEntity(VALIDATE_INTERVAL, DO_VALIDATE);
		licensee = new LicenseeEntity(LICENSEE_NAME, LICENSEE_NUMBER);
		
		ValidateExecutor.startSchedule(schedulerEntity, licensee);
	}
	
	public LicenseChecker getValidationData() {
		ValidationResultCache cache = ValidationResultCache.getInstance();
		ValidationResult validationResult = cache.getCachedValidationResult();
		Instant timestamp = cache.getValidationTimestamp();
		
		LicenseCheckerImpl checker = new LicenseCheckerImpl(validationResult, timestamp);
		
		return checker;
	}
	
	// TODO: override clone()
//	@Override 
//	public Object clone(){
//		throw new RuntimeException();
//	}

}
