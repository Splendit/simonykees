package at.splendit.simonykees.core.license;

import java.time.Instant;
import java.time.ZonedDateTime;

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

	public static final String LICENSEE_NAME = "License-Ali-Test"; //$NON-NLS-1$ to be provided  as a parameter or to be read from a secure storage.
	public static final String LICENSEE_NUMBER = "IITAK75GN"; //$NON-NLS-1$ to be provided as a parameter or to be read from a secure storage.
	private final String PRODUCT_NUMBER = "PNZNF7Y7E"; //$NON-NLS-1$
	private final String PRODUCT_MODULE_NUMBER = "M6IS9TIWG"; //$NON-NLS-1$ product module number for floating

	private final boolean DO_VALIDATE = true;
	private final long VALIDATE_INTERVAL_IN_SECONDS = 5; // validation interval in seconds.

	private static LicenseManager instance;

	private SchedulerEntity schedulerEntity;
	private LicenseeEntity licensee;
	private LicenseModel licenseModel;

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

		Instant now = Instant.now();

		PersistenceManager persistenceManager = PersistenceManager.getInstance();
		LicenseType licenseType;
		ZonedDateTime evaluationExpiresDate;
		ZonedDateTime expirationTimeStamp;
		
		try {
			// make a pre-validate call to get the license model relevant information...
			ValidationResult validationResult = preValidate(PRODUCT_NUMBER, PRODUCT_MODULE_NUMBER, LICENSEE_NUMBER, LICENSEE_NAME);
			LicenseCheckerImpl checker = new LicenseCheckerImpl(validationResult, now, LICENSEE_NAME);
			
			// cash and persist pre-validation...
			ValidationResultCache cache = ValidationResultCache.getInstance();
			cache.updateCachedResult(validationResult, now);
			persistenceManager.persistCachedData();
			
			// extract pre-validation result
			licenseType = checker.getType();
			evaluationExpiresDate = checker.getEvaluationExpiresDate();
			expirationTimeStamp = checker.getExpirationTimeStamp();
			
		} catch (NetLicensingException e) {
			PersistenceModel persistedData = persistenceManager.getPersistenceModel();
			
			licenseType = persistedData.getLicenseType().orElse(null);
			evaluationExpiresDate = persistedData.getDemoExpirationDate().orElse(null);
			expirationTimeStamp = persistedData.getExpirationTimeStamp().orElse(null);
		}

		// construct a license model
		LicenseModel licenseModel = constructLicenseModel(licenseType, evaluationExpiresDate, expirationTimeStamp, PRODUCT_MODULE_NUMBER);
		setLicenseModel(licenseModel);

		// construct a licensee object...
		LicenseeEntity licensee = new LicenseeEntity(LICENSEE_NAME, LICENSEE_NUMBER, licenseModel, PRODUCT_NUMBER);
		setLicensee(licensee);

		// start validate scheduler
		ValidateExecutor.startSchedule(schedulerEntity, licensee);
 
	}

	private void setLicensee(LicenseeEntity licensee) {
		this.licensee = licensee;
	}

	private ValidationResult preValidate(String productNumber,
										 String productModuleNumber,
										 String licenseeNumber, 
										 String licenseeName) throws NetLicensingException {
		
		Context context = RestApiConnection.getAPIRestConnection().getContext();
		ValidationResult preValidationResult = null;
		ZonedDateTime now = ZonedDateTime.now();
		// to be used only during pre-validation, as a expiration date.
		ZonedDateTime nowInOneYear = now.plusYears(1);
		// pre-validation is done by using floating model 
		// because it contains a superset of all other model's validation parameters 
		FloatingModel floatingModel = new FloatingModel(productModuleNumber, nowInOneYear, getUniqueNodeIdentifier());

		// pre-validation with floating license model...
		LicenseeEntity licensee = new LicenseeEntity(licenseeName, licenseeNumber, floatingModel, productNumber);
		ValidationParameters valParams = licensee.getValidationParams();
		logPrevalidationRequest(LICENSEE_NUMBER, valParams);
		preValidationResult = LicenseeService.validate(context, LICENSEE_NUMBER, valParams);
		logPrevalidationResponse(preValidationResult);

		return preValidationResult;
	}
	
	
	private void logPrevalidationRequest(String licenseeNumber, ValidationParameters validationParams) {
		System.out.println("------Prevalidation request-----");
		System.out.println("Licensee number:" + licenseeNumber);
		validationParams.getParameters().forEach((key, val) -> {
			System.out.println("key: " + key);
			val.forEach((mapKey, mapVal) -> {
				System.out.println(mapKey + ":" + mapVal);
			});
		});
		System.out.println("------------------------\n");
		
	}
	
	private void logPrevalidationResponse(ValidationResult validationResult) {
		System.out.println("------Prevalidation response-----");
		System.out.println("size: " +  validationResult.getValidations().size());
		
		System.out.print(validationResult.toString());
		
		System.out.println("------------------------\n");
	}

	/**
	 * Sends a request to free one session from the session pool available
	 * for Floating License Model. Relevant only for the case when the 
	 * licensing model is Floating. 
	 */
	public void checkIn() {
		LicenseModel licenseModel = getLicenseModel();
		if(licenseModel instanceof FloatingModel){
			Context context = RestApiConnection.getAPIRestConnection().getContext();
			FloatingModel floatingModel = (FloatingModel)licenseModel;
			ValidationParameters checkingValParameters = floatingModel.getCheckInValidationParameters();
			try {
				Instant now = Instant.now();
				ValidationResult checkinResult = LicenseeService.validate(context, LICENSEE_NUMBER, checkingValParameters);
				ValidationResultCache cache = ValidationResultCache.getInstance();
				cache.updateCachedResult(checkinResult, now);
				 
			} catch (NetLicensingException e) {
				// TODO add a validation status indicating that the checkin was not successful.
				e.printStackTrace();
			}		
		}
	}
	
	public LicenseeEntity getLicensee() {
		return licensee;
	}

	private LicenseModel constructLicenseModel(LicenseType licenseType, ZonedDateTime expireDate, ZonedDateTime expireTimeStamp, String productModulNumber) {
		LicenseModel licenseModel;

		switch (licenseType) {
		case FLOATING:
			String sessionId = getUniqueNodeIdentifier();
			licenseModel = new FloatingModel(productModulNumber, expireTimeStamp, sessionId);
			break;
		case TRY_AND_BUY:
			String secret = getUniqueNodeIdentifier();
			licenseModel = new TryAndBuyModel(productModulNumber, expireDate, secret);
			break;
		case NODE_LOCKED:
			String secretKey = getUniqueNodeIdentifier();
			licenseModel = new NodeLockedModel(productModulNumber, expireDate, secretKey);
			break;
		default:
			licenseModel = null;
			break;
		}

		return licenseModel;
	}

	private String getUniqueNodeIdentifier() {
		// TODO get CPU ID
		return "test-unique-machine-id";
	}

	public LicenseChecker getValidationData() {
		ValidationResultCache cache = ValidationResultCache.getInstance();
		LicenseChecker checker;
		if(!cache.isEmpty()) {
			ValidationResult validationResult = cache.getCachedValidationResult();
			Instant timestamp = cache.getValidationTimestamp();
			checker = new LicenseCheckerImpl(validationResult, timestamp, LICENSEE_NAME);
		} else {
			PersistenceManager persistenceManager = PersistenceManager.getInstance();
			checker = persistenceManager.vlidateUsingPersistedData();
		}

		return checker;
	}
	

	public LicenseModel getLicenseModel() {
		return licenseModel;
	}
	

	private void setLicenseModel(LicenseModel licenseModel) {
		this.licenseModel = licenseModel;
	}

	// TODO: override clone()
	// @Override
	// public Object clone(){
	// throw new RuntimeException();
	// }

}
