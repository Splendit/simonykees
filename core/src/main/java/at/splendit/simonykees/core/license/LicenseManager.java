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

	private final String LICENSEE_NAME = "test-licensee-name"; //$NON-NLS-1$ to be provided  as a parameter or to be read from a secure storage.
	private final String LICENSEE_NUMBER = "test-licensee-number"; //$NON-NLS-1$ to be provided as a parameter or to be read from a secure storage.
	private final String PRODUCT_NUMBER = "test-01"; //$NON-NLS-1$
	private final String PRODUCT_MODULE_NUMBER = "toBeChecked"; //$NON-NLS-1$

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

		Instant now = Instant.now();
		ValidationResult validationResult;
		try {
			// make a pre-validate call to get the license model relevant information...
			validationResult = preValidate(PRODUCT_NUMBER, PRODUCT_MODULE_NUMBER, LICENSEE_NUMBER, LICENSEE_NAME);
			LicenseCheckerImpl checker = new LicenseCheckerImpl(validationResult, now, LICENSEE_NAME);

			// extract pre-validation result
			LicenseType licenseType = checker.getType();
			ZonedDateTime expireDate = checker.getExprieDate();
			String productModuleNumber = checker.getProductModulNumber();

			// cash pre-validation...
			ValidationResultCache cache = ValidationResultCache.getInstance();
			cache.updateCachedResult(validationResult, now);

			// construct a license model
			LicenseModel licenseModel = constructLicenseModel(licenseType, expireDate, PRODUCT_NUMBER, productModuleNumber);

			// construct a licensee object...
			licensee = new LicenseeEntity(LICENSEE_NAME, LICENSEE_NUMBER, licenseModel, PRODUCT_NUMBER);

			// start validate scheduler
			ValidateExecutor.startSchedule(schedulerEntity, licensee);
		} catch (NetLicensingException e) {
			// TODO proper behavior should be triggered
			e.printStackTrace();
		} 

	}

	private ValidationResult preValidate(String productNumber,
										 String productModuleNumber,
										 String licenseeNumber, 
										 String licenseeName) throws NetLicensingException {
		
		Context context = APIRestConnection.getAPIRestConnection().getContext();
		ValidationResult preValidationResult = null;
		ZonedDateTime now = ZonedDateTime.now();
		// to be used only during pre-validation, as a expiration date.
		ZonedDateTime nowInOneYear = now.plusYears(1);
		FloatingModel floatingModel = new FloatingModel(productModuleNumber, nowInOneYear, getUniqueNodeIdentifier());
		NodeLockedModel nodeLockedModel = new NodeLockedModel(productModuleNumber, nowInOneYear, getUniqueNodeIdentifier());


		// pre-validation with floating license model...
		LicenseeEntity licensee = new LicenseeEntity(licenseeName, licenseeNumber, floatingModel, productNumber);
		ValidationParameters valParams = licensee.getValidationParams();
		preValidationResult = LicenseeService.validate(context, LICENSEE_NUMBER, valParams);
		LicenseCheckerImpl checker = new LicenseCheckerImpl(preValidationResult, now.toInstant(), licenseeName);

		// if the pre-validation with floating license model fails, then try
		// a node-locked pre-validation...
		if (!checker.getStatus()) {
			licensee = new LicenseeEntity(licenseeName, licenseeNumber, nodeLockedModel, productNumber);
				valParams = licensee.getValidationParams();
			preValidationResult = LicenseeService.validate(context, LICENSEE_NUMBER, valParams);

		}


		return preValidationResult;
	}

	private LicenseModel constructLicenseModel(LicenseType licenseType, ZonedDateTime expireDate, String productNumber,
			String productModulNumber) {
		LicenseModel licenseModel = null;

		switch (licenseType) {
		case FLOATING:
			String sessionId = getUniqueNodeIdentifier();
			licenseModel = new FloatingModel(productModulNumber, expireDate, sessionId);
			break;
		case TRIAL:
			// TODO: to be implemented
			break;
		case NODE_LOCKED:
			String secretKey = getUniqueNodeIdentifier();
			licenseModel = new NodeLockedModel(productModulNumber, expireDate, secretKey);
			break;
		}

		return licenseModel;
	}

	private String getUniqueNodeIdentifier() {
		// TODO get CPU ID
		return "";
	}

	public LicenseChecker getValidationData() {
		ValidationResultCache cache = ValidationResultCache.getInstance();
		ValidationResult validationResult = cache.getCachedValidationResult();
		Instant timestamp = cache.getValidationTimestamp();

		LicenseCheckerImpl checker = new LicenseCheckerImpl(validationResult, timestamp, null);

		return checker;
	}

	// TODO: override clone()
	// @Override
	// public Object clone(){
	// throw new RuntimeException();
	// }

}
