package at.splendit.simonykees.core.license;

import java.time.Instant;

import com.labs64.netlicensing.domain.vo.ValidationResult;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.service.LicenseeService;
import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.ValidationParameters;

/**
 * Manager of the validation process. Starts the validation process, caches the
 * result of the validation, and provides relevant information of the validation
 * results.
 * 
 * @author ardit.ymeri
 *
 */
public class LicenseManager {

	private final String LICENSEE_NAME = "test-licensee-name"; // to be provided as a parameter or to be read from a secure storage.
	private final String LICENSEE_NUMBER = "test-licensee-number"; // to be provided as a parameter or to be read from a secure storage.
//	private final String PRODUCT_NUMBER = "test-01"; // to be read from pre-validation
//	private final String PRODUCT_MODULE_NUMBER = "toBeChecked"; // to be read from pre-validation

	private final boolean DO_VALIDATE = true;
	private final long VALIDATE_INTERVAL_IN_SECONDS = 5; // validation interval in seconds.
	
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
		schedulerEntity = new SchedulerEntity(VALIDATE_INTERVAL_IN_SECONDS, DO_VALIDATE);
		
		// make a pre-validate call to get the license model relevant information...
		Context context = APIRestConnection.getAPIRestConnection().getContext();
		try {
			Instant now = Instant.now();
			ValidationResult validationResult = LicenseeService.validate(context, LICENSEE_NUMBER, new ValidationParameters());
			LicenseCheckerImpl checker = new LicenseCheckerImpl(validationResult, now, LICENSEE_NAME);
			
			LicenseType licenseType = checker.getType();
			Instant expireDate = checker.getExprieDate();
			String productNumber = checker.getProductNumber();
			String productModuleNumber = checker.getProductModulNumber();
			
			// cash pre-validation...
			ValidationResultCache cache = ValidationResultCache.getInstance();
			cache.updateCachedResult(validationResult, now);
			
			
			// construct a license model
			LicenseModel licenseModel = constructLicenseModel(licenseType, expireDate, productNumber, productModuleNumber);
			
			// construct a licensee object...
			licensee = new LicenseeEntity(LICENSEE_NAME, LICENSEE_NUMBER, licenseModel, productNumber, productModuleNumber);
			
			// start validate scheduler
			ValidateExecutor.startSchedule(schedulerEntity, licensee);
			
			
		} catch (NetLicensingException e) {
			// TODO proper behavior should be triggered
			e.printStackTrace();
		}
	}
	
	private LicenseModel constructLicenseModel(LicenseType licenseType, Instant expireDate, String productNumber, String productModulNumber) {
		LicenseModel licenseModel = null;
		
		switch(licenseType) {
		case FLOATING: 
			String sessionId = "";// TODO: get cpu id.
			licenseModel = new FloatingModel(productNumber, productModulNumber, expireDate, sessionId);
			break;
		case TRIAL:
			// TODO: to be implemented
			break;
		case NODE_LOCKED:
			String secretKey = "";// TODO: get cpu id.
			licenseModel = new NodeLockModel(productNumber, productModulNumber, expireDate, secretKey);
			break;
		}
		
		return licenseModel;
	}
	
	public LicenseChecker getValidationData() {
		ValidationResultCache cache = ValidationResultCache.getInstance();
		ValidationResult validationResult = cache.getCachedValidationResult();
		Instant timestamp = cache.getValidationTimestamp();
		
		LicenseCheckerImpl checker = new LicenseCheckerImpl(validationResult, timestamp, null);
		
		return checker;
	}
	
	// TODO: override clone()
//	@Override 
//	public Object clone(){
//		throw new RuntimeException();
//	}

}
